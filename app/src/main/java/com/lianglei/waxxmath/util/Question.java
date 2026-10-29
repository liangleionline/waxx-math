package com.lianglei.waxxmath.util;

/** 一道已生成的题目：横式表达式 + 可选竖式两行。 */
public class Question {
    /** 横式文本，如 "35 + 63 =" */
    public final String expr;
    /** 竖式上行（可为 null） */
    public final String vTop;
    /** 竖式下行，如 "+ 63"（可为 null） */
    public final String vBottom;
    /** 是否支持竖式（二元加减乘除类才支持） */
    public final boolean verticalSupported;
    /** 最终展示是否为竖式 */
    public boolean vertical;

    public Question(String expr, String vTop, String vBottom, boolean verticalSupported) {
        this.expr = expr;
        this.vTop = vTop;
        this.vBottom = vBottom;
        this.verticalSupported = verticalSupported;
    }
}
