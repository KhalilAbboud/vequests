
package com.nx.vequest.quest.objective;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public record MineBlockObjective(
        String targetBlockId,
        int amount
) implements BlockBreakObjective {

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

    @Override
    public int requiredAmount() {
        return amount;
    }

    @Override
    public boolean matches(BlockState state) {
        String blockId = BuiltInRegistries.BLOCK
                .getKey(state.getBlock())
                .toString();

        return targetBlockId.equals(blockId);
    }
}
