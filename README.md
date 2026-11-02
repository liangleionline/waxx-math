# 数学出题器（Waxx Math）

一款面向小学生家长的安卓数学口算出题工具：按**年级/学期**选题，批量生成**不重复**题目，排版为 A4 8×5 网格 PDF，保存到手机根目录 `waxx` 文件夹，可直接打印或分享到微信。

## 功能

- **按年级选题**：一年级 ~ 五年级；1–3 年级分上/下学期，4–5 年级为全学期，共 30 个题型（10以内加减、进位退位、表内乘法、除法、小数加减、分数加减、四则混合等）。
- **题型步进器**：每种题型可独立设数量（点击数量框可直接键盘输入，或用 −/+ 步进），跨题型混合选题，合计上限 40。
- **横式 / 竖式**：可单选；两者都选时每道题随机采用一种形式（分数、小数、连加连减等复杂题型固定横式）。
- **一式两份**：题目总数 ≤ 20 时，可选择在纸张下半部分复制一份相同题目。
- **PDF 导出**：A4 8行5列网格，保存至 `/sdcard/waxx/`；生成后弹窗可直接分享到微信等应用。

## 技术栈

- 纯 Android Framework API（无第三方依赖、无 AndroidX），Java 编写。
- PDF 使用系统 `android.graphics.pdf.PdfDocument` 绘制。
- 分享通过自实现的最小 ContentProvider（`WaxxFileProvider`）。
- minSdk 23 / targetSdk 33（Android 11+ 通过“所有文件访问”权限写入根目录）。

## 构建（无需 Gradle）

```bash
# 1. 准备 Android SDK（platforms;android-33 + build-tools;34.0.0）
export ANDROID_HOME=$HOME/Android/Sdk

# 2. 构建并签名 release APK（输出 waxx-math-release.apk）
bash build.sh
```

构建产物经 zipalign 对齐、apksigner 签名（keystore 位于 `keystore/`，未入库），可直接安装到手机，后续版本用同一签名可覆盖升级。

## 安装

将 `waxx-math-release.apk` 传入手机后点击安装（需允许“未知来源”）。首次生成时按提示授予存储权限，PDF 自动保存到 `手机存储/waxx/`。

## 目录结构

```
waxx-math/
├── build.sh                     # 一键构建脚本
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/lianglei/waxxmath/
│   │   ├── MainActivity.java            # 界面与交互逻辑
│   │   ├── WaxxFileProvider.java        # 分享用 ContentProvider
│   │   └── util/
│   │       ├── Question.java            # 题目模型（横式/竖式两行）
│   │       ├── TopicCatalog.java        # 30 个题型生成器
│   │       ├── QuestionGenerator.java   # 随机不重复生成器
│   │       └── PdfExporter.java         # A4 8×5 网格 PDF 导出
│   └── res/                     # 布局 / 资源 / 图标
└── waxx-math-release.apk        # 构建产物
```

## Release

- 最新版：https://github.com/liangleionline/waxx-math/releases
