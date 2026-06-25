# 导航与 Intent — BaseArch Skill

## 功能概述
提供完整的页面导航方案，包含流式 Intent 启动器、Fragment 容器 Activity、参数工具、以及页面栈管理。
支持**跨页面跳转 + 携带返参返回指定页面**（跳过中间页面）。

## 核心文件

### 1. IntentStarter.kt
流式 Intent 启动器，支持：
- 链式传参 `withData()`
- 结果回调 `withActivityResultCallback { data -> ... }`（已简化，只传 Intent）
- 自动包裹 Fragment 到 `FragmentContainActivity`
- 转场动画控制

### 2. PageManager.kt
页面栈管理器，支持：
- 自动维护 Activity/Fragment 栈
- `backToFragment()` — 跳过中间页面，直接返回指定页面并携带数据
- 目标页面实现 `BackResultListener` 接收回调

### 3. FragmentContainActivity.kt
通用 Fragment 容器 Activity，自动加载 Intent 中指定的 Fragment。

### 4. IntentUtils.kt
Intent/Bundle 参数工具，支持泛型安全读写。

## 关键模式：跨页面跳转 + 返参回传

### 场景
```
PageA → PageB → PageC
```
在 PageC 中完成操作后，携带数据直接返回到 PageA（跳过 PageB）。

### 实现方案

#### 方案 A：标准逐级返回（相邻页面）

**PageA** — 使用 IntentStarter 启动 PageB，监听返回
```kotlin
class PageAFragment : Fragment() {
    fun openPageB() {
        IntentStarter.create(this)
            .withData("key", value)
            .withActivityResultCallback { data ->
                val result = data.getStringExtra("resultKey")
                updateUI(result)
            }
            .startFragment(PageBFragment::class.java)
    }
}
```

**PageB** — 返回时设置结果
```kotlin
class PageBFragment : Fragment() {
    fun onBackWithResult() {
        val intent = Intent().apply {
            putExtra("resultKey", "resultValue")
        }
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }
}
```

#### 方案 B：跨页面返回（跳过中间页面）

**PageA** — 实现 BackResultListener 接口
```kotlin
class PageAFragment : Fragment(), PageManager.BackResultListener {

    override fun onBackResult(data: Bundle) {
        val result = data.getString("resultKey")
        updateUI(result)
    }

    fun openPageB() {
        IntentStarter.create(this)
            .withData("key", value)
            .startFragment(PageBFragment::class.java)
    }
}
```

**PageB** — 不做特殊处理，自然入栈
```kotlin
class PageBFragment : Fragment() {
    fun openPageC() {
        IntentStarter.create(this)
            .withData("data", data)
            .startFragment(PageCFragment::class.java)
    }
}
```

**PageC** — 使用 PageManager 直接跳回 PageA，携带数据
```kotlin
class PageCFragment : Fragment() {
    fun onComplete() {
        val bundle = Bundle().apply {
            putString("resultKey", "来自 PageC 的结果")
        }
        PageManager.backToFragment(PageAFragment::class.java, null, bundle)
    }
}
```

### PageManager 核心方法

| 方法 | 说明 |
|------|------|
| `backToActivity(class, tag?, data?)` | 返回到指定 Activity，关闭中间页面 |
| `backToFragment(class, tag?, data?)` | 返回到指定 Fragment 所在的 Activity |
| `finishActivity(class, tag?)` | 关闭指定 Activity |
| `finishFragment(class, tag?)` | 关闭指定 Fragment |

目标组件需实现 `PageManager.BackResultListener` 接口接收 data。

## 依赖要求
```gradle
dependencies {
    implementation 'androidx.fragment:fragment-ktx:1.6.2'
    implementation 'androidx.activity:activity-ktx:1.8.2'
}
```

## 快速使用模板

### 简洁的伴生对象封装
```kotlin
// 在目标 Fragment 中封装启动方法
class SelectPassengerFragment : Fragment() {

    companion object {
        fun start(
            any: Any,
            list: List<Passenger>? = null,
            activityResultCallback: ((Intent) -> Unit)?
        ) {
            IntentStarter.create(any)
                .withData(IntentConstants.SELECTED_PASSENGER, list)
                .withActivityResultCallback(activityResultCallback)
                .startFragment(SelectPassengerFragment::class.java)
        }
    }

    fun onClickReturn() {
        val resultIntent = Intent().apply {
            putParcelableArrayListExtra(IntentConstants.SELECTED_PASSENGER, selectedList)
        }
        requireActivity().setResult(Activity.RESULT_OK, resultIntent)
        requireActivity().finish()
    }
}

// 调用方一行代码跳转 + 接收结果
SelectPassengerFragment.start(this, passengerList) { data ->
    mViewModel.passengerList.value =
        data.getParcelableArrayListExtra(IntentConstants.SELECTED_PASSENGER)
}
```
