# 数学出题器（Waxx Math）

[![Latest Release](https://img.shields.io/github/v/release/liangleionline/waxx-math?label=%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC&color=4C6FFF)](https://github.com/liangleionline/waxx-math/releases)
[![Downloads](https://img.shields.io/github/downloads/liangleionline/waxx-math/total?label=%E7%B4%AF%E8%AE%A1%E4%B8%8B%E8%BD%BD&color=2EA043)](https://github.com/liangleionline/waxx-math/releases)
[![Stars](https://img.shields.io/github/stars/liangleionline/waxx-math?label=Stars&color=FFB020)](https://github.com/liangleionline/waxx-math/stargazers)
[![Last Commit](https://img.shields.io/github/last-commit/liangleionline/waxx-math?label=%E6%9C%80%E8%BF%91%E6%8F%90%E4%BA%A4)](https://github.com/liangleionline/waxx-math/commits)
[![APK Size](https://img.shields.io/github/size/liangleionline/waxx-math/waxx-math-release.apk?label=APK)](https://github.com/liangleionline/waxx-math/releases)

一款面向小学生家长的安卓数学出题工具：按**年级/学期**选题，批量生成**不重复**题目，自动排成**试卷式** A4 PDF（卷后附等页数的参考答案卷），保存到手机 `Download/waxx` 文件夹，可直接打印或分享到微信。

## 功能

- **按年级选题**：一年级 ~ 五年级；1–3 年级分上/下学期，4–5 年级为全学期。
- **一年级新增题型**：比大小、填括号、找规律填数（上、下学期各一套）。
- **试卷式排版（v2.0）**：一张纸 = 一张卷子。按题型自动分大题（一、二、三……），横式/比大小/填括号一行 4 题，找规律一行 2 题；不预留答题框，下一行紧贴。
- **页首表头**：每页顶部均有「姓名 / 日期 / 用时 / 得分」栏，页脚标注页码。
- **参考答案卷**：PDF 前半段为题目，后半段为答案；题目页数与答案页数严格一一对应（1.5 页题目 → 1.5 页题目 + 1.5 页答案，共 3 页）。
- **数量调节**：只能点 −/+ 调节，不能手动输入；计算/比大小/填括号每次 ±4 题，找规律每次 ±2 题。**不限总题量**。
- **横式 / 竖式**：可单选，也可两者都选（此时每题随机横式或竖式）；至少选一种，不允许都不选。
- **复制上次选择**：一键带入上一次实际打印所选的年级、格式与各题型数量。
- **PDF 导出**：保存至 `/sdcard/Download/waxx/`；启动应用时自动清理旧文件，仅保留最新 3 个 PDF。

## 技术栈

- 纯 Android Framework API（无第三方依赖、无 AndroidX），Java 编写。
- PDF 使用系统 `android.graphics.pdf.PdfDocument` 绘制；排版由独立的纯 Java 引擎 `PaperLayout` 完成（先分页，再按同一布局分别渲染题目卷与答案卷）。
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

将 `waxx-math-release.apk` 传入手机后点击安装（需允许“未知来源”）。首次生成时按提示授予存储权限，PDF 自动保存到 `手机存储/Download/waxx/`；启动时会自动清理旧文件，仅保留最新 3 个 PDF。

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
│   │       ├── Question.java            # 题目模型（CALC/COMPARE/FILL/PATTERN）
│   │       ├── TopicCatalog.java        # 各年级题型生成器
│   │       ├── QuestionGenerator.java   # 随机不重复生成 + 按题型分区
│   │       ├── PaperLayout.java         # 纯 Java 试卷排版/分页引擎
│   │       └── PdfExporter.java         # 题目卷 + 答案卷 PDF 渲染
│   └── res/                     # 布局 / 资源 / 图标
└── waxx-math-release.apk        # 构建产物
```

## Release

- 最新版：https://github.com/liangleionline/waxx-math/releases
