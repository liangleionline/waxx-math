# 数学出题器（Waxx Math）

[![Latest Release](https://img.shields.io/github/v/release/liangleionline/waxx-math?label=%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC&color=4C6FFF)](https://github.com/liangleionline/waxx-math/releases)
[![Downloads](https://img.shields.io/github/downloads/liangleionline/waxx-math/total?label=%E7%B4%AF%E8%AE%A1%E4%B8%8B%E8%BD%BD&color=2EA043)](https://github.com/liangleionline/waxx-math/releases)
[![Stars](https://img.shields.io/github/stars/liangleionline/waxx-math?label=Stars&color=FFB020)](https://github.com/liangleionline/waxx-math/stargazers)
[![Last Commit](https://img.shields.io/github/last-commit/liangleionline/waxx-math?label=%E6%9C%80%E8%BF%91%E6%8F%90%E4%BA%A4)](https://github.com/liangleionline/waxx-math/commits)
[![APK Size](https://img.shields.io/github/size/liangleionline/waxx-math/waxx-math-release.apk?label=APK)](https://github.com/liangleionline/waxx-math/releases)
[![Android](https://img.shields.io/badge/Android-6.0%2B-3DDC84?logo=android&logoColor=white)](#下载安装)

一款面向小学生家长的安卓数学出题工具：按**年级 / 学期**选题，批量生成**不重复**题目，自动排成**试卷式 A4 PDF**（卷后附等页数的**参考答案卷**），保存到手机 `Download/waxx`，可一键打印或分享到微信。

## 下载安装

| | |
|---|---|
| 最新版 | [v2.3](https://github.com/liangleionline/waxx-math/releases/latest) |
| 直链 | [waxx-math-release.apk](https://github.com/liangleionline/waxx-math/releases/download/v2.3/waxx-math-release.apk) |
| 系统要求 | Android 6.0（API 23）及以上 |
| 升级方式 | 同一签名，直接覆盖安装，无需卸载 |

首次生成 PDF 前会申请「所有文件访问」权限（Android 11+ 写入公共目录所需）；拒绝后无法保存到 `Download/waxx`。

## 功能特性

### 出题
- **按年级选题**：一年级 ~ 五年级；1–3 年级分上 / 下学期，4–5 年级为全学期。
- **保证不重复**：同一份卷子内按题目内容去重，不会出现两道一样的题。
- **题型种类**：加减法、连加连减、加减混合、带进位 / 退位、表内乘除、四则混合（含带括号）、分数与小数计算，以及一年级新增的**比大小、填括号、找规律填数**。
- **不限总题量**：40 题上限已取消，想要多少出多少（受 PDF 页数影响）。

### 数量与格式
- **只能点 −/+ 调节**，数字框不可手动输入：计算 / 比大小 / 填括号每次 ±4 题，找规律每次 ±2 题，与实际排版的一行题数对齐。
- **横式 / 竖式**可单选，也可两者都选（此时每题随机横式或竖式）；至少选一种。
- **复制上次选择**（`↺ 复制上次`）：一键带入上一次实际打印所选的年级、横竖式、各题型数量。

### 生成前选项（v2.3）
点「生成练习」先弹出选项框，确认后才出 PDF：
- **几份题**：1~9 份，每份页眉标注「第 x 份 / 共 N 份」
- **多份是否相同**：各不相同（跨份也不重复出题）／完全相同（同一套题印 N 份）
- **是否附带参考答案**：答案统一放在整份 PDF 的**最后**，不跟随每份题目；多份相同时答案只印一份
- **记住本次选择**：勾选后下次打开自动填充以上选项

### 排版（v2.0 起为试卷式）
- **一张纸 = 一张卷子**：按题型自动分大题（一、二、三……），跨页续排的大题会标注「（续）」，编号连续不重开。
- **行内题数按题型**：横式 / 比大小 / 填括号一行 4 题；找规律一行 2 题。横式不预留写答案的位置，下一行紧贴。
- **每页表头**：姓名 / 日期 / 用时 / 得分，并有分隔线。
- **页脚页码**：第 x 页 / 共 n 页。
- **大字号（v2.2）**：题目正文 15pt（原 11pt）、大题标题 16pt、竖式 15pt、表头 12pt，行高同步放大，纸面不挤，近距离阅读不伤视力。
- **无页脚**：不打印页码（v2.3 起去掉），纸面更干净。
- 题目过宽时自动缩小字号（最小 9pt），不会溢出纸面。

### 参考答案卷
- 答案统一排在整份 PDF 的**最后几页**，不插在每份题目后面。
- 多份相同：答案只印一份；多份不同：按「参考答案 · 第 x 份」逐份对应，与题目卷卷号一致。
- 答案以**红色**填在原题位置，方便家长批改。

### 文件管理
- 保存路径：`手机存储/Download/waxx/`，文件名含年级、学期与时间戳。
- **自动清理**：每次启动检查该目录，只保留最新的 3 个 PDF，多余的自动删除。
- **分享**：结果弹窗右侧「分享文件」一键调起系统分享，可直接发微信（v2.1 修复了微信提示「获取资源失败」的问题）。

## 支持范围

| 年级 | 学期 | 题型数 | 题型 |
|---|---|---|---|
| 一年级 | 上 | 9 | 10 以内加减、连加连减、加减混合、十几加几、十几减几、20 以内进位加法、比大小、填括号、找规律 |
| 一年级 | 下 | 6 | 20 以内退位减法、两位数加 / 减一位数（进位 / 退位）、比大小、填括号、找规律 |
| 二年级 | 上 | 8 | 100 以内加 / 减（进位 / 退位）、连加、连减、加减混合、表内乘法 |
| 二年级 | 下 | 3 | 除法（可除尽）、加减乘除混合、混合运算（带括号） |
| 三年级 | 上 | 4 | 两位数加减、三位数加减、分数简单计算、两位数乘一位数 |
| 三年级 | 下 | 3 | 三位数除法、两位数乘法、一位小数加减 |
| 四年级 | 全 | 2 | 两位小数加减、四则混合运算 |
| 五年级 | 全 | 1 | 分数加减（分母为个位数） |

> 二 ~ 五年级题型仍在持续补充中（见下方「版本记录」与「后续计划」）。

## 版本记录

> 完整历史与细节见 [CHANGELOG.md](CHANGELOG.md)。

| 版本 | 主要变化 |
|---|---|
| **v2.3** | 生成前选项弹窗（份数 / 是否相同 / 是否带答案 / 记住选择）；多份卷眉标注「第 x 份 / 共 N 份」；答案统一放最后且与卷号对应；去掉页脚页码 |
| **v2.2** | 全卷字号加大：正文 11→15pt、大题标题 16pt、表头 12pt，行高同步放大；版式与分页规则不变 |
| **v2.1** | 修复分享到微信提示「获取资源失败」：Provider 改为按 `Download/waxx` 解析、补 `ClipData` 读权限授权、中文文件名 URI 编码修正 |
| **v2.0** | 大改版：试卷式分区排版（分大题、一张纸一张卷）、取消 40 题上限与「一式两份」、取消数量手动输入（仅 ± 步进）、PDF 增加等页数参考答案卷、每页姓名 / 日期 / 用时 / 得分表头、一年级新增比大小 / 填括号 / 找规律 |
| v1.8 | PDF 改存 `Download/waxx`，启动时仅保留最新 3 个文件 |
| v1.7 | 数量弹窗自动全选原数字并唤起数字键盘 |
| v1.6 | 新增「↺ 复制上次」，记住上次打印所选题型与数量 |
| v1.5 | 弹窗分享按钮移到最右侧，清理未使用资源 |

## 后续计划

- 二 ~ 五年级题型补齐：有余数除法、单位换算、三位数乘两位数竖式、除数是两位数除法、带中括号脱式、面积单位换算、小数乘除、简便运算、大数改写与近似数、异分母分数加减、分数小数百分数互化、简易方程、因数倍数与质合数等。
- 二 ~ 五年级补充「比大小 / 填括号 / 找规律」题型。

## 技术实现

- 纯 Android Framework API，**零第三方依赖、不用 AndroidX**，Java 编写。
- PDF 用系统 `android.graphics.pdf.PdfDocument` 绘制；排版由独立的纯 Java 引擎 `PaperLayout` 完成：先算出分页方案，再以同一布局分别渲染题目卷与答案卷，保证两者页码严格一一对应。
- 分享用自实现的最小 `ContentProvider`（`WaxxFileProvider`），配合 `ClipData` 把文件读权限授权给微信等接收方。
- minSdk 23 / targetSdk 33。

## 构建

```bash
# 1. 准备 Android SDK（platforms;android-33 + build-tools;34.0.0）与 JDK 17
export ANDROID_HOME=$HOME/Android/Sdk

# 2. 构建并签名 release APK（产物：waxx-math-release.apk）
bash build.sh
```

不用 Gradle：`build.sh` 直接调用 `aapt` / `javac` / `d8` / `zipalign` / `apksigner`。签名使用 `keystore/waxx-keystore.jks`（未入库），同签名可覆盖升级。

## 目录结构

```
waxx-math/
├── build.sh                     # 一键构建（aapt + javac + d8 + zipalign + apksigner）
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/lianglei/waxxmath/
│   │   ├── MainActivity.java            # 界面、选题、生成、分享、旧文件清理
│   │   ├── WaxxFileProvider.java        # 分享用 ContentProvider
│   │   └── util/
│   │       ├── Question.java            # 题目模型（CALC / COMPARE / FILL / PATTERN）
│   │       ├── TopicCatalog.java        # 各年级题型目录与出题器
│   │       ├── QuestionGenerator.java   # 随机不重复生成 + 按题型分区
│   │       ├── PaperLayout.java         # 纯 Java 试卷排版 / 分页引擎
│   │       └── PdfExporter.java         # 题目卷 + 答案卷 PDF 渲染
│   └── res/                     # 布局 / 资源 / 图标
└── waxx-math-release.apk        # 构建产物
```

## 发布约定

- 版本号：`versionCode` +1、`versionName` +0.1。
- Release 资产统一命名 `waxx-math-release.apk`。
- 线上**保留最新 3 个 Release**，更早的删除。
- 每次发版同步更新本 README 的「版本记录」与「支持范围」。
