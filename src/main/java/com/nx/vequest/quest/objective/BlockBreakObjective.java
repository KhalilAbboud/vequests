package com.nx.vequest.quest.objective;

import net.minecraft.world.level.block.state.BlockState;

public interface BlockBreakObjective extends QuestObjective {

    boolean matches(BlockState state);
}