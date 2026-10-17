package com.nx.vequest.quest.reward;
import net.minecraft.server.level.ServerPlayer;

public interface QuestReward {
    String description();

    void grant(ServerPlayer player);
}