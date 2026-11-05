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
     * 按题型生成题目并分组为试卷分区（顺序与 counts 插入顺序一致）。
     *
     * @param counts          每个题型 -> 数量
     * @param useHorizontal   是否允许横式
     * @param useVertical     是否允许竖式
     * @param randomizeFormat 两者都选时，逐题随机
     */
    public List<PaperLayout.Section> buildSections(Map<TopicCatalog.Topic, Integer> counts,
                                                   boolean useHorizontal, boolean useVertical,
                                                   boolean randomizeFormat) {
        List<PaperLayout.Section> sections = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Map.Entry<TopicCatalog.Topic, Integer> e : counts.entrySet()) {
            int n = e.getValue();
            if (n <= 0) continue;
            List<Question> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Question q = unique(e.getKey(), seen);
                if (q != null) list.add(q);
            }
            if (list.isEmpty()) continue;

            for (Question q : list) {
                boolean wantV;
                if (!q.verticalSupported) {
                    wantV = false;
                } else if (randomizeFormat) {
                    wantV = random.nextBoolean();
                } else {
                    wantV = useVertical;
                }
                q.vertical = wantV;
            }
            sections.add(new PaperLayout.Section(e.getKey().title, e.getKey().kind, list));
        }
        return sections;
    }

    private Question unique(TopicCatalog.Topic topic, Set<String> seen) {
        for (int i = 0; i < 5000; i++) {
            Question q = topic.gen.generate(random);
            if (seen.add(q.key())) return q;
        }
        return null;
    }
}
