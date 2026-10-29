package com.lianglei.waxxmath.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * 将题目按 A4 8行5列网格导出为 PDF。
 * 题目水平居中、垂直位于格子高度 1/4 处；支持一式两份。
 */
public class PdfExporter {

    private static final int ROWS = 8, COLS = 5;
    private static final float MARGIN = 30f;
    private static final float PAGE_W = 595f; // A4 宽度(pt)
    private static final float PAGE_H = 842f; // A4 高度(pt)

    public static final class Result {
        public final File file;
        public final int total;
        public final boolean duplicated;
        public Result(File file, int total, boolean duplicated) {
            this.file = file;
            this.total = total;
            this.duplicated = duplicated;
        }
    }

    public static Result export(List<Question> questions, boolean duplicate, File outFile)
            throws Exception {
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder((int) PAGE_W, (int) PAGE_H, 1)
                .create();
        PdfDocument.Page page = doc.startPage(info);
        Canvas canvas = page.getCanvas();

        float cellW = (PAGE_W - 2 * MARGIN) / COLS;
        float cellH = (PAGE_H - 2 * MARGIN) / ROWS;

        drawGrid(canvas, cellW, cellH);

        // 题目排版
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextSize(cellW * 0.18f);

        Paint rulePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rulePaint.setColor(Color.BLACK);
        rulePaint.setStrokeWidth(1.2f);

        int n = questions.size();
        boolean duplicated = duplicate && n <= 20;

        for (int i = 0; i < n; i++) {
            // 上半部分格子（前20格）
            int topIndex = i;
            drawQuestion(canvas, questions.get(i), topIndex, cellW, cellH, textPaint, rulePaint);
            // 一式两份：复制到下半部分（从第5行开始）
            if (duplicated) {
                int bottomIndex = i + 20;
                drawQuestion(canvas, questions.get(i), bottomIndex, cellW, cellH, textPaint, rulePaint);
            }
        }

        doc.finishPage(page);

        File dir = outFile.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            doc.writeTo(fos);
        } finally {
            doc.close();
        }
        return new Result(outFile, n, duplicated);
    }

    private static void drawGrid(Canvas canvas, float cellW, float cellH) {
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(Color.parseColor("#CFD6E4"));
        line.setStrokeWidth(1f);
        for (int r = 0; r <= ROWS; r++) {
            float y = MARGIN + r * cellH;
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, line);
        }
        for (int c = 0; c <= COLS; c++) {
            float x = MARGIN + c * cellW;
            canvas.drawLine(x, MARGIN, x, PAGE_H - MARGIN, line);
        }
    }

    private static void drawQuestion(Canvas canvas, Question q, int index,
                                     float cellW, float cellH,
                                     Paint textPaint, Paint rulePaint) {
        int row = index / COLS;
        int col = index % COLS;
        float left = MARGIN + col * cellW;
        float top = MARGIN + row * cellH;
        // 格子内 1/4 高度基准线
        float anchorY = top + cellH * 0.25f;
        float centerX = left + cellW / 2f;

        if (!q.vertical) {
            String s = q.expr;
            // 横式较长时自动缩小字号
            float size = textPaint.getTextSize();
            if (textPaint.measureText(s) > cellW * 0.92f) {
                textPaint.setTextSize(size * (cellW * 0.92f / textPaint.measureText(s)));
            }
            float w = textPaint.measureText(s);
            canvas.drawText(s, centerX - w / 2f, anchorY, textPaint);
            textPaint.setTextSize(size);
        } else {
            // 竖式：两行 + 下划线，整体垂直居中于 1/4 高度附近
            String topS = q.vTop;
            String botS = q.vBottom;
            float lh = textPaint.getFontSpacing();
            float blockH = lh * 2f + 10f;
            float blockTop = anchorY - blockH / 2f;
            float topBaseline = blockTop + lh * 0.8f;
            float botBaseline = blockTop + lh + lh * 0.8f;
            float ruleY = botBaseline + 6f;

            // 两行右对齐到算子宽度，整体相对格子居中
            float opW = textPaint.measureText(botS);
            float topW = textPaint.measureText(topS);
            float w = Math.max(opW, topW);
            float rightX = centerX + w / 2f;

            canvas.drawText(topS, rightX - topW, topBaseline, textPaint);
            canvas.drawText(botS, rightX - opW, botBaseline, textPaint);
            canvas.drawLine(rightX - w - 6f, ruleY, rightX + 6f, ruleY, rulePaint);
        }
    }
}
