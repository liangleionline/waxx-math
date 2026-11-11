package com.lianglei.waxxmath.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 试卷式 PDF 导出（v2.3）：
 * - 一次可导出 N 份卷子，每份在页眉标注「第 x 份 / 共 N 份」
 * - 多份可以完全相同（重复），也可以各不相同（不重复）
 * - 参考答案统一放在整份 PDF 的最后，不跟随每份题目
 * - 不再绘制页脚页码
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

    private static final String HEADER_TEXT =
            "姓名：__________    日期：__________    用时：________    得分：________";

    public static final class Result {
        public final File file;
        public final int sets;      // 份数
        public final int perSet;    // 每份题数
        public final int qPages;    // 题目页数
        public final int aPages;    // 参考答案页数

        public Result(File file, int sets, int perSet, int qPages, int aPages) {
            this.file = file;
            this.sets = sets;
            this.perSet = perSet;
            this.qPages = qPages;
            this.aPages = aPages;
        }

        public int totalPages() { return qPages + aPages; }
    }

    /**
     * @param distinctSets 各不相同的题目集合：多份重复时传 1 个元素，不重复时传 N 个
     * @param copies       份数
     * @param withAnswer   是否附带参考答案（统一放最后）
     */
    public static Result export(List<List<PaperLayout.Section>> distinctSets, int copies,
                                boolean withAnswer, File outFile) throws Exception {
        if (distinctSets == null || distinctSets.isEmpty()) {
            throw new IllegalArgumentException("没有可导出的题目");
        }
        int copyN = Math.max(1, copies);

        int perSet = 0;
        for (PaperLayout.Section s : distinctSets.get(0)) perSet += s.questions.size();

        // 每套题目各自的排版结果
        List<List<PaperLayout.PageData>> layouts = new ArrayList<>();
        for (List<PaperLayout.Section> set : distinctSets) {
            layouts.add(PaperLayout.build(set));
        }

        PdfDocument doc = new PdfDocument();
        int pageNo = 0;
        int qPages = 0, aPages = 0;

        // 第一段：各份题目卷
        for (int c = 0; c < copyN; c++) {
            List<PaperLayout.PageData> layout = distinctSets.size() == 1
                    ? layouts.get(0) : layouts.get(Math.min(c, layouts.size() - 1));
            String label = copyN > 1 ? "第 " + (c + 1) + " 份 / 共 " + copyN + " 份" : null;
            for (PaperLayout.PageData pd : layout) {
                renderPage(doc, ++pageNo, pd, label, false);
                qPages++;
            }
        }

        // 第二段：参考答案统一放最后
        if (withAnswer) {
            if (layouts.size() == 1) {
                // 多份重复时，答案只需一份
                for (PaperLayout.PageData pd : layouts.get(0)) {
                    renderPage(doc, ++pageNo, pd, "参考答案", true);
                    aPages++;
                }
            } else {
                for (int s = 0; s < layouts.size(); s++) {
                    String label = copyN > 1
                            ? "参考答案 · 第 " + (s + 1) + " 份" : "参考答案";
                    for (PaperLayout.PageData pd : layouts.get(s)) {
                        renderPage(doc, ++pageNo, pd, label, true);
                        aPages++;
                    }
                }
            }
        }

        File dir = outFile.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            doc.writeTo(fos);
        } finally {
            doc.close();
        }
        return new Result(outFile, copyN, perSet, qPages, aPages);
    }

    private static void renderPage(PdfDocument doc, int pageNo, PaperLayout.PageData data,
                                   String rightLabel, boolean answer) {
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

        drawHeader(canvas, text, thin, rightLabel, answer);

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

    /** 页眉：左侧信息栏，右侧标注「第 x 份」或「参考答案」 */
    private static void drawHeader(Canvas canvas, Paint text, Paint thin,
                                   String rightLabel, boolean answer) {
        Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
        label.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        label.setTextSize(SIZE_HEADER);
        label.setColor(answer ? ANSWER_RED : Color.BLACK);

        float reserved = 0f;
        if (rightLabel != null && !rightLabel.isEmpty()) {
            reserved = label.measureText(rightLabel) + 10f;
        }

        // 右侧要放标注时，若信息栏放不下则逐级微缩，避免文字相撞
        float size = SIZE_HEADER;
        text.setTextSize(size);
        text.setColor(Color.BLACK);
        float avail = CONTENT_W - reserved;
        while (size > 9f && text.measureText(HEADER_TEXT) > avail) {
            size -= 0.5f;
            text.setTextSize(size);
        }
        canvas.drawText(HEADER_TEXT, MARGIN_L, 36f, text);

        if (reserved > 0f) {
            float w = label.measureText(rightLabel);
            canvas.drawText(rightLabel, PAGE_W - MARGIN_R - w, 36f, label);
        }
        canvas.drawLine(MARGIN_L, 50f, PAGE_W - MARGIN_R, 50f, thin);
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
