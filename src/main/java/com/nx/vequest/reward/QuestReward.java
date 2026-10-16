package com.nx.vequest.reward;
import net.minecraft.server.level.ServerPlayer;

public interface QuestReward {
    String description();

    void grant(ServerPlayer player);
}