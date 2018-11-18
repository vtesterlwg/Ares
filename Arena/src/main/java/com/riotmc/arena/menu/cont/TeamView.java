package com.riotmc.arena.menu.cont;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.player.ArenaPlayer;
import com.riotmc.arena.team.Team;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public final class TeamView extends Menu implements Listener {
    @Nonnull @Getter
    public final Arenas arenas;

    @Nullable @Getter
    public BukkitTask updateTask;

    public TeamView(@Nonnull Arenas plugin, @Nonnull Player player, @Nonnull String title, int rows) {
        super(plugin, player, title, rows);
        this.arenas = plugin;
    }

    public void startUpdater() {
        updateTask = new Scheduler(plugin).sync(() -> {
            final List<Team> teams = arenas.getTeamManager().getAvailableTeams();

            teams.sort(Comparator.comparingInt(t -> t.getMembers().size()));

            clearInventory();

            for (int i = 0; i < teams.size(); i++) {
                if (i >= 53) {
                    break;
                }

                final Team team = teams.get(i);
                final ArenaPlayer teamLeader = team.getLeader();
                final List<String> teamRoster = Lists.newArrayList();
                final ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
                final SkullMeta meta = (SkullMeta)icon.getItemMeta();

                for (ArenaPlayer member : team.getMembers()) {
                    teamRoster.add(ChatColor.YELLOW + member.getUsername());
                }

                meta.setDisplayName(ChatColor.BLUE + teamLeader.getUsername() + "'s Team");
                meta.setLore(teamRoster);
                meta.setOwner(teamLeader.getUsername());

                icon.setItemMeta(meta);

                addItem(new ClickableItem(icon, i, click -> {
                    final ArenaPlayer profile = arenas.getPlayerManager().getPlayer(player.getUniqueId());

                    if (profile == null) {
                        player.sendMessage(ChatColor.RED + "Could not find your profile");
                        return;
                    }

                    if (profile.getTeam() == null) {
                        player.sendMessage(ChatColor.RED + "You are not on a team");
                        return;
                    }

                    if (profile.getTeam().getUniqueId().equals(team.getUniqueId())) {
                        return;
                    }

                    if (!profile.getTeam().getLeader().getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You are not the leader of the team");
                        return;
                    }

                    new Scheduler(plugin).sync(() -> {
                        player.closeInventory();
                        arenas.getMenuHandler().openTeamChallengeMenu(player, team);
                    }).run();
                }));
            }
        }).repeat(0L, 3 * 20L).run();
    }

    public void stopUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        InventoryCloseEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getPlayer();

        if (getPlayer().getUniqueId().equals(player.getUniqueId()) || event.getInventory().equals(getInventory())) {
            stopUpdater();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (getPlayer().getUniqueId().equals(player.getUniqueId())) {
            stopUpdater();
        }
    }
}
