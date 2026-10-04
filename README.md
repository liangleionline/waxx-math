# 数学出题器（Waxx Math）

一款面向儿童/小学生的安卓数学出题工具：按自定义参数**批量生成不重复**的加减乘除口算题，排版为 A4 8×5 网格，导出 PDF 保存到手机根目录 `waxx` 文件夹，方便直接打印。

## 功能

- **随机不重复**：同一批题目不会重复。
  - 注意：受“不重复”约束，单类题数量不能超过其组合空间。例如范围 0~3 的加法最多 `4×4=16` 道不重复题；超出时按实际可生成的最大不重复数量输出。
- **运算方式可选**：加法、减法、乘法、除法可单独或任意组合。
- **数字范围**：`___到___的运算？` 可选填，仅约束加减法的**参与运算数字**（不含结果）。
- **出题数量**：各题型数量可自定义（默认 5，可为 0），四类之和上限 40。
- **横式 / 竖式**：可单选；两者都选时每道题随机采用一种形式。
- **一式两份**：题目总数 ≤ 20 时，可选择在纸张下半部分（第 5 行起）复制一份相同题目。
- **PDF 导出**：A4 纸张，8 行 5 列网格，题目在格子内水平居中、垂直位于格子 1/4 高度；保存至 `/sdcard/waxx/`。

## 界面交互规则

1. 数字范围两框**只填其中一个**，或**起始 > 结束** → “生成题目”按钮置灰。
2. 范围两框都合法填写 → 乘法/除法数量输入框置灰并归 0，仅加减法可调。
3. 四类数量之和 **> 40** → 点击时弹窗提示“题目总数不得超过40”。
4. 四类数量之和 **= 0** → 按钮置灰。
5. 题目总数 **≤ 20** → 弹窗询问是否“一式两份”；**> 20** → 直接生成。

## 技术栈

- 纯 Android Framework API（无第三方依赖），Java 编写。
- PDF 使用系统 `android.graphics.pdf.PdfDocument` 绘制。
- minSdk 23 / targetSdk 33（Android 11+ 通过“所有文件访问”权限写入根目录）。

## 构建（无需 Gradle）

```bash
# 1. 准备 Android SDK（commandline-tools + platforms;android-33 + build-tools;34.0.0）
export ANDROID_HOME=$HOME/Android/Sdk

# 2. 构建并签名 release APK
bash build.sh
# 输出：waxx-math-release.apk
```

构建产物经 zipalign 对齐、apksigner 签名，可直接安装到手机。

## 安装

将 `waxx-math-release.apk` 传入手机后点击安装（需允许“未知来源”）。
首次生成时按提示授予存储权限，PDF 将自动保存到 `手机存储/waxx/`。

## 目录结构

```
waxx-math/
├── build.sh                     # 一键构建脚本
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/lianglei/waxxmath/
│   │   ├── MainActivity.java    # 界面与交互逻辑
│   │   └── util/
│   │       ├── Question.java            # 题目模型
│   │       ├── QuestionGenerator.java   # 随机不重复生成器
│   │       └── PdfExporter.java         # A4 8×5 网格 PDF 导出
│   └── res/                     # 布局 / 资源 / 图标
└── waxx-math-release.apk        # 构建产物
```
