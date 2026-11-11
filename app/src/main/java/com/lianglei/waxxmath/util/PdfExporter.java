package com.lianglei.waxxmath.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * 试卷式 PDF 导出：
 * 前后两段 —— 前一段为纯题目卷，后一段为同布局的参考答案卷（页数一一对应）。
 */
public class PdfExporter {

    private static final float PAGE_W = PaperLayout.PAGE_W;
    private static final float PAGE_H = PaperLayout.PAGE_H;
    private static final float MARGIN_L = PaperLayout.MARGIN_L;
    private static final float MARGIN_R = PaperLayout.MARGIN_R;
    private static final float CONTENT_W = PaperLayout.CONTENT_W;
    private static final float CONTENT_TOP = PaperLayout.CONTENT_TOP;
    private static final int ANSWER_RED = 0xFFC62828;

    /** 正文字号（题目），加大到接近课本作业本的字号，便于孩子阅读 */
    private static final float SIZE_BODY = 15f;
    /** 自动缩字时允许的最小字号 */
    private static final float SIZE_MIN = 9f;
    /** 大题标题字号 */
    private static final float SIZE_TITLE = 16f;
    /** 表头字号 */
    private static final float SIZE_HEADER = 12f;
    /** 页脚字号 */
    private static final float SIZE_FOOTER = 10f;

    public static final class Result {
        public final File file;
        public final int total;
        public final int pages;
        public Result(File file, int total, int pages) {
            this.file = file;
            this.total = total;
            this.pages = pages;
        }
    }

    public static Result export(List<PaperLayout.Section> sections, File outFile) throws Exception {
        int total = 0;
        for (PaperLayout.Section s : sections) total += s.questions.size();

        List<PaperLayout.PageData> pages = PaperLayout.build(sections);
        int n = Math.max(1, pages.size());

        PdfDocument doc = new PdfDocument();

        // 第一段：题目卷
        for (int i = 0; i < n; i++) {
            renderPage(doc, i + 1, n, pages.get(i), false);
        }
        // 第二段：参考答案卷（同一布局）
        for (int i = 0; i < n; i++) {
            renderPage(doc, i + 1, n, pages.get(i), true);
        }

        File dir = outFile.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            doc.writeTo(fos);
        } finally {
            doc.close();
        }
        return new Result(outFile, total, n);
    }

    private static void renderPage(PdfDocument doc, int pageNo, int totalPages,
                                   PaperLayout.PageData data, boolean answer) {
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(
                (int) PAGE_W, (int) PAGE_H, pageNo).create();
        PdfDocument.Page page = doc.startPage(info);
        Canvas canvas = page.getCanvas();

        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(Color.BLACK);
        text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        text.setTextSize(SIZE_BODY);

        Paint ans = new Paint(Paint.ANTI_ALIAS_FLAG);
        ans.setColor(ANSWER_RED);
        ans.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        ans.setTextSize(SIZE_BODY);

        Paint thin = new Paint(Paint.ANTI_ALIAS_FLAG);
        thin.setColor(Color.parseColor("#A9B2C0"));
        thin.setStrokeWidth(0.6f);

        drawHeader(canvas, text, thin, answer);
        drawFooter(canvas, text, pageNo, totalPages, answer);

        float y = CONTENT_TOP;
        for (PaperLayout.Row row : data.rows) {
            if (row.isTitle()) {
                // 编号已在排版阶段固定，保证跨页连续
                text.setTextSize(SIZE_TITLE);
                canvas.drawText(row.title, MARGIN_L, y + 21f, text);
                text.setTextSize(SIZE_BODY);
            } else {
                float rowTop = y + 2f;
                float colW = CONTENT_W / row.cols;
                for (int i = 0; i < row.items.size(); i++) {
                    Question q = row.items.get(i);
                    float left = MARGIN_L + i * colW;
                    if (q.vertical) {
                        drawVertical(canvas, q, left, rowTop, colW, row.height, text, ans, answer);
                    } else {
                        float baseline = rowTop + row.height * 0.68f;
                        drawInline(canvas, q, left + 4f, baseline, colW - 8f, text, ans, answer);
                    }
                }
            }
            y += row.height;
        }

        doc.finishPage(page);
    }

    private static void drawHeader(Canvas canvas, Paint text, Paint thin, boolean answer) {
        text.setTextSize(SIZE_HEADER);
        canvas.drawText("姓名：__________    日期：__________    用时：________    得分：________",
                MARGIN_L, 36f, text);
        text.setTextSize(SIZE_HEADER - 0.5f);
        if (answer) {
            Paint red = new Paint(Paint.ANTI_ALIAS_FLAG);
            red.setColor(ANSWER_RED);
            red.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            red.setTextSize(SIZE_HEADER - 1f);
            String tag = "（参考答案）";
            float w = red.measureText(tag);
            canvas.drawText(tag, PAGE_W - MARGIN_R - w, 36f, red);
        }
        canvas.drawLine(MARGIN_L, 50f, PAGE_W - MARGIN_R, 50f, thin);
        text.setTextSize(SIZE_BODY);
    }

    private static void drawFooter(Canvas canvas, Paint text, int pageNo, int totalPages,
                                   boolean answer) {
        text.setTextSize(SIZE_FOOTER);
        String s = (answer ? "参考答案 · " : "")
                + "第 " + pageNo + " 页 / 共 " + totalPages + " 页";
        float w = text.measureText(s);
        canvas.drawText(s, (PAGE_W - w) / 2f, PAGE_H - 14f, text);
        text.setTextSize(SIZE_BODY);
    }

    /** 横式题目：prefix + 占位/答案 + suffix */
    private static void drawInline(Canvas canvas, Question q, float x, float baseline,
                                   float maxW, Paint text, Paint ans, boolean answer) {
        String pre = q.prefix;
        String mid = answer ? q.answer : q.placeholder;
        String suf = q.suffix;

        float size = SIZE_BODY;
        text.setTextSize(size);
        float w = text.measureText(pre) + text.measureText(mid) + text.measureText(suf);
        if (w > maxW && w > 0) {
            size = Math.max(SIZE_MIN, size * maxW / w);
            text.setTextSize(size);
            ans.setTextSize(size);
        }

        canvas.drawText(pre, x, baseline, text);
        float x2 = x + text.measureText(pre);
        if (answer) {
            canvas.drawText(mid, x2, baseline, ans);
        } else {
            canvas.drawText(mid, x2, baseline, text);
        }
        float x3 = x2 + text.measureText(mid);
        canvas.drawText(suf, x3, baseline, text);

        text.setTextSize(SIZE_BODY);
        ans.setTextSize(SIZE_BODY);
    }

    /** 竖式题目 */
    private static void drawVertical(Canvas canvas, Question q, float left, float top,
                                     float colW, float rowH, Paint text, Paint ans,
                                     boolean answer) {
        String a = q.vTop;
        String b = q.vBottom;
        if (a == null || b == null) return;

        float size = SIZE_BODY;
        text.setTextSize(size);
        ans.setTextSize(size);
        float aw = text.measureText(a);
        float bw = text.measureText(b);
        float w = Math.max(aw, bw);
        float cx = left + colW / 2f;
        float rightX = cx + w / 2f;

        float topBase = top + rowH * 0.26f;
        float botBase = topBase + text.getFontSpacing();
        float ruleY = botBase + 6f;

        canvas.drawText(a, rightX - aw, topBase, text);
        canvas.drawText(b, rightX - bw, botBase, text);
        canvas.drawLine(rightX - w - 5f, ruleY, rightX + 5f, ruleY, text);

        if (answer) {
            float ansW = ans.measureText(q.answer);
            canvas.drawText(q.answer, rightX - ansW, ruleY + 20f, ans);
        }

        text.setTextSize(SIZE_BODY);
        ans.setTextSize(SIZE_BODY);
    }
}
