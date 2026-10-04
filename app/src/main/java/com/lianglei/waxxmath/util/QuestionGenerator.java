package com.lianglei.waxxmath.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 题目生成器：按数量、范围与随机性生成不重复题目。
 * 范围仅约束加减法的参与运算数字（不含结果）；乘除法使用固定因子区间。
 */
public class QuestionGenerator {

    private final Random random = new Random();
    // 乘除法默认因子区间
    private static final int MUL_MIN = 1, MUL_MAX = 9;

    /** 生成配置。 */
    public static class Config {
        public int addCount;
        public int subCount;
        public int mulCount;
        public int divCount;
        public Integer rangeStart; // 可为 null
        public Integer rangeEnd;   // 可为 null
        public boolean useHorizontal;
        public boolean useVertical;

        public int total() {
            return addCount + subCount + mulCount + divCount;
        }
    }

    /**
     * 生成题目列表。
     * @param cfg 配置
     * @param useRandomFormat 是否每道题随机横/竖式（两者都选时）
     */
    public List<Question> generate(Config cfg, boolean useRandomFormat) {
        List<Question> list = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        int aMin = 0, aMax = 9;
        if (cfg.rangeStart != null && cfg.rangeEnd != null) {
            aMin = cfg.rangeStart;
            aMax = cfg.rangeEnd;
        }

        add(list, seen, cfg.addCount, Question.Type.ADD, aMin, aMax);
        add(list, seen, cfg.subCount, Question.Type.SUB, aMin, aMax);
        add(list, seen, cfg.mulCount, Question.Type.MUL, MUL_MIN, MUL_MAX);
        add(list, seen, cfg.divCount, Question.Type.DIV, MUL_MIN, MUL_MAX);

        if (useRandomFormat) {
            for (Question q : list) {
                q.format = random.nextBoolean() ? Question.Format.HORIZONTAL : Question.Format.VERTICAL;
            }
        } else if (cfg.useHorizontal) {
            for (Question q : list) q.format = Question.Format.HORIZONTAL;
        } else {
            for (Question q : list) q.format = Question.Format.VERTICAL;
        }
        return list;
    }

    private void add(List<Question> list, Set<String> seen, int count,
                     Question.Type type, int lo, int hi) {
        for (int i = 0; i < count; i++) {
            Question q = makeUnique(type, lo, hi, seen);
            if (q != null) list.add(q);
        }
    }

    private Question makeUnique(Question.Type type, int lo, int hi, Set<String> seen) {
        int attempts = 0;
        while (attempts++ < 5000) {
            Question q = make(type, lo, hi);
            if (q == null) return null;
            String k = q.key();
            if (!seen.contains(k)) {
                seen.add(k);
                return q;
            }
        }
        return null; // 理论上不会触发：总数≤40 且区间足够
    }

    private Question make(Question.Type type, int lo, int hi) {
        switch (type) {
            case ADD: {
                int a = rand(lo, hi);
                int b = rand(lo, hi);
                return new Question(Question.Type.ADD, a, b, a + b);
            }
            case SUB: {
                // 保证结果非负：a >= b
                int a = rand(lo, hi);
                int b = rand(lo, a);
                return new Question(Question.Type.SUB, a, b, a - b);
            }
            case MUL: {
                int a = rand(lo, hi);
                int b = rand(lo, hi);
                return new Question(Question.Type.MUL, a, b, a * b);
            }
            case DIV: {
                int b = rand(lo, hi);
                int q = rand(lo, hi);
                int a = b * q;
                return new Question(Question.Type.DIV, a, b, q);
            }
        }
        return null;
    }

    private int rand(int lo, int hi) {
        return lo + random.nextInt(hi - lo + 1);
    }
}
