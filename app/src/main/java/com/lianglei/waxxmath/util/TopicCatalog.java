package com.lianglei.waxxmath.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 题型目录：按年级/学期列出全部题型，并为每个题型实现独立的随机出题器。
 * 竖式仅对二元加减乘除类题型开放；连加连减、分数、小数、混合运算一律横式。
 */
public class TopicCatalog {

    public static class Topic {
        public final String id;
        public final String title;
        public final String example;
        public final boolean verticalSupported;
        public final Gen gen;

        public interface Gen { Question generate(Random r); }

        Topic(String id, String title, String example, boolean vs, Gen gen) {
            this.id = id; this.title = title; this.example = example;
            this.verticalSupported = vs; this.gen = gen;
        }
    }

    // ---------- 工具 ----------
    private static int ri(Random r, int lo, int hi) {
        if (hi < lo) hi = lo;
        return lo + r.nextInt(hi - lo + 1);
    }
    private static Question bin(int a, String op, int b, boolean vs) {
        return new Question(a + " " + op + " " + b + " =",
                String.valueOf(a), op + " " + b, vs);
    }

    // ---------- 一年级 上学期 ----------
    private static final Topic G1U_T1 = new Topic("1u_addsub10", "10以内加减法", "例题：2+5=", true, r -> {
        if (r.nextBoolean()) {
            int s = ri(r, 2, 10); int a = ri(r, 0, s); int b = s - a;
            return bin(a, "+", b, true);
        }
        int a = ri(r, 1, 10); int b = ri(r, 0, a);
        return bin(a, "-", b, true);
    });
    private static final Topic G1U_T2 = new Topic("1u_chain10", "10以内的连加连减(不进位)", "例题：2+1+4=", false, r -> {
        if (r.nextBoolean()) {
            int x = ri(r, 1, 8); int y = ri(r, 0, 10 - x); int z = ri(r, 0, 10 - x - y);
            return new Question(x + " + " + y + " + " + z + " =", null, null, false);
        }
        int x = ri(r, 3, 10); int a = ri(r, 0, x); int b = ri(r, 0, x - a);
        return new Question(x + " - " + a + " - " + b + " =", null, null, false);
    });
    private static final Topic G1U_T3 = new Topic("1u_mix10", "10以内的加减混合(不进位)", "例题：5-3+2=", false, r -> {
        if (r.nextBoolean()) {
            int x = ri(r, 1, 8); int y = ri(r, 0, 10 - x); int z = ri(r, 0, x + y);
            return new Question(x + " + " + y + " - " + z + " =", null, null, false);
        }
        int x = ri(r, 2, 10); int y = ri(r, 0, x); int z = ri(r, 0, 10 - x + y);
        return new Question(x + " - " + y + " + " + z + " =", null, null, false);
    });
    private static final Topic G1U_T4 = new Topic("1u_teenadd", "十几加几(不进位)", "例题：12+1=", true, r -> {
        int a = ri(r, 11, 19); int b = ri(r, 0, 9 - (a % 10));
        return bin(a, "+", b, true);
    });
    private static final Topic G1U_T5 = new Topic("1u_teensub", "十几减几(不退位)", "例题：15-3=", true, r -> {
        int a = ri(r, 11, 19); int b = ri(r, 0, a % 10);
        return bin(a, "-", b, true);
    });
    private static final Topic G1U_T6 = new Topic("1u_add20carry", "20以内加法(带进位)", "例题：7+8=", true, r -> {
        int a, b;
        do { a = ri(r, 2, 9); b = ri(r, 2, 9); } while (a + b < 10 || a + b > 18);
        return bin(a, "+", b, true);
    });

    // ---------- 一年级 下学期 ----------
    private static final Topic G1D_T1 = new Topic("1d_sub20borrow", "20以内减法(带退位)", "例题：16-8=", true, r -> {
        int a = ri(r, 11, 18); int ones = a % 10; int b = ri(r, ones + 1, Math.min(9, a));
        return bin(a, "-", b, true);
    });
    private static final Topic G1D_T2 = new Topic("1d_2dplus1carry", "两位数加一位数(进位)", "例题：35+6=", true, r -> {
        int tens = ri(r, 1, 8); int ones = ri(r, 1, 8); int b = ri(r, 10 - ones, 9);
        return bin(tens * 10 + ones, "+", b, true);
    });
    private static final Topic G1D_T3 = new Topic("1d_2dminus1borrow", "两位数减一位数(退位)", "例题：32-5=", true, r -> {
        int tens = ri(r, 2, 9); int ones = ri(r, 0, 8); int b = ri(r, ones + 1, 9);
        return bin(tens * 10 + ones, "-", b, true);
    });

    // ---------- 二年级 上学期 ----------
    private static final Topic G2U_T1 = new Topic("2u_add100nocarry", "100以内加法(不进位)", "例题：35+63=", true, r -> {
        int at = ri(r, 1, 8); int bt = ri(r, 1, 9 - at);
        int ao = ri(r, 0, 8); int bo = ri(r, 0, 9 - ao);
        return bin(at * 10 + ao, "+", bt * 10 + bo, true);
    });
    private static final Topic G2U_T2 = new Topic("2u_add100carry", "100以内加法(进位)", "例题：67+33=", true, r -> {
        int at = ri(r, 1, 7); int bt = ri(r, 1, 8 - at);
        int ao = ri(r, 2, 9); int bo = ri(r, 10 - ao, 9);
        return bin(at * 10 + ao, "+", bt * 10 + bo, true);
    });
    private static final Topic G2U_T3 = new Topic("2u_sub100noborrow", "100以内减法(不退位)", "例题：67-32=", true, r -> {
        int at = ri(r, 2, 9); int ao = ri(r, 0, 9);
        int bt = ri(r, 1, at); int bo = ri(r, 0, ao);
        int b = bt * 10 + bo; int a = at * 10 + ao;
        if (b > a) { int t = a; a = b; b = t; }
        return bin(a, "-", b, true);
    });
    private static final Topic G2U_T4 = new Topic("2u_sub100borrow", "100以内减法(退位)", "例题：54-25=", true, r -> {
        int at = ri(r, 2, 9); int ao = ri(r, 0, 7);
        int bt = ri(r, 1, at - 1); int bo = ri(r, ao + 1, 9);
        return bin(at * 10 + ao, "-", bt * 10 + bo, true);
    });
    private static final Topic G2U_T5 = new Topic("2u_addchain100", "100以内连加", "例题：23+34+19=", false, r -> {
        int x = ri(r, 10, 50); int y = ri(r, 10, 90 - x); int z = ri(r, 5, 95 - x - y);
        return new Question(x + " + " + y + " + " + z + " =", null, null, false);
    });
    private static final Topic G2U_T6 = new Topic("2u_subchain100", "100以内连减", "例题：87-16-26=", false, r -> {
        int x = ri(r, 50, 100); int a = ri(r, 10, x - 30); int b = ri(r, 10, x - a - 5);
        return new Question(x + " - " + a + " - " + b + " =", null, null, false);
    });
    private static final Topic G2U_T7 = new Topic("2u_mix100", "100以内加减混合", "例题：13+26-18=", false, r -> {
        if (r.nextBoolean()) {
            int x = ri(r, 20, 70); int y = ri(r, 10, 95 - x); int z = ri(r, 10, x + y - 5);
            return new Question(x + " + " + y + " - " + z + " =", null, null, false);
        }
        int x = ri(r, 30, 90); int y = ri(r, 10, x - 10); int z = ri(r, 10, 95 - (x - y));
        return new Question(x + " - " + y + " + " + z + " =", null, null, false);
    });
    private static final Topic G2U_T8 = new Topic("2u_tablemul", "表内乘法(九九乘法表)", "例题：8×9=", true, r -> {
        int a = ri(r, 1, 9); int b = ri(r, 1, 9);
        return bin(a, "×", b, true);
    });

    // ---------- 二年级 下学期 ----------
    private static final Topic G2D_T1 = new Topic("2d_divExact", "除法(可除尽)", "例题：56÷8=", true, r -> {
        int d = ri(r, 1, 9); int q = ri(r, 1, 9);
        return bin(d * q, "÷", d, true);
    });
    private static final Topic G2D_T2 = new Topic("2d_mix4ops", "加减乘除混合运算", "例题：5×8÷4=", false, r -> {
        int a = ri(r, 2, 9); int b = ri(r, 2, 9); int p = a * b;
        List<Integer> divs = new ArrayList<>();
        for (int c = 2; c <= p; c++) if (p % c == 0) divs.add(c);
        int c = divs.get(r.nextInt(divs.size()));
        return new Question(a + " × " + b + " ÷ " + c + " =", null, null, false);
    });
    private static final Topic G2D_T3 = new Topic("2d_mix4opsParen", "加减乘除混合运算(带括号)", "例题：5×(8÷4)=", false, r -> {
        int b = ri(r, 2, 9); int q = ri(r, 2, 9); int a = b * q; int c = ri(r, 2, 9);
        return new Question("(" + a + " ÷ " + b + ") × " + c + " =", null, null, false);
    });

    // ---------- 三年级 上学期 ----------
    private static final Topic G3U_T1 = new Topic("3u_2daddsub", "两位数加减", "例题：37+42=", true, r -> {
        int a = ri(r, 10, 99); int b = ri(r, 10, 99);
        if (b > a && r.nextBoolean()) { int t = a; a = b; b = t; }
        return bin(a, r.nextBoolean() ? "+" : "-", b, true);
    });
    private static final Topic G3U_T2 = new Topic("3u_3daddsub", "三位数加减", "例题：325-127=", true, r -> {
        int a = ri(r, 100, 999); int b = ri(r, 100, 999);
        if (b > a) { int t = a; a = b; b = t; }
        return bin(a, r.nextBoolean() ? "+" : "-", b, true);
    });
    private static final Topic G3U_T3 = new Topic("3u_fracSimple", "分数的简单计算", "例题：1/6+3/6=", false, r -> {
        int den = ri(r, 3, 9); int n1 = ri(r, 1, den - 1);
        if (r.nextBoolean()) {
            int n2 = ri(r, 0, den - n1);
            return new Question(n1 + "/" + den + " + " + n2 + "/" + den + " =", null, null, false);
        }
        int n2 = ri(r, 0, n1);
        return new Question(n1 + "/" + den + " - " + n2 + "/" + den + " =", null, null, false);
    });
    private static final Topic G3U_T4 = new Topic("3u_2d×1d", "两位数乘一位数", "例题：23×3=", true, r -> {
        return bin(ri(r, 12, 99), "×", ri(r, 2, 9), true);
    });

    // ---------- 三年级 下学期 ----------
    private static final Topic G3D_T1 = new Topic("3d_3dDiv", "三位数除法", "例题：128÷64=", true, r -> {
        int d = ri(r, 12, 49); int q = ri(r, 3, 20); int n = d * q;
        while (n < 100 || n > 999) { d = ri(r, 12, 49); q = ri(r, 3, 20); n = d * q; }
        return bin(n, "÷", d, true);
    });
    private static final Topic G3D_T2 = new Topic("3d_2d×2d", "两位数乘法", "例题：25×18=", true, r -> {
        return bin(ri(r, 12, 49), "×", ri(r, 11, 29), true);
    });
    private static final Topic G3D_T3 = new Topic("3d_dec1", "小数加减法(1位小数)", "例题：6.5+2.5=", false, r -> {
        int x = ri(r, 5, 95); int y = ri(r, 5, 95);
        boolean sub = r.nextBoolean();
        if (sub && y > x) { int t = x; x = y; y = t; }
        String ax = String.format(java.util.Locale.US, "%.1f", x / 10.0);
        String by = String.format(java.util.Locale.US, "%.1f", y / 10.0);
        return new Question(ax + (sub ? " - " : " + ") + by + " =", null, null, false);
    });

    // ---------- 四年级 全学期 ----------
    private static final Topic G4_T1 = new Topic("4_dec2", "小数加减法(2位小数)", "例题：7.65+4.35=", false, r -> {
        int x = ri(r, 50, 999); int y = ri(r, 50, 999);
        boolean sub = r.nextBoolean();
        if (sub && y > x) { int t = x; x = y; y = t; }
        String ax = String.format(java.util.Locale.US, "%.2f", x / 100.0);
        String by = String.format(java.util.Locale.US, "%.2f", y / 100.0);
        return new Question(ax + (sub ? " - " : " + ") + by + " =", null, null, false);
    });
    private static final Topic G4_T2 = new Topic("4_fourOps", "四则混合运算", "例题：(15+25)×40÷10=", false, r -> {
        int a = ri(r, 5, 25); int b = ri(r, 5, 25); int s = a + b;
        int c = ri(r, 2, 9); int p = s * c;
        List<Integer> divs = new ArrayList<>();
        for (int d = 2; d <= 9; d++) if (p % d == 0) divs.add(d);
        int d = divs.get(r.nextInt(divs.size()));
        return new Question("(" + a + " + " + b + ") × " + c + " ÷ " + d + " =", null, null, false);
    });

    // ---------- 五年级 全学期 ----------
    private static final Topic G5_T1 = new Topic("5_fracDen1", "分数加减法(分母为个位数)", "例题：6/8+7/9=", false, r -> {
        int d1 = ri(r, 2, 9); int d2 = ri(r, 2, 9);
        int n1 = ri(r, 1, d1 - 1); int n2 = ri(r, 1, d2 - 1);
        return new Question(n1 + "/" + d1 + (r.nextBoolean() ? " + " : " - ") + n2 + "/" + d2 + " =",
                null, null, false);
    });

    // ---------- 年级/学期映射 ----------
    public static List<Topic> topicsFor(int grade, boolean spring) {
        // grade: 1..5；spring=true 下学期，false 上学期；4/5 年级忽略学期
        switch (grade) {
            case 1: return spring ? list(G1D_T1, G1D_T2, G1D_T3)
                                 : list(G1U_T1, G1U_T2, G1U_T3, G1U_T4, G1U_T5, G1U_T6);
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
