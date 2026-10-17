package com.nx.vequest.quest;

import com.nx.vequest.quest.objective.MineBlockObjective;
import com.nx.vequest.quest.reward.ExperienceReward;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class QuestManager {

    private static final Map<String, QuestDefinition> QUESTS =
            new LinkedHashMap<>();

    static {
        register(new QuestDefinition(
                "mine_5_stone",
                "Stone Starter",
                "Mine five stone blocks to begin your journey.",
                List.of(
                        new MineBlockObjective("minecraft:stone", 5)
                ),
                List.of(
                        new ExperienceReward(20)
                )
        ));
    }

    private QuestManager() {
    }

    public static void register(QuestDefinition quest) {
        QuestDefinition previous = QUESTS.putIfAbsent(
                quest.id(),
                quest
        );

        if (previous != null) {
            throw new IllegalArgumentException(
                    "Duplicate quest ID: " + quest.id()
            );
        }
    }

    public static Collection<QuestDefinition> getAll() {
        return List.copyOf(QUESTS.values());
    }

    public static Optional<QuestDefinition> findById(String id) {
        return Optional.ofNullable(QUESTS.get(id));
    }
}
