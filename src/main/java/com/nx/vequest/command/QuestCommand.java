
package com.nx.vequest.command;

import com.nx.vequest.quest.QuestDefinition;
import com.nx.vequest.quest.QuestManager;
import com.nx.vequest.quest.player.PlayerQuestData;
import com.nx.vequest.quest.player.QuestProgress;
import com.nx.vequest.quest.player.QuestProgressService;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class QuestCommand {

    private QuestCommand() {
    }

    public static void register() {

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            Commands.literal("quests")

                                    .executes(QuestCommand::listQuests)

                                    .then(
                                            Commands.literal("list")
                                                    .executes(QuestCommand::listQuests)
                                    )

                                    .then(
                                            Commands.literal("start")
                                                    .then(
                                                            Commands.argument(
                                                                    "id",
                                                                    StringArgumentType.word()
                                                            ).executes(QuestCommand::startQuest)
                                                    )
                                    )

                                    .then(
                                            Commands.literal("progress")
                                                    .executes(QuestCommand::showProgress)
                                    )
                    );
                }
        );
    }

    // List all registered quests.
    private static int listQuests(
            CommandContext<CommandSourceStack> context
    ) {

        var source = context.getSource();

        var quests = QuestManager.getAll();

        if (quests.isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal("No quests are registered."),
                    false
            );
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("=== Available Quests ==="),
                false
        );

        for (QuestDefinition quest : quests) {

            source.sendSuccess(
                    () -> Component.literal(
                            quest.title() + " [" + quest.id() + "]"
                    ),
                    false
            );
        }

        return quests.size();
    }

    // Start a quest for the executing player.
    private static int startQuest(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        var source = context.getSource();

        ServerPlayer player = source.getPlayerOrException();

        String questId = StringArgumentType.getString(
                context,
                "id"
        );

        QuestDefinition quest = QuestManager.findById(questId)
                .orElse(null);

        if (quest == null) {
            source.sendFailure(
                    Component.literal("Unknown quest: " + questId)
            );
            return 0;
        }

        boolean started = QuestProgressService.startQuest(
                player,
                questId
        );

        if (!started) {
            source.sendFailure(
                    Component.literal(
                            "You have already started or completed this quest."
                    )
            );
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Quest started: " + quest.title()
                ),
                false
        );

        return 1;
    }

    // Display the executing player's quest progress.
    private static int showProgress(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        var source = context.getSource();

        ServerPlayer player = source.getPlayerOrException();

        PlayerQuestData data =
                QuestProgressService.getPlayerData(player);

        if (data.quests().isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal(
                            "You haven't started any quests yet."
                    ),
                    false
            );
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("=== Your Quest Progress ==="),
                false
        );

        for (var entry : data.quests().entrySet()) {

            String questId = entry.getKey();
            QuestProgress progress = entry.getValue();

            QuestDefinition quest = QuestManager.findById(questId)
                    .orElse(null);

            String title = quest != null
                    ? quest.title()
                    : questId;

            String status = progress.completed()
                    ? "Completed"
                    : progress.active()
                    ? "Active"
                    : "Inactive";

            source.sendSuccess(
                    () -> Component.literal(title + " - " + status),
                    false
            );

            if (quest == null) {
                continue;
            }

            for (int i = 0; i < quest.objectives().size(); i++) {

                int index = i;

                var objective = quest.objectives().get(index);

                String objectiveInfo =
                        objective.description()
                                + " ["
                                + progress.progressAt(index)
                                + "/"
                                + objective.requiredAmount()
                                + "]";

                source.sendSuccess(
                        () -> Component.literal("  " + objectiveInfo),
                        false
                );
            }
        }

        return 1;
    }
}
