"""
交互式初始化 BaseArch 依赖库
克隆后运行此脚本，按提示输入新库的信息即可自动替换所有占位符
"""
import os
import re
import sys


def prompt(label, default=None, validator=None):
    """提示用户输入，带校验；输入 exit 可随时退出"""
    while True:
        if default:
            hint = f"（默认: {default}）"
        else:
            hint = ""
        value = input(f"  {label} {hint}: ").strip()
        if value.lower() in ("exit", "quit"):
            print("  已退出。")
            sys.exit(0)
        if not value and default:
            return default
        if not value:
            print("  ❌ 输入不能为空，请重新输入")
            continue
        if validator:
            ok, msg = validator(value)
            if not ok:
                print(f"  ❌ {msg}")
                continue
        return value


def confirm(label, default="y"):
    """确认提示"""
    hint = " (Y/n)" if default == "y" else " (y/N)"
    while True:
        value = input(f"  {label}{hint}: ").strip().lower()
        if not value:
            return default == "y"
        if value in ("y", "yes"):
            return True
        if value in ("n", "no"):
            return False
        print("  请输入 y 或 n")


def validate_package(v):
    if not re.match(r'^[a-z_][a-z0-9_]*(\.[a-z_][a-z0-9_]*)*$', v):
        return False, "包名格式错误，正确示例: com.example.mylib"
    return True, ""


def validate_artifact(v):
    if not re.match(r'^[a-z][a-z0-9_-]*$', v):
        return False, "artifactId 格式错误，正确示例: my-lib（小写字母、数字、-、_）"
    return True, ""


def validate_version(v):
    if not re.match(r'^\d+\.\d+\.\d+$', v):
        return False, "版本号格式错误，正确示例: 1.0.0"
    return True, ""


def validate_email(v):
    if '@' not in v or '.' not in v.split('@')[-1]:
        return False, "邮箱格式错误"
    return True, ""


def replace_in_file(filepath, old_str, new_str):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except (FileNotFoundError, IOError):
        return False
    if old_str not in content:
        return False
    content = content.replace(old_str, new_str)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    return True


def rename_directory(src, dst):
    if not os.path.exists(src):
        return False
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    os.rename(src, dst)
    parent = os.path.dirname(src)
    while parent and os.path.basename(parent) not in ('java', 'kotlin', 'main'):
        if os.path.exists(parent) and not os.listdir(parent):
            os.rmdir(parent)
            parent = os.path.dirname(parent)
        else:
            break
    return True


def main():
    print()
    print("=" * 54)
    print("   📦  BaseArch — 依赖库初始化工具")
    print("=" * 54)
    print()
    print("  请按提示输入新库的信息，我们将自动替换模板中的所有占位符。")
    print()

    # ========== 收集信息 ==========
    package_name = prompt(
        "【1/6】请输入包名 (Package Name)",
        validator=validate_package
    )

    artifact_id = prompt(
        "【2/6】请输入 Artifact ID（项目名）",
        validator=validate_artifact
    )

    username = prompt(
        "【3/6】请输入 GitHub 用户名",
        validator=lambda v: (True, "") if v else (False, "不能为空")
    )

    lib_name = prompt(
        "【4/6】请输入库名称（显示名称）",
        default="My Android Library"
    )

    description = prompt(
        "【5/6】请输入库描述",
        default="An Android base architecture library"
    )

    email = prompt(
        "【6/6】请输入开发者邮箱",
        default=f"{username}@users.noreply.github.com",
        validator=validate_email
    )

    version = prompt(
        "  初始版本号",
        default="1.0.0",
        validator=validate_version
    )

    # ========== 确认信息 ==========
    print()
    print("-" * 54)
    print("  请确认以下信息：")
    print()
    print(f"    包名 (Package)     : {package_name}")
    print(f"    项目名 (Artifact)  : {artifact_id}")
    print(f"    GitHub 用户名      : {username}")
    print(f"    库名称             : {lib_name}")
    print(f"    描述               : {description}")
    print(f"    邮箱               : {email}")
    print(f"    版本号             : {version}")
    print("-" * 54)

    if not confirm("  确认无误，开始执行？", default="y"):
        print("  已取消。")
        sys.exit(0)

    # ========== 执行替换 ==========
    old_package = "com.bbv.base"
    old_package_path = old_package.replace('.', '/')
    new_package_path = package_name.replace('.', '/')

    old_artifact = "BaseArch"
    old_lib_name = "BaseArch"
    old_description = "Android base architecture library — MVVM, List paging, network layer, common utils"
    old_email = "gql2012@foxmail.com"

    # 切换到脚本所在目录
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)

    print()
    print("=" * 54)
    print("  正在初始化...")
    print("=" * 54)

    # STEP 1: 重命名源码目录
    print("\n  [1/5] 重命名源码目录...")

    renamed = rename_directory(
        os.path.join("src", "main", "java", old_package_path),
        os.path.join("src", "main", "java", new_package_path)
    )
    if renamed:
        print(f"    ✅ 源码目录已重命名为 {new_package_path}")

    renamed = rename_directory(
        os.path.join("src", "main", "kotlin", old_package_path),
        os.path.join("src", "main", "kotlin", new_package_path)
    )
    if renamed:
        print(f"    ✅ Kotlin 源码目录已重命名")

    if not renamed:
        print("    ⚠️  未找到源码目录，跳过")

    # STEP 2: 替换源文件中的包名
    print("\n  [2/5] 更新源文件中的包声明...")
    updated_count = 0
    for root, dirs, files in os.walk("src"):
        for f in files:
            if not f.endswith(('.kt', '.java', '.xml')):
                continue
            filepath = os.path.join(root, f)
            if replace_in_file(filepath, old_package, package_name):
                updated_count += 1
                short = os.path.relpath(filepath)
                print(f"    ✅ 已更新 {short}")

    if updated_count == 0:
        print("    ⚠️  未找到需更新的源文件")

    # STEP 3: 更新配置文件
    print("\n  [3/5] 更新配置文件...")

    config_replacements = {
        old_package: package_name,
        old_artifact: artifact_id,
        "[UserName]": username,
        old_lib_name: lib_name,
        old_description: description,
        old_email: email,
    }

    config_files = [
        "build.gradle",
        "gradle.properties",
        "proguard-rules.pro",
        "consumer-rules.pro",
        "jitpack.yml",
    ]

    for filename in config_files:
        if not os.path.exists(filename):
            continue
        with open(filename, 'r', encoding='utf-8') as f:
            content = f.read()
        original = content
        for old, new in config_replacements.items():
            content = content.replace(old, new)
        if version != "1.0.0":
            content = re.sub(r'VERSION_NAME=1\.0\.0', f'VERSION_NAME={version}', content)
            major_version = version.split('.')[0]
            content = re.sub(r'VERSION_CODE=1', f'VERSION_CODE={major_version}', content)
        if content != original:
            with open(filename, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"    ✅ 已更新 {filename}")

    # STEP 4: 更新 settings.gradle 的 rootProject.name
    print("\n  [4/5] 更新项目名称...")
    if os.path.exists("settings.gradle"):
        with open("settings.gradle", 'r', encoding='utf-8') as f:
            content = f.read()
        content = re.sub(
            r'rootProject\.name\s*=\s*"[^"]*"',
            f'rootProject.name = "{artifact_id}"',
            content
        )
        with open("settings.gradle", 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"    ✅ rootProject.name 已设置为 '{artifact_id}'")

    # STEP 5: 更新 AndroidManifest.xml
    print("\n  [5/5] 更新 AndroidManifest.xml...")
    manifest_path = os.path.join("src", "main", "AndroidManifest.xml")
    if os.path.exists(manifest_path) and replace_in_file(manifest_path, old_package, package_name):
        print("    ✅ 已更新 AndroidManifest.xml")

    print()
    print("=" * 54)
    print("  ✅  初始化完成！")
    print("=" * 54)
    print()
    print("  后续操作：")
    print(f"    1. 检查更新后的文件")
    print(f"    2. 在 src/main/java/{new_package_path}/ 下编写代码")
    print(f"    3. 修改 build.gradle 中的 dependencies（按需删除不需要的库）")
    print(f"    4. 推送到 GitHub 并通过 JitPack 发布")
    print()


if __name__ == "__main__":
    main()
