
package com.nx.vequest.quest.player;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record PlayerQuestData(
        Map<String, QuestProgress> quests
) {

    public static final Codec<PlayerQuestData> CODEC =
            Codec.unboundedMap(
                    Codec.STRING,
                    QuestProgress.CODEC
            ).xmap(
                    PlayerQuestData::new,
                    PlayerQuestData::quests
            );

    public PlayerQuestData {
        quests = Map.copyOf(
                Objects.requireNonNull(quests, "quests")
        );
    }

    public static PlayerQuestData empty() {
        return new PlayerQuestData(Map.of());
    }

    public PlayerQuestData withQuestProgress(
            String questId,
            QuestProgress progress
    ) {
        Map<String, QuestProgress> updated =
                new HashMap<>(quests);

        updated.put(questId, progress);

        return new PlayerQuestData(updated);
    }
}
