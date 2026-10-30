
package com.nx.vequest.quest.player;

import com.nx.vequest.VanillaExtraQuests;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class QuestDataAttachments {

    public static final AttachmentType<PlayerQuestData> PLAYER_QUEST_DATA =
            AttachmentRegistry.create(
                    VanillaExtraQuests.id("player_quest_data"),
                    builder -> builder
                            .initializer(PlayerQuestData::empty)
                            .persistent(PlayerQuestData.CODEC)
                            .copyOnDeath()
            );

    private QuestDataAttachments() {
    }

    public static void initialize() {
        // Ensures this class is initialized during mod startup.
    }
}
