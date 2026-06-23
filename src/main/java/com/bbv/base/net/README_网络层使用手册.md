# 网络层使用手册

## 目录

- [一、概述](#一概述)
- [二、最简上手](#二最简上手)
- [三、CommonResponse 响应格式](#三commonresponse-响应格式)
- [四、BaseRetrofitServiceProvider](#四baseretrofitserviceprovider)
- [五、拦截器](#五拦截器)
- [六、错误处理](#六错误处理)
- [七、@WithoutStepParse 注解](#七withoutstepparse-注解)
- [八、NetworkRequestUtil（轻量 OkHttp）](#八networkrequestutil轻量-okhttp)
- [九、常见问题](#九常见问题)

---

## 一、概述

本网络层分为两种使用方式：

| 方式 | 适用场景 | 特点 |
|---|---|---|
| **Retrofit + ServiceProvider** | 正式项目、统一API管理 | 自动解析 CommonResponse、拦截器、动态超时 |
| **NetworkRequestUtil** | 简单请求、快速调试 | 工具类风格、同步/异步、手动解析 |

> **推荐正式接口都走 Retrofit 方式**，NetworkRequestUtil 更适合临时调试或不重要的请求。

---

## 二、最简上手

### 1. 定义 API 接口

```kotlin
interface UserApi {
    @GET("user/info")
    suspend fun getUserInfo(): User

    @POST("user/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
}
```

### 2. 创建 ServiceProvider

```kotlin
class UserServiceProvider : BaseRetrofitServiceProvider() {
    val api: UserApi by lazy {
        genRetrofitClient("https://api.example.com/")
            .create(UserApi::class.java)
    }
}
```

### 3. 在 ViewModel 中调用

```kotlin
class UserViewModel : ViewModel() {
    private val provider = UserServiceProvider()
    val user = MutableLiveData<User>()

    fun loadUser() {
        viewModelScope.launch {
            try {
                val result = provider.api.getUserInfo()
                user.value = result
            } catch (e: Exception) {
                // 使用 toShowMsg() 获取用户可读的错误消息
                toast(e.toShowMsg())
            }
        }
    }
}
```

---

## 三、CommonResponse 响应格式

### 服务端返回格式

```json
{
    "code": "200",
    "msg": "success",
    "result": { ... 实际业务数据 ... }
}
```

### 自动解析流程

```
服务端返回 JSON
    ↓
MoshiResponseBodyConverter 拦截
    ↓
判断有无 @WithoutStepParse 注解
    ├── 有 → 直接解析为目标类型（如 String、User）
    └── 无 → 解析为 CommonResponse
         ├── code == "200" → 自动提取 result 反序列化为 API 方法返回类型
         ├── code != "200" → 抛出 RetrofitException(SERVER)
         └── result == null → 返回空对象
```

**你只需在 API 接口里写返回类型即可，无需关心 CommonResponse 包装：**

```kotlin
// 接口定义
@GET("user/info")
suspend fun getUserInfo(): User

// 调用时直接拿到 User，无需手动解包
val user: User = api.getUserInfo()
```

### 关于 result 为 null 的情况

当 `code == "200"` 但 `result` 为 `null` 时：
- 如果 API 返回类型是 `List<T>` → 返回空 `ArrayList`
- 如果 API 返回类型是 `T` → 返回 `new Object()`
- Retrofit 不会报 null 错误

### 配置成功状态码

```kotlin
// 如果你的服务端用的是 "0" 而不是 "200"
NetConfig.RESPONSE_CODE_SUCCESS = "0"
```

---

## 四、BaseRetrofitServiceProvider

### 默认提供的配置

| 配置项 | 默认值 |
|---|---|
| 超时（连接/读取/写入） | 各 10 秒 |
| JSON 解析 | Moshi（带数字智能适配、Observable 适配） |
| 日志 | 调试模式自动打印请求/响应 |
| 拦截器 | DynamicTimeoutInterceptor + 日志 |

### 自定义超时

```kotlin
class UserServiceProvider : BaseRetrofitServiceProvider() {
    val api: UserApi by lazy {
        genRetrofitClient(
            baseUrl = "https://api.example.com/",
            connectTimeout = 5000L,  // 5秒
            readTimeout = 30000L     // 30秒（下载大文件）
        ).create(UserApi::class.java)
    }
}
```

### 自定义拦截器

```kotlin
class UserServiceProvider : BaseRetrofitServiceProvider() {
    override fun addInterceptors(builder: OkHttpClient.Builder) {
        // 先调用父类（保留默认的动态超时 + 日志）
        super.addInterceptors(builder)
        // 添加自定义拦截器
        builder.addInterceptor(CommonHeaderInterceptor())
        builder.addInterceptor(MyCustomInterceptor())
    }

    val api: UserApi by lazy {
        genRetrofitClient("https://api.example.com/")
            .create(UserApi::class.java)
    }
}
```

### 自定义 Converter

```kotlin
class UserServiceProvider : BaseRetrofitServiceProvider() {
    override fun addCallAdapterFactories(builder: Retrofit.Builder) {
        // 添加 Gson 作为备选解析器
        builder.addConverterFactory(GsonConverterFactory.create())
    }
    // ... 其他代码
}
```

---

## 五、拦截器

### 1. CommonHeaderInterceptor（通用请求头）

```kotlin
// 1. 全局配置
NetConfig.HEADERS["Authorization"] = "Bearer tokenxxx"
NetConfig.HEADERS["Device-Id"] = "abc123"
NetConfig.HEADERS["App-Version"] = "1.0.0"

// 2. 创建拦截器实例（会从 NetConfig.HEADERS 读取全局头）
val headerInterceptor = CommonHeaderInterceptor()

// 3. 添加到 ServiceProvider
class AppServiceProvider : BaseRetrofitServiceProvider() {
    override fun addInterceptors(builder: OkHttpClient.Builder) {
        super.addInterceptors(builder)
        builder.addInterceptor(CommonHeaderInterceptor())
    }
}
```

### 2. DynamicTimeoutInterceptor（动态超时）

默认超时 10 秒。如果需要某个接口超时更长，在请求前添加请求头：

```kotlin
// 方式一：Retrofit 注解
@GET("large/file/download")
@Headers("DYNAMIC-READ-TIMEOUT: 60")
suspend fun downloadLargeFile(): ResponseBody

// 方式二：手动构建 Request
val request = Request.Builder()
    .url("https://api.example.com/slow-api")
    .header(DynamicTimeoutInterceptor.DYNAMIC_CONNECT_TIMEOUT, "20") // 20秒
    .header(DynamicTimeoutInterceptor.DYNAMIC_READ_TIMEOUT, "60")    // 60秒
    .build()
```

支持的请求头（值单位：**秒**）：

| 请求头 | 含义 |
|---|---|
| `DYNAMIC-CONNECT-TIMEOUT` | 连接超时 |
| `DYNAMIC-READ-TIMEOUT` | 读取超时 |
| `DYNAMIC-WRITE-TIMEOUT` | 写入超时 |

---

## 六、错误处理

### 错误类型

```kotlin
enum class ErrorKind {
    NETWORK,      // 网络不通、DNS 解析失败
    HTTP,         // HTTP 状态码错误（非 200）
    SERVER,       // 服务端业务错误（code != "200"）
    DATA_PARSE,   // JSON 解析失败
    UNEXPECTED    // 其他未知异常
}
```

### 捕获异常并显示错误消息

```kotlin
viewModelScope.launch {
    try {
        val result = api.getUserInfo()
        // 成功处理
    } catch (e: Exception) {
        // 一行代码转为用户可读的错误提示
        toast(e.toShowMsg())
    }
}
```

`toShowMsg()` 会返回以下消息：

| 异常类型 | 提示文字 |
|---|---|
| RetrofitException(HTTP) | "遇到错误" |
| RetrofitException(SERVER) | commonResponse?.msg 或 "服务器未知错误" |
| RetrofitException(DATA_PARSE) | "数据解析错误" |
| RetrofitException(NETWORK) | "网络IO错误" |
| HttpException | "HTTP错误，${code}" |
| IOException | "网络IO错误" |
| 其他 | "请检查网络设置" |

### 细粒度错误处理

```kotlin
viewModelScope.launch {
    try {
        val result = api.getUserInfo()
    } catch (e: RetrofitException) {
        when (e.errorKind) {
            ErrorKind.HTTP -> toast("网络连接失败")
            ErrorKind.SERVER -> toast(e.commonResponse?.msg ?: "服务端错误")
            ErrorKind.DATA_PARSE -> toast("数据格式异常")
            ErrorKind.NETWORK -> toast("请检查网络连接")
            ErrorKind.UNEXPECTED -> toast("未知错误：${e.message}")
        }
    } catch (e: IOException) {
        toast("网络连接异常")
    }
}
```

---

## 七、@WithoutStepParse 注解

### 什么时候用？

当接口**不是**标准 CommonResponse 格式时使用。例如：

**例1：直接返回字符串**
```json
"success"
```
```kotlin
@WithoutStepParse
@GET("health/check")
suspend fun healthCheck(): String
```

**例2：返回未包装的对象**
```json
{ "id": 1, "name": "test" }
```
```kotlin
@WithoutStepParse
@GET("raw/data")
suspend fun getRawData(): RawData
```

**例3：下载文件**
```kotlin
@WithoutStepParse
@GET("file/download")
suspend fun download(@Url url: String): ResponseBody
```

不加 `@WithoutStepParse` 时，转换器默认按 `CommonResponse` 格式解析。

---

## 八、NetworkRequestUtil（轻量 OkHttp）

如果不想定义 Retrofit 接口，可以用 `NetworkRequestUtil`。

### GET 请求

```kotlin
// 异步 GET
NetworkRequestUtil.getAsync("https://api.example.com/data",
    params = mapOf("page" to "1"),
    callback = object : Callback {
        override fun onFailure(call: Call, e: IOException) { }
        override fun onResponse(call: Call, response: Response) { }
    })

// 异步 GET + 自动 Gson 解析
NetworkRequestUtil.getAsyncWithResult<User>(
    url = "https://api.example.com/user/1",
    onSuccess = { user -> /* 直接用 User 对象 */ },
    onError = { e -> /* 错误处理 */ }
)

// 同步 GET + 自定义异常处理
try {
    val response = NetworkRequestUtil.getSync("https://api.example.com/data")
    val user: User = NetworkRequestUtil.handleResponse(response)
} catch (e: NetworkRequestUtil.UnauthorizedException) {
    // 401
} catch (e: NetworkRequestUtil.ServerException) {
    // 5xx
}
```

### POST 请求

```kotlin
// POST JSON
NetworkRequestUtil.postJsonAsync("https://api.example.com/login",
    jsonBody = """{"name":"test","pwd":"123"}""",
    callback = ...)

// POST 表单
NetworkRequestUtil.postFormAsync("https://api.example.com/login",
    formParams = mapOf("name" to "test", "pwd" to "123"),
    callback = ...)
```

### 上传文件

```kotlin
NetworkRequestUtil.uploadFileAsync("https://api.example.com/upload",
    paramName = "file",
    file = File("/path/to/image.jpg"),
    formParams = mapOf("type" to "avatar"),
    callback = ...)
```

### 查看所有方法

| 方法 | 说明 |
|---|---|
| `getAsync` | 异步 GET，返回 Response |
| `getAsyncWithResult<T>` | 异步 GET，自动 Gson 解析为 T |
| `getSync` | 同步 GET，返回 Response |
| `getSyncWithResult<T>` | 同步 GET，自动 Gson 解析为 T |
| `postJsonAsync` | 异步 POST JSON |
| `postJsonAsyncWithResult<T>` | 异步 POST JSON，自动解析 |
| `postFormAsync` | 异步 POST 表单 |
| `postFormAsyncWithResult<T>` | 异步 POST 表单，自动解析 |
| `uploadFileAsync` | 异步上传文件 |
| `uploadFileAsyncWithResult<T>` | 异步上传文件，自动解析 |

---

## 九、常见问题

**Q: 接口返回 `{"code":"200","msg":"ok","result":null}`，我定义的返回类型是 `User`，会报 null 错误吗？**

A: 不会。转换器自动做了保护——`result` 为 null 时，会返回空对象而不是 null，Retrofit 不会报错。

**Q: 接口返回的 code 不是 "200" 怎么办？**

A: 在 `NetConfig.RESPONSE_CODE_SUCCESS` 设置你的成功状态码，或者让后端统一。

**Q: 某个接口超时了怎么办？**

A: 在 API 方法上加 `@Headers("DYNAMIC-READ-TIMEOUT: 30")`，设置 30 秒超时。

**Q: Retrofit 和 NetworkRequestUtil 选哪个？**

A: 正式业务接口用 Retrofit（规范、统一）；临时调试、简单请求用 NetworkRequestUtil（方便、无定义成本）。

**Q: 调试日志在哪里看？**

A: `BaseRetrofitServiceProvider` 自动通过 `HttpLoggingInterceptor` 打印请求/响应。RELEASE 模式下自动关闭。如果你想额外控制，设置 `Lg.isDebug`。
