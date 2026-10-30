
package com.nx.vequest.quest.player;

import com.nx.vequest.quest.QuestDefinition;
import com.nx.vequest.quest.QuestManager;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.nx.vequest.quest.objective.BlockBreakObjective;

import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class QuestProgressService {

    private QuestProgressService() {
    }

    // Retrieve the player's quest data.
    public static PlayerQuestData getPlayerData(ServerPlayer player) {
        return player.getAttachedOrSet(
                QuestDataAttachments.PLAYER_QUEST_DATA,
                PlayerQuestData.empty()
        );
    }

    // Start a quest for a specific player.
    public static boolean startQuest(
            ServerPlayer player,
            String questId
    ) {
        QuestDefinition quest = QuestManager.findById(questId)
                .orElse(null);

        if (quest == null) {
            return false;
        }

        PlayerQuestData data = getPlayerData(player);

        // Prevent starting an already active or completed quest.
        if (data.quests().containsKey(questId)) {
            return false;
        }

        QuestProgress progress = QuestProgress.startFor(quest);

        PlayerQuestData updatedData = data.withQuestProgress(
                questId,
                progress
        );

        player.setAttached(
                QuestDataAttachments.PLAYER_QUEST_DATA,
                updatedData
        );

        return true;
    }

    // Add progress to a specific objective of an active quest.
    public static boolean addObjectiveProgress(
            ServerPlayer player,
            String questId,
            int objectiveIndex,
            int amount
    ) {

        if (amount <= 0) {
            return false;
        }

        QuestDefinition quest = QuestManager.findById(questId)
                .orElse(null);

        if (quest == null) {
            return false;
        }

        // Make sure the objective index is valid.
        if (objectiveIndex < 0 ||
                objectiveIndex >= quest.objectives().size()) {
            return false;
        }

        PlayerQuestData data = getPlayerData(player);

        QuestProgress progress = data.quests().get(questId);

        // Ignore quests that haven't been started or are completed.
        if (progress == null ||
                !progress.active() ||
                progress.completed()) {
            return false;
        }

        int current = progress.progressAt(objectiveIndex);

        int required = quest.objectives()
                .get(objectiveIndex)
                .requiredAmount();

        // Don't count progress beyond the objective requirement.
        if (current >= required) {
            return false;
        }

        int remaining = required - current;
        int newAmount = current + Math.min(amount, remaining);

        QuestProgress updatedProgress =
                progress.withObjectiveProgress(
                        objectiveIndex,
                        newAmount
                );

        boolean questCompleted =
                updatedProgress.allObjectivesComplete(quest);

        if (questCompleted) {
            updatedProgress = updatedProgress.markCompleted();
        }

        // Save the updated state before executing rewards.
        PlayerQuestData updatedData = data.withQuestProgress(
                questId,
                updatedProgress
        );

        player.setAttached(
                QuestDataAttachments.PLAYER_QUEST_DATA,
                updatedData
        );

        // Grant rewards only on the transition to completion.
        if (questCompleted) {
            for (var reward : quest.rewards()) {
                reward.grant(player);
            }

            player.sendSystemMessage(
                    Component.literal(
                            "Quest completed: " + quest.title()
                    )
            );
        }

        return true;
    }


    public static void onBlockBroken(
            ServerPlayer player,
            BlockState brokenState
    ) {
        PlayerQuestData originalData = getPlayerData(player);
        PlayerQuestData updatedData = originalData;

        // Remember quests completed by this block-breaking event.
        // Rewards will be granted after player data is updated.
        List<QuestDefinition> completedQuests = new ArrayList<>();

        // Only inspect quests assigned to this player.
        for (Map.Entry<String, QuestProgress> entry :
                originalData.quests().entrySet()) {

            String questId = entry.getKey();
            QuestProgress progress = entry.getValue();

            if (!progress.active() || progress.completed()) {
                continue;
            }

            QuestDefinition quest = QuestManager.findById(questId)
                    .orElse(null);

            if (quest == null) {
                continue;
            }

            QuestProgress updatedProgress = progress;
            boolean changed = false;

            // Check every objective belonging to this quest.
            for (int i = 0; i < quest.objectives().size(); i++) {

                var objective = quest.objectives().get(i);

                // Only block-breaking objectives can react to this event.
                if (!(objective instanceof BlockBreakObjective blockObjective)) {
                    continue;
                }

                // Ignore blocks that don't match the objective.
                if (!blockObjective.matches(brokenState)) {
                    continue;
                }

                int current = updatedProgress.progressAt(i);
                int required = objective.requiredAmount();

                if (current >= required) {
                    continue;
                }

                updatedProgress = updatedProgress.withObjectiveProgress(
                        i,
                        current + 1
                );

                changed = true;
            }

            if (!changed) {
                continue;
            }

            // Check whether all objectives have now been completed.
            if (updatedProgress.allObjectivesComplete(quest)) {
                updatedProgress = updatedProgress.markCompleted();
                completedQuests.add(quest);
            }

            updatedData = updatedData.withQuestProgress(
                    questId,
                    updatedProgress
            );
        }

        // Don't update the attachment if nothing changed.
        if (updatedData == originalData) {
            return;
        }

        // Save all updated quest states before granting rewards.
        player.setAttached(
                QuestDataAttachments.PLAYER_QUEST_DATA,
                updatedData
        );

        // Execute rewards for newly completed quests.
        for (QuestDefinition quest : completedQuests) {

            for (var reward : quest.rewards()) {
                reward.grant(player);
            }

            player.sendSystemMessage(
                    Component.literal(
                            "Quest completed: " + quest.title()
                    )
            );
        }
    }
}
