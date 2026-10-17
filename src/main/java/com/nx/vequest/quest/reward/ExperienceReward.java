package com.nx.vequest.quest.reward;

import net.minecraft.server.level.ServerPlayer;

public record ExperienceReward(int amount) implements QuestReward {

    public ExperienceReward {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "XP reward cannot be negative"
            );
        }
    }

    @Override
    public String description() {
        return amount + " XP";
    }

    @Override
    public void grant(ServerPlayer player) {
        player.giveExperiencePoints(amount);
    }
}
