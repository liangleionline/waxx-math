package com.lianglei.waxxmath.util;

/**
 * 一道已生成的题目。
 * 统一用 prefix + placeholder + suffix 描述题面，答案页把 placeholder 换成 answer。
 * 例：计算题 prefix="35 + 63 = "，placeholder=""，answer="98"
 *     比大小 prefix="5 + 2 "，placeholder="○"，suffix=" 8"，answer="＞"
 *     填括号 prefix="", placeholder="(    )", suffix=" + 6 = 9", answer="3"
 */
public class Question {

    public enum Kind { CALC, COMPARE, FILL, PATTERN }

    public final Kind kind;
    /** 答案前文本 */
    public final String prefix;
    /** 题目页占位符（计算题为空串） */
    public final String placeholder;
    /** 答案后文本 */
    public final String suffix;
    /** 标准答案 */
    public final String answer;

    /** 竖式上行（可为 null） */
    public final String vTop;
    /** 竖式下行，如 "+ 63"（可为 null） */
    public final String vBottom;
    /** 是否支持竖式（二元加减乘除类才支持） */
    public final boolean verticalSupported;
    /** 最终展示是否为竖式 */
    public boolean vertical;

    public Question(Kind kind, String prefix, String placeholder, String suffix,
                    String answer, String vTop, String vBottom, boolean verticalSupported) {
        this.kind = kind;
        this.prefix = prefix;
        this.placeholder = placeholder;
        this.suffix = suffix;
        this.answer = answer;
        this.vTop = vTop;
        this.vBottom = vBottom;
        this.verticalSupported = verticalSupported;
    }

    /** 题目页完整文本 */
    public String displayText() {
        return prefix + placeholder + suffix;
    }

    /** 去重键：题面 + 答案 */
    public String key() {
        return prefix + "|" + placeholder + "|" + suffix + "|" + answer;
    }
}
