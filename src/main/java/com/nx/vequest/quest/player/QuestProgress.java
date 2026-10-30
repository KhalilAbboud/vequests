
package com.nx.vequest.quest.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nx.vequest.quest.QuestDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record QuestProgress(
        boolean active,
        boolean completed,
        List<Integer> objectiveProgress
) {

    public static final Codec<QuestProgress> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.fieldOf("active")
                            .forGetter(QuestProgress::active),

                    Codec.BOOL.fieldOf("completed")
                            .forGetter(QuestProgress::completed),

                    Codec.INT.listOf()
                            .fieldOf("objective_progress")
                            .forGetter(QuestProgress::objectiveProgress)

            ).apply(instance, QuestProgress::new));

    public QuestProgress {
        Objects.requireNonNull(objectiveProgress);

        objectiveProgress = List.copyOf(objectiveProgress);

        if (objectiveProgress.stream().anyMatch(value -> value < 0)) {
            throw new IllegalArgumentException(
                    "Objective progress cannot be negative"
            );
        }
    }

    public static QuestProgress startFor(QuestDefinition quest) {

        List<Integer> initialProgress = quest.objectives()
                .stream()
                .map(objective -> 0)
                .toList();

        return new QuestProgress(
                true,
                false,
                initialProgress
        );
    }

    public int progressAt(int index) {
        if (index < 0 || index >= objectiveProgress.size()) {
            return 0;
        }

        return objectiveProgress.get(index);
    }

    public QuestProgress withObjectiveProgress(
            int index,
            int amount
    ) {
        if (index < 0 || index >= objectiveProgress.size()) {
            throw new IllegalArgumentException(
                    "Invalid objective index: " + index
            );
        }

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Progress cannot be negative"
            );
        }

        List<Integer> updatedProgress =
                new ArrayList<>(objectiveProgress);

        updatedProgress.set(index, amount);

        return new QuestProgress(
                active,
                completed,
                updatedProgress
        );
    }

    public boolean allObjectivesComplete(QuestDefinition quest) {

        for (int i = 0; i < quest.objectives().size(); i++) {

            int current = progressAt(i);

            int required = quest.objectives()
                    .get(i)
                    .requiredAmount();

            if (current < required) {
                return false;
            }
        }

        return true;
    }

    public QuestProgress markCompleted() {
        return new QuestProgress(
                false,
                true,
                objectiveProgress
        );
    }
}
