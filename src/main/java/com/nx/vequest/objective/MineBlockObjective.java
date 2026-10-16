package com.nx.vequest.objective;

public record MineBlockObjective(
        String targetBlockId,
        int amount
) implements QuestObjective {

    public MineBlockObjective {
        if (targetBlockId == null || targetBlockId.isBlank()) {
            throw new IllegalArgumentException(
                    "Target block ID cannot be empty"
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }
    }

    @Override
    public String description() {
        return "Mine " + amount + " blocks of " + targetBlockId;
    }
}
