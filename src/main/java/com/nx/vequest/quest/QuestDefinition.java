
package com.nx.vequest.quest;

import com.nx.vequest.quest.objective.QuestObjective;
import com.nx.vequest.quest.reward.QuestReward;

import java.util.List;
import java.util.Objects;

public record QuestDefinition(
        String id,
        String title,
        String description,
        List<QuestObjective> objectives,
        List<QuestReward> rewards
) {
    public QuestDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Quest ID cannot be empty"
            );
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Quest title cannot be empty"
            );
        }

        Objects.requireNonNull(description, "description");

        objectives = List.copyOf(
                Objects.requireNonNull(objectives, "objectives")
        );

        rewards = List.copyOf(
                Objects.requireNonNull(rewards, "rewards")
        );

        if (objectives.isEmpty()) {
            throw new IllegalArgumentException(
                    "A quest must have at least one objective"
            );
        }
    }
}
