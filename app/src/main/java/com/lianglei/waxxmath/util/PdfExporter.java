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
        text.setTextSize(11f);

        Paint ans = new Paint(Paint.ANTI_ALIAS_FLAG);
        ans.setColor(ANSWER_RED);
        ans.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        ans.setTextSize(11f);

        Paint thin = new Paint(Paint.ANTI_ALIAS_FLAG);
        thin.setColor(Color.parseColor("#A9B2C0"));
        thin.setStrokeWidth(0.6f);

        drawHeader(canvas, text, thin, answer);
        drawFooter(canvas, text, pageNo, totalPages, answer);

        float y = CONTENT_TOP;
        for (PaperLayout.Row row : data.rows) {
            if (row.isTitle()) {
                // 编号已在排版阶段固定，保证跨页连续
                text.setTextSize(11.5f);
                canvas.drawText(row.title, MARGIN_L, y + 16f, text);
                text.setTextSize(11f);
            } else {
                float rowTop = y + 2f;
                float colW = CONTENT_W / row.cols;
                for (int i = 0; i < row.items.size(); i++) {
                    Question q = row.items.get(i);
                    float left = MARGIN_L + i * colW;
                    if (q.vertical) {
                        drawVertical(canvas, q, left, rowTop, colW, row.height, text, ans, answer);
                    } else {
                        float baseline = rowTop + row.height * 0.70f;
                        drawInline(canvas, q, left + 6f, baseline, colW - 12f, text, ans, answer);
                    }
                }
            }
            y += row.height;
        }

        doc.finishPage(page);
    }

    private static void drawHeader(Canvas canvas, Paint text, Paint thin, boolean answer) {
        text.setTextSize(10f);
        canvas.drawText("姓名：____________    日期：____________    用时：__________    得分：__________",
                MARGIN_L, 34f, text);
        text.setTextSize(9f);
        if (answer) {
            Paint red = new Paint(Paint.ANTI_ALIAS_FLAG);
            red.setColor(ANSWER_RED);
            red.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            red.setTextSize(9.5f);
            String tag = "（参考答案）";
            float w = red.measureText(tag);
            canvas.drawText(tag, PAGE_W - MARGIN_R - w, 34f, red);
        }
        canvas.drawLine(MARGIN_L, 45f, PAGE_W - MARGIN_R, 45f, thin);
        text.setTextSize(11f);
    }

    private static void drawFooter(Canvas canvas, Paint text, int pageNo, int totalPages,
                                   boolean answer) {
        text.setTextSize(9f);
        String s = (answer ? "参考答案 · " : "")
                + "第 " + pageNo + " 页 / 共 " + totalPages + " 页";
        float w = text.measureText(s);
        canvas.drawText(s, (PAGE_W - w) / 2f, PAGE_H - 12f, text);
        text.setTextSize(11f);
    }

    /** 横式题目：prefix + 占位/答案 + suffix */
    private static void drawInline(Canvas canvas, Question q, float x, float baseline,
                                   float maxW, Paint text, Paint ans, boolean answer) {
        String pre = q.prefix;
        String mid = answer ? q.answer : q.placeholder;
        String suf = q.suffix;

        float size = 11f;
        text.setTextSize(size);
        float w = text.measureText(pre) + text.measureText(mid) + text.measureText(suf);
        if (w > maxW && w > 0) {
            size = Math.max(7f, size * maxW / w);
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

        text.setTextSize(11f);
        ans.setTextSize(11f);
    }

    /** 竖式题目 */
    private static void drawVertical(Canvas canvas, Question q, float left, float top,
                                     float colW, float rowH, Paint text, Paint ans,
                                     boolean answer) {
        String a = q.vTop;
        String b = q.vBottom;
        if (a == null || b == null) return;

        float size = 10f;
        text.setTextSize(size);
        ans.setTextSize(size);
        float aw = text.measureText(a);
        float bw = text.measureText(b);
        float w = Math.max(aw, bw);
        float cx = left + colW / 2f;
        float rightX = cx + w / 2f;

        float topBase = top + rowH * 0.32f;
        float botBase = top + rowH * 0.32f + text.getFontSpacing();
        float ruleY = botBase + 5f;

        canvas.drawText(a, rightX - aw, topBase, text);
        canvas.drawText(b, rightX - bw, botBase, text);
        canvas.drawLine(rightX - w - 5f, ruleY, rightX + 5f, ruleY, text);

        if (answer) {
            float ansW = ans.measureText(q.answer);
            canvas.drawText(q.answer, rightX - ansW, ruleY + 16f, ans);
        }

        text.setTextSize(11f);
        ans.setTextSize(11f);
    }
}
