package com.lianglei.waxxmath.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 试卷排版引擎（纯 Java，不依赖 Android）。
 * 把各题型题目按「大题标题 + 每行 4 题（找规律每行 2 题）」排成试卷页，
 * 题目页与答案页共用同一份布局，保证页数严格一一对应。
 */
public class PaperLayout {

    public static final float PAGE_W = 595f;   // A4 宽(pt)
    public static final float PAGE_H = 842f;   // A4 高(pt)
    public static final float MARGIN_L = 24f;  // 左右边距尽量小，内容铺开
    public static final float MARGIN_R = 24f;
    public static final float CONTENT_W = PAGE_W - MARGIN_L - MARGIN_R;

    /** 顶部表头区域（姓名/日期/用时/得分）高度 */
    public static final float HEADER_H = 62f;
    public static final float CONTENT_TOP = HEADER_H + 12f;
    public static final float CONTENT_BOTTOM = PAGE_H - 32f;
    public static final float CONTENT_H = CONTENT_BOTTOM - CONTENT_TOP;

    // 字号调大后行高同步放大，保证题与题之间留白、不挤成一团
    public static final float TITLE_H = 32f;      // 大题标题行高
    public static final float ROW_TEXT_H = 42f;   // 横式/比大小/填括号行高
    public static final float ROW_VERT_H = 76f;   // 竖式行高（含答案位置）
    public static final float ROW_PATTERN_H = 46f;// 找规律行高

    private static final String[] CN_NUM = {
            "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十"
    };

    /** 一个题型分区（一个大题） */
    public static class Section {
        public final String title;
        public final Question.Kind kind;
        public final List<Question> questions;

        public Section(String title, Question.Kind kind, List<Question> questions) {
            this.title = title;
            this.kind = kind;
            this.questions = questions;
        }
    }

    /** 试卷上的一行：标题行或题目行 */
    public static class Row {
        public final String title;
        public final List<Question> items;
        public final int cols;
        public final float height;

        private Row(String title, List<Question> items, int cols, float height) {
            this.title = title;
            this.items = items;
            this.cols = cols;
            this.height = height;
        }

        public boolean isTitle() { return title != null; }
    }

    /** 一页试卷 */
    public static class PageData {
        public final List<Row> rows = new ArrayList<>();
        public float used;
    }

    public static int colsFor(Question.Kind kind) {
        return kind == Question.Kind.PATTERN ? 2 : 4;
    }

    public static float rowHeight(Question.Kind kind, List<Question> items) {
        if (kind == Question.Kind.PATTERN) return ROW_PATTERN_H;
        if (kind == Question.Kind.CALC) {
            for (Question q : items) if (q.vertical) return ROW_VERT_H;
            return ROW_TEXT_H;
        }
        return ROW_TEXT_H;
    }

    /** 按分区顺序排版并分页 */
    public static List<PageData> build(List<Section> sections) {
        List<PageData> pages = new ArrayList<>();
        PageData cur = new PageData();

        int sectionNo = 0;
        for (Section s : sections) {
            List<Question> qs = s.questions;
            if (qs == null || qs.isEmpty()) continue;
            sectionNo++;
            String num = sectionNo <= CN_NUM.length
                    ? CN_NUM[sectionNo - 1] : String.valueOf(sectionNo);
            String heading = num + "、" + s.title;
            int cols = colsFor(s.kind);
            int idx = 0;
            boolean first = true;

            while (idx < qs.size()) {
                String title = first ? heading : s.title + "（续）";
                List<Question> firstRow = qs.subList(idx, Math.min(idx + cols, qs.size()));
                float firstRowH = rowHeight(s.kind, firstRow);

                // 标题不能与首行分离
                if (cur.used > 0 && cur.used + TITLE_H + firstRowH > CONTENT_H) {
                    pages.add(cur);
                    cur = new PageData();
                }

                cur.rows.add(new Row(title, null, 0, TITLE_H));
                cur.used += TITLE_H;
                first = false;

                while (idx < qs.size()) {
                    List<Question> row = qs.subList(idx, Math.min(idx + cols, qs.size()));
                    float h = rowHeight(s.kind, row);
                    if (cur.used + h > CONTENT_H) break;
                    cur.rows.add(new Row(null, new ArrayList<>(row), cols, h));
                    cur.used += h;
                    idx += cols;
                }

                if (idx < qs.size()) {
                    pages.add(cur);
                    cur = new PageData();
                }
            }
        }

        if (!cur.rows.isEmpty()) pages.add(cur);
        return pages;
    }
}
