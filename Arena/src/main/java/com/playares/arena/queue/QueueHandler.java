package com.playares.arena.queue;

import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class QueueHandler {
    @Getter public final QueueManager manager;

    public void openUnrankedQueueSelector(Player player) {
        final Menu menu = new Menu(manager.getPlugin(), player, ChatColor.DARK_AQUA + "Unranked Queues", 1);
        final ArenaPlayer profile = getManager().getPlugin().getPlayerManager().getPlayer(player);

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        for (MatchmakingQueue queues : manager.getMatchmakingQueues()) {
            menu.addItem(new ClickableItem(queues.getIcon(), queues.getQueueType().getIconPosition(), click -> {
                player.closeInventory();

                final SearchingPlayer search = new SearchingPlayer(profile, queues.getQueueType(), null);
                getManager().getSearchingPlayers().add(search);
                manager.getPlugin().getPlayerManager().getHandler().giveItems(profile);

                player.sendMessage(ChatColor.YELLOW + "You have joined the " + ChatColor.GREEN + "unranked" + ChatColor.YELLOW + " queue for " + ChatColor.AQUA + queues.getQueueType().getDisplayName());
            }));
        }

        menu.open();
    }

    public void openRankedQueueSelector(Player player) {
        final Menu menu = new Menu(manager.getPlugin(), player, ChatColor.RED + "Ranked Queues", 1);
        final ArenaPlayer profile = getManager().getPlugin().getPlayerManager().getPlayer(player);

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        if (!profile.getRankedData().isLoaded()) {
            player.sendMessage(ChatColor.RED + "Please wait a moment while we fetch your ranked data");
            return;
        }

        for (MatchmakingQueue queues : manager.getMatchmakingQueues()) {
            menu.addItem(new ClickableItem(queues.getIcon(), queues.getQueueType().getIconPosition(), click -> {
                player.closeInventory();

                final SearchingPlayer search = new SearchingPlayer(profile, queues.getQueueType(), new RankedData(profile.getRankedData().getRating(queues.getQueueType())));
                getManager().getSearchingPlayers().add(search);
                manager.getPlugin().getPlayerManager().getHandler().giveItems(profile);

                player.sendMessage(ChatColor.YELLOW + "You have joined the " + ChatColor.RED + "ranked" + ChatColor.YELLOW + " queue for " + ChatColor.AQUA + queues.getQueueType().getDisplayName());
            }));
        }

        menu.open();
    }
}