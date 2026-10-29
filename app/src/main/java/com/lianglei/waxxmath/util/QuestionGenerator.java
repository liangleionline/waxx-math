package com.lianglei.waxxmath.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** 按所选题型与数量批量生成不重复题目，并按横/竖式规则决定展示形式。 */
public class QuestionGenerator {

    private final Random random = new Random();

    /**
     * @param counts            每个题型 -> 数量
     * @param useHorizontal     是否允许横式
     * @param useVertical       是否允许竖式
     * @param randomizeFormat    两者都选时，逐题随机
     */
    public List<Question> generate(Map<TopicCatalog.Topic, Integer> counts,
                                  boolean useHorizontal, boolean useVertical,
                                  boolean randomizeFormat) {
        List<Question> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Map.Entry<TopicCatalog.Topic, Integer> e : counts.entrySet()) {
            int n = e.getValue();
            for (int i = 0; i < n; i++) {
                Question q = unique(e.getKey(), seen);
                if (q != null) out.add(q);
            }
        }
        // 决定展示形式
        for (Question q : out) {
            boolean wantV;
            if (!q.verticalSupported) {
                wantV = false;
            } else if (randomizeFormat) {
                wantV = random.nextBoolean();
            } else if (useVertical) {
                wantV = true;
            } else {
                wantV = false;
            }
            q.vertical = wantV;
        }
        return out;
    }

    private Question unique(TopicCatalog.Topic topic, Set<String> seen) {
        for (int i = 0; i < 5000; i++) {
            Question q = topic.gen.generate(random);
            String k = q.expr;
            if (seen.add(k)) return q;
        }
        return null;
    }
}
