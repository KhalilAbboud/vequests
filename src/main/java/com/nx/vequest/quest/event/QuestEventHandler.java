
package com.nx.vequest.quest.event;

import com.nx.vequest.quest.player.QuestProgressService;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;

public final class QuestEventHandler {

    private QuestEventHandler() {
    }

    public static void register() {

        PlayerBlockBreakEvents.AFTER.register(
                (level, player, pos, state, blockEntity) -> {

                    // Only process actual server-side players.
                    if (!(player instanceof ServerPlayer serverPlayer)) {
                        return;
                    }

                    QuestProgressService.onBlockBroken(
                            serverPlayer,
                            state
                    );
                }
        );
    }
}
