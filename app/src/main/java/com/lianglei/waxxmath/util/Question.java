package com.lianglei.waxxmath.util;

/** 一道数学题及其展示形式。 */
public class Question {
    public enum Type { ADD, SUB, MUL, DIV }
    public enum Format { HORIZONTAL, VERTICAL }

    public final Type type;
    public final int a;
    public final int b;
    public final int result;
    public Format format;

    public Question(Type type, int a, int b, int result) {
        this.type = type;
        this.a = a;
        this.b = b;
        this.result = result;
    }

    /** 唯一性判重键。 */
    public String key() {
        return type.name() + ":" + a + ":" + b;
    }

    /** 横式文本，如 "3 + 5 ="。 */
    public String horizontal() {
        switch (type) {
            case ADD: return a + " + " + b + " =";
            case SUB: return a + " - " + b + " =";
            case MUL: return a + " × " + b + " =";
            case DIV: return a + " ÷ " + b + " =";
        }
        return "";
    }

    /** 竖式第一行（被除数/被减数/被乘数/被加数）。 */
    public String verticalTop() {
        return String.valueOf(a);
    }

    /** 竖式第二行，如 "+ 5"。 */
    public String verticalBottom() {
        String op;
        switch (type) {
            case ADD: op = "+"; break;
            case SUB: op = "-"; break;
            case MUL: op = "×"; break;
            default:  op = "÷"; break;
        }
        return op + " " + b;
    }
}
