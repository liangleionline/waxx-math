package com.lianglei.waxxmath.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 题型目录：按年级/学期列出全部题型，并为每个题型实现独立的随机出题器。
 * 竖式仅对二元加减乘除类题型开放；连加连减、分数、小数、混合运算一律横式。
 * 比大小、填括号、找规律为一页两行/四行卷面题型，永远横式。
 */
public class TopicCatalog {

    public static class Topic {
        public final String id;
        public final String title;
        public final String example;
        public final Question.Kind kind;
        public final boolean verticalSupported;
        public final Gen gen;

        public interface Gen { Question generate(Random r); }

        Topic(String id, String title, String example, Question.Kind kind,
              boolean vs, Gen gen) {
            this.id = id; this.title = title; this.example = example;
            this.kind = kind; this.verticalSupported = vs; this.gen = gen;
        }
    }

    // ---------- 工具 ----------
    private static int ri(Random r, int lo, int hi) {
        if (hi < lo) hi = lo;
        return lo + r.nextInt(hi - lo + 1);
    }

    /** 二元计算题 */
    private static Question bin(int a, String op, int b, boolean vs) {
        int ans = op.equals("+") ? a + b : op.equals("-") ? a - b
                : op.equals("×") ? a * b : a / b;
        return new Question(Question.Kind.CALC,
                a + " " + op + " " + b + " = ", "", "", String.valueOf(ans),
                String.valueOf(a), op + " " + b, vs);
    }

    /** 多元计算题（横式） */
    private static Question calc(String expr, long ans, boolean vs) {
        return new Question(Question.Kind.CALC, expr + " = ", "", "",
                String.valueOf(ans), null, null, vs);
    }

    /** 分数答案：约分后输出，整数直接输出 */
    private static String frac(int num, int den) {
        if (num == 0) return "0";
        if (num == den) return "1";
        int a = Math.abs(num), b = den;
        while (b != 0) { int t = a % b; a = b; b = t; }
        int g = a;
        return (num / g) + "/" + (den / g);
    }

    /** 比大小题 */
    private static Question compare(String left, int lv, String right, int rv) {
        String ans = lv > rv ? "＞" : lv < rv ? "＜" : "＝";
        return new Question(Question.Kind.COMPARE, left + " ", "○", " " + right, ans,
                null, null, false);
    }

    /** 填括号题 */
    private static Question fill(String pre, String suf, int ans) {
        return new Question(Question.Kind.FILL, pre, "(      )", suf, String.valueOf(ans),
                null, null, false);
    }

    /** 找规律题：5 项数列，空一格 */
    private static Question pattern(int[] seq, int blank) {
        StringBuilder pre = new StringBuilder();
        StringBuilder suf = new StringBuilder();
        for (int i = 0; i < seq.length; i++) {
            if (i == blank) continue;
            String s = String.valueOf(seq[i]);
            if (i < blank) {
                if (pre.length() > 0) pre.append("、");
                pre.append(s);
            } else {
                if (suf.length() == 0) suf.append("、");
                else suf.append("、");
                suf.append(s);
            }
        }
        pre.append("、");
        return new Question(Question.Kind.PATTERN, pre.toString(), "（　　　）", suf.toString(),
                String.valueOf(seq[blank]), null, null, false);
    }

    // ---------- 一年级 上学期 ----------
    private static final Topic G1U_T1 = new Topic("1u_addsub10", "10以内加减法", "例题：2+5=",
            Question.Kind.CALC, true, r -> {
        if (r.nextBoolean()) {
            int s = ri(r, 2, 10); int a = ri(r, 0, s); int b = s - a;
            return bin(a, "+", b, true);
        }
        int a = ri(r, 1, 10); int b = ri(r, 0, a);
        return bin(a, "-", b, true);
    });
    private static final Topic G1U_T2 = new Topic("1u_chain10", "10以内的连加连减(不进位)", "例题：2+1+4=",
            Question.Kind.CALC, false, r -> {
        if (r.nextBoolean()) {
            int x = ri(r, 1, 8); int y = ri(r, 0, 10 - x); int z = ri(r, 0, 10 - x - y);
            return calc(x + " + " + y + " + " + z, x + y + z, false);
        }
        int x = ri(r, 3, 10); int a = ri(r, 0, x); int b = ri(r, 0, x - a);
        return calc(x + " - " + a + " - " + b, x - a - b, false);
    });
    private static final Topic G1U_T3 = new Topic("1u_mix10", "10以内的加减混合(不进位)", "例题：5-3+2=",
            Question.Kind.CALC, false, r -> {
        if (r.nextBoolean()) {
            int x = ri(r, 1, 8); int y = ri(r, 0, 10 - x); int z = ri(r, 0, x + y);
            return calc(x + " + " + y + " - " + z, x + y - z, false);
        }
        int x = ri(r, 2, 10); int y = ri(r, 0, x); int z = ri(r, 0, 10 - x + y);
        return calc(x + " - " + y + " + " + z, x - y + z, false);
    });
    private static final Topic G1U_T4 = new Topic("1u_teenadd", "十几加几(不进位)", "例题：12+1=",
            Question.Kind.CALC, true, r -> {
        int a = ri(r, 11, 19); int b = ri(r, 0, 9 - (a % 10));
        return bin(a, "+", b, true);
    });
    private static final Topic G1U_T5 = new Topic("1u_teensub", "十几减几(不退位)", "例题：15-3=",
            Question.Kind.CALC, true, r -> {
        int a = ri(r, 11, 19); int b = ri(r, 0, a % 10);
        return bin(a, "-", b, true);
    });
    private static final Topic G1U_T6 = new Topic("1u_add20carry", "20以内加法(带进位)", "例题：7+8=",
            Question.Kind.CALC, true, r -> {
        int a, b;
        do { a = ri(r, 2, 9); b = ri(r, 2, 9); } while (a + b < 10 || a + b > 18);
        return bin(a, "+", b, true);
    });
    private static final Topic G1U_CMP = new Topic("1u_compare10", "比大小(10以内)", "例题：3+2 ○ 6",
            Question.Kind.COMPARE, false, r -> {
        int t = ri(r, 0, 3);
        if (t == 0) {
            int a = ri(r, 0, 10); int b = ri(r, 0, 10);
            return compare(String.valueOf(a), a, String.valueOf(b), b);
        } else if (t == 1) {
            int a = ri(r, 0, 6); int b = ri(r, 0, 10 - a);
            int c = ri(r, 0, 10);
            return compare(a + " + " + b, a + b, String.valueOf(c), c);
        } else if (t == 2) {
            int a = ri(r, 2, 10); int b = ri(r, 0, a);
            int c = ri(r, 0, 10);
            return compare(a + " - " + b, a - b, String.valueOf(c), c);
        }
        // 算式 vs 算式
        int a = ri(r, 0, 5); int b = ri(r, 0, 10 - a);
        int c = ri(r, 0, 5); int d = ri(r, 0, 10 - c);
        return compare(a + " + " + b, a + b, c + " + " + d, c + d);
    });
    private static final Topic G1U_FILL = new Topic("1u_fill10", "填括号(10以内)", "例题：( )+6=9",
            Question.Kind.FILL, false, r -> {
        int t = ri(r, 0, 3);
        if (t == 0) { // ( )+b=c
            int a = ri(r, 0, 10); int b = ri(r, 0, 10 - a);
            return fill("", " + " + b + " = " + (a + b), a);
        } else if (t == 1) { // a+( )=c
            int a = ri(r, 0, 10); int b = ri(r, 0, 10 - a);
            return fill(a + " + ", " = " + (a + b), b);
        } else if (t == 2) { // a-( )=c
            int a = ri(r, 1, 10); int b = ri(r, 0, a);
            return fill(a + " - ", " = " + (a - b), b);
        }
        // ( )-b=c
        int b = ri(r, 0, 10); int c = ri(r, 0, 10 - b);
        return fill("", " - " + b + " = " + c, b + c);
    });
    private static final Topic G1U_PAT = new Topic("1u_pattern10", "找规律填数(10以内)", "例题：2、4、6、__、10",
            Question.Kind.PATTERN, false, r -> {
        int step = ri(r, 1, 3);
        int blank = ri(r, 2, 4);
        int[] seq = makeSeq(r, step, 10, false);
        return pattern(seq, blank);
    });

    // ---------- 一年级 下学期 ----------
    private static final Topic G1D_T1 = new Topic("1d_sub20borrow", "20以内减法(带退位)", "例题：16-8=",
            Question.Kind.CALC, true, r -> {
        int a = ri(r, 11, 18); int ones = a % 10; int b = ri(r, ones + 1, Math.min(9, a));
        return bin(a, "-", b, true);
    });
    private static final Topic G1D_T2 = new Topic("1d_2dplus1carry", "两位数加一位数(进位)", "例题：35+6=",
            Question.Kind.CALC, true, r -> {
        int tens = ri(r, 1, 8); int ones = ri(r, 1, 8); int b = ri(r, 10 - ones, 9);
        return bin(tens * 10 + ones, "+", b, true);
    });
    private static final Topic G1D_T3 = new Topic("1d_2dminus1borrow", "两位数减一位数(退位)", "例题：32-5=",
            Question.Kind.CALC, true, r -> {
        int tens = ri(r, 2, 9); int ones = ri(r, 0, 8); int b = ri(r, ones + 1, 9);
        return bin(tens * 10 + ones, "-", b, true);
    });
    private static final Topic G1D_CMP = new Topic("1d_compare20", "比大小(20以内)", "例题：8+9 ○ 15",
            Question.Kind.COMPARE, false, r -> {
        int t = ri(r, 0, 2);
        if (t == 0) {
            int a = ri(r, 0, 20); int b = ri(r, 0, 20);
            return compare(String.valueOf(a), a, String.valueOf(b), b);
        } else if (t == 1) {
            int a = ri(r, 5, 14); int b = ri(r, 1, 20 - a);
            int c = ri(r, 0, 20);
            return compare(a + " + " + b, a + b, String.valueOf(c), c);
        }
        int a = ri(r, 10, 20); int b = ri(r, 0, 9);
        int c = ri(r, 0, 20);
        return compare(a + " - " + b, a - b, String.valueOf(c), c);
    });
    private static final Topic G1D_FILL = new Topic("1d_fill20", "填括号(20以内)", "例题：( )+8=15",
            Question.Kind.FILL, false, r -> {
        int t = ri(r, 0, 2);
        if (t == 0) {
            int a = ri(r, 0, 20); int b = ri(r, 0, 20 - a);
            return fill("", " + " + b + " = " + (a + b), a);
        } else if (t == 1) {
            int a = ri(r, 0, 20); int b = ri(r, 0, 20 - a);
            return fill(a + " + ", " = " + (a + b), b);
        }
        int a = ri(r, 1, 20); int b = ri(r, 0, a);
        return fill(a + " - ", " = " + (a - b), b);
    });
    private static final Topic G1D_PAT = new Topic("1d_pattern20", "找规律填数(20以内)", "例题：3、6、9、__、15",
            Question.Kind.PATTERN, false, r -> {
        int step = ri(r, 1, 4);
        int blank = ri(r, 2, 4);
        int[] seq = makeSeq(r, step, 18, r.nextBoolean());
        return pattern(seq, blank);
    });

    /** 生成等差数列，空位由调用方指定 */
    private static int[] makeSeq(Random r, int step, int max, boolean desc) {
        int[] seq = new int[5];
        if (desc) {
            int start = ri(r, step * 4, Math.min(max, step * 6));
            for (int i = 0; i < 5; i++) seq[i] = start - i * step;
        } else {
            int start = ri(r, 0, max - step * 4);
            for (int i = 0; i < 5; i++) seq[i] = start + i * step;
        }
        return seq;
    }

    // ---------- 二年级 上学期 ----------
    private static final Topic G2U_T1 = new Topic("2u_add100nocarry", "100以内加法(不进位)", "例题：35+63=",
            Question.Kind.CALC, true, r -> {
        int at = ri(r, 1, 8); int bt = ri(r, 1, 9 - at);
        int ao = ri(r, 0, 8); int bo = ri(r, 0, 9 - ao);
        return bin(at * 10 + ao, "+", bt * 10 + bo, true);
    });
    private static final Topic G2U_T2 = new Topic("2u_add100carry", "100以内加法(进位)", "例题：67+33=",
            Question.Kind.CALC, true, r -> {
        int at = ri(r, 1, 7); int bt = ri(r, 1, 8 - at);
        int ao = ri(r, 2, 9); int bo = ri(r, 10 - ao, 9);
        return bin(at * 10 + ao, "+", bt * 10 + bo, true);
    });
    private static final Topic G2U_T3 = new Topic("2u_sub100noborrow", "100以内减法(不退位)", "例题：67-32=",
            Question.Kind.CALC, true, r -> {
        int at = ri(r, 2, 9); int ao = ri(r, 0, 9);
        int bt = ri(r, 1, at); int bo = ri(r, 0, ao);
        int b = bt * 10 + bo; int a = at * 10 + ao;
        if (b > a) { int t = a; a = b; b = t; }
        return bin(a, "-", b, true);
    });
    private static final Topic G2U_T4 = new Topic("2u_sub100borrow", "100以内减法(退位)", "例题：54-25=",
            Question.Kind.CALC, true, r -> {
        int at = ri(r, 2, 9); int ao = ri(r, 0, 7);
        int bt = ri(r, 1, at - 1); int bo = ri(r, ao + 1, 9);
        return bin(at * 10 + ao, "-", bt * 10 + bo, true);
    });
    private static final Topic G2U_T5 = new Topic("2u_addchain100", "100以内连加", "例题：23+34+19=",
            Question.Kind.CALC, false, r -> {
        int x = ri(r, 10, 50); int y = ri(r, 10, 90 - x); int z = ri(r, 5, 95 - x - y);
        return calc(x + " + " + y + " + " + z, x + y + z, false);
    });
    private static final Topic G2U_T6 = new Topic("2u_subchain100", "100以内连减", "例题：87-16-26=",
            Question.Kind.CALC, false, r -> {
        int x = ri(r, 50, 100); int a = ri(r, 10, x - 30); int b = ri(r, 10, x - a - 5);
        return calc(x + " - " + a + " - " + b, x - a - b, false);
    });
    private static final Topic G2U_T7 = new Topic("2u_mix100", "100以内加减混合", "例题：13+26-18=",
            Question.Kind.CALC, false, r -> {
        if (r.nextBoolean()) {
            int x = ri(r, 20, 70); int y = ri(r, 10, 95 - x); int z = ri(r, 10, x + y - 5);
            return calc(x + " + " + y + " - " + z, x + y - z, false);
        }
        int x = ri(r, 30, 90); int y = ri(r, 10, x - 10); int z = ri(r, 10, 95 - (x - y));
        return calc(x + " - " + y + " + " + z, x - y + z, false);
    });
    private static final Topic G2U_T8 = new Topic("2u_tablemul", "表内乘法(九九乘法表)", "例题：8×9=",
            Question.Kind.CALC, true, r -> {
        int a = ri(r, 1, 9); int b = ri(r, 1, 9);
        return bin(a, "×", b, true);
    });

    // ---------- 二年级 下学期 ----------
    private static final Topic G2D_T1 = new Topic("2d_divExact", "除法(可除尽)", "例题：56÷8=",
            Question.Kind.CALC, true, r -> {
        int d = ri(r, 1, 9); int q = ri(r, 1, 9);
        return bin(d * q, "÷", d, true);
    });
    private static final Topic G2D_T2 = new Topic("2d_mix4ops", "加减乘除混合运算", "例题：5×8÷4=",
            Question.Kind.CALC, false, r -> {
        int a = ri(r, 2, 9); int b = ri(r, 2, 9); int p = a * b;
        List<Integer> divs = new ArrayList<>();
        for (int c = 2; c <= p; c++) if (p % c == 0) divs.add(c);
        int c = divs.get(r.nextInt(divs.size()));
        return calc(a + " × " + b + " ÷ " + c, p / c, false);
    });
    private static final Topic G2D_T3 = new Topic("2d_mix4opsParen", "加减乘除混合运算(带括号)", "例题：5×(8÷4)=",
            Question.Kind.CALC, false, r -> {
        int b = ri(r, 2, 9); int q = ri(r, 2, 9); int a = b * q; int c = ri(r, 2, 9);
        return calc("(" + a + " ÷ " + b + ") × " + c, q * c, false);
    });

    // ---------- 三年级 上学期 ----------
    private static final Topic G3U_T1 = new Topic("3u_2daddsub", "两位数加减", "例题：37+42=",
            Question.Kind.CALC, true, r -> {
        int a = ri(r, 10, 99); int b = ri(r, 10, 99);
        if (b > a && r.nextBoolean()) { int t = a; a = b; b = t; }
        return bin(a, r.nextBoolean() ? "+" : "-", b, true);
    });
    private static final Topic G3U_T2 = new Topic("3u_3daddsub", "三位数加减", "例题：325-127=",
            Question.Kind.CALC, true, r -> {
        int a = ri(r, 100, 999); int b = ri(r, 100, 999);
        if (b > a) { int t = a; a = b; b = t; }
        return bin(a, r.nextBoolean() ? "+" : "-", b, true);
    });
    private static final Topic G3U_T3 = new Topic("3u_fracSimple", "分数的简单计算", "例题：1/6+3/6=",
            Question.Kind.CALC, false, r -> {
        int den = ri(r, 3, 9); int n1 = ri(r, 1, den - 1);
        if (r.nextBoolean()) {
            int n2 = ri(r, 0, den - n1);
            return new Question(Question.Kind.CALC,
                    n1 + "/" + den + " + " + n2 + "/" + den + " = ", "", "",
                    frac(n1 + n2, den), null, null, false);
        }
        int n2 = ri(r, 0, n1);
        return new Question(Question.Kind.CALC,
                n1 + "/" + den + " - " + n2 + "/" + den + " = ", "", "",
                frac(n1 - n2, den), null, null, false);
    });
    private static final Topic G3U_T4 = new Topic("3u_2d×1d", "两位数乘一位数", "例题：23×3=",
            Question.Kind.CALC, true, r -> {
        return bin(ri(r, 12, 99), "×", ri(r, 2, 9), true);
    });

    // ---------- 三年级 下学期 ----------
    private static final Topic G3D_T1 = new Topic("3d_3dDiv", "三位数除法", "例题：128÷64=",
            Question.Kind.CALC, true, r -> {
        int d = ri(r, 12, 49); int q = ri(r, 3, 20); int n = d * q;
        while (n < 100 || n > 999) { d = ri(r, 12, 49); q = ri(r, 3, 20); n = d * q; }
        return bin(n, "÷", d, true);
    });
    private static final Topic G3D_T2 = new Topic("3d_2d×2d", "两位数乘法", "例题：25×18=",
            Question.Kind.CALC, true, r -> {
        return bin(ri(r, 12, 49), "×", ri(r, 11, 29), true);
    });
    private static final Topic G3D_T3 = new Topic("3d_dec1", "小数加减法(1位小数)", "例题：6.5+2.5=",
            Question.Kind.CALC, false, r -> {
        int x = ri(r, 5, 95); int y = ri(r, 5, 95);
        boolean sub = r.nextBoolean();
        if (sub && y > x) { int t = x; x = y; y = t; }
        String ax = String.format(Locale.US, "%.1f", x / 10.0);
        String by = String.format(Locale.US, "%.1f", y / 10.0);
        double ans = sub ? (x - y) / 10.0 : (x + y) / 10.0;
        return new Question(Question.Kind.CALC,
                ax + (sub ? " - " : " + ") + by + " = ", "", "",
                String.format(Locale.US, "%.1f", ans), null, null, false);
    });

    // ---------- 四年级 全学期 ----------
    private static final Topic G4_T1 = new Topic("4_dec2", "小数加减法(2位小数)", "例题：7.65+4.35=",
            Question.Kind.CALC, false, r -> {
        int x = ri(r, 50, 999); int y = ri(r, 50, 999);
        boolean sub = r.nextBoolean();
        if (sub && y > x) { int t = x; x = y; y = t; }
        String ax = String.format(Locale.US, "%.2f", x / 100.0);
        String by = String.format(Locale.US, "%.2f", y / 100.0);
        double ans = sub ? (x - y) / 100.0 : (x + y) / 100.0;
        return new Question(Question.Kind.CALC,
                ax + (sub ? " - " : " + ") + by + " = ", "", "",
                String.format(Locale.US, "%.2f", ans), null, null, false);
    });
    private static final Topic G4_T2 = new Topic("4_fourOps", "四则混合运算", "例题：(15+25)×40÷10=",
            Question.Kind.CALC, false, r -> {
        int a = ri(r, 5, 25); int b = ri(r, 5, 25); int s = a + b;
        int c = ri(r, 2, 9); int p = s * c;
        List<Integer> divs = new ArrayList<>();
        for (int d = 2; d <= 9; d++) if (p % d == 0) divs.add(d);
        int d = divs.get(r.nextInt(divs.size()));
        return calc("(" + a + " + " + b + ") × " + c + " ÷ " + d, p / d, false);
    });

    // ---------- 五年级 全学期 ----------
    private static final Topic G5_T1 = new Topic("5_fracDen1", "分数加减法(分母为个位数)", "例题：6/8+7/9=",
            Question.Kind.CALC, false, r -> {
        int d1 = ri(r, 2, 9); int d2 = ri(r, 2, 9);
        int n1 = ri(r, 1, d1 - 1); int n2 = ri(r, 1, d2 - 1);
        boolean sub = r.nextBoolean();
        if (sub) {
            // 保证结果非负
            if (n1 * d2 < n2 * d1) { int t = n1; n1 = n2; n2 = t; t = d1; d1 = d2; d2 = t; }
            int num = n1 * d2 - n2 * d1;
            return new Question(Question.Kind.CALC,
                    n1 + "/" + d1 + " - " + n2 + "/" + d2 + " = ", "", "",
                    frac(num, d1 * d2), null, null, false);
        }
        int num = n1 * d2 + n2 * d1;
        return new Question(Question.Kind.CALC,
                n1 + "/" + d1 + " + " + n2 + "/" + d2 + " = ", "", "",
                frac(num, d1 * d2), null, null, false);
    });

    // ---------- 年级/学期映射 ----------
    public static List<Topic> topicsFor(int grade, boolean spring) {
        // grade: 1..5；spring=true 下学期，false 上学期；4/5 年级忽略学期
        switch (grade) {
            case 1: return spring
                    ? list(G1D_T1, G1D_T2, G1D_T3, G1D_CMP, G1D_FILL, G1D_PAT)
                    : list(G1U_T1, G1U_T2, G1U_T3, G1U_T4, G1U_T5, G1U_T6, G1U_CMP, G1U_FILL, G1U_PAT);
            case 2: return spring ? list(G2D_T1, G2D_T2, G2D_T3)
                                 : list(G2U_T1, G2U_T2, G2U_T3, G2U_T4, G2U_T5, G2U_T6, G2U_T7, G2U_T8);
            case 3: return spring ? list(G3D_T1, G3D_T2, G3D_T3)
                                 : list(G3U_T1, G3U_T2, G3U_T3, G3U_T4);
            case 4: return list(G4_T1, G4_T2);
            case 5: return list(G5_T1);
            default: return list();
        }
    }

    private static List<Topic> list(Topic... t) {
        List<Topic> l = new ArrayList<>();
        for (Topic x : t) l.add(x);
        return l;
    }

    public static final String[] GRADE_NAMES = {"一年级", "二年级", "三年级", "四年级", "五年级"};
    public static boolean hasSpring(int grade) { return grade <= 3; }
}
