package com.riotmc.arena.menu;

import com.riotmc.arena.Arenas;
import com.riotmc.arena.aftermatch.PlayerReport;
import com.riotmc.arena.aftermatch.TeamReport;
import com.riotmc.arena.match.Match;
import com.riotmc.arena.menu.cont.DuelModeView;
import com.riotmc.arena.menu.cont.TeamModeView;
import com.riotmc.arena.menu.cont.TeamView;
import com.riotmc.arena.mode.Mode;
import com.riotmc.arena.player.ArenaPlayer;
import com.riotmc.arena.team.Team;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.item.ItemBuilder;
import com.riotmc.commons.bukkit.menu.ClickableItem;
import com.riotmc.commons.bukkit.menu.Menu;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public final class MenuHandler {
    @Nonnull @Getter
    public final Arenas plugin;

    public MenuHandler(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    public void openTeamMenu(@Nonnull Player viewer) {
        final TeamView teamViewer = new TeamView(plugin, viewer, "Teams", 6);
        teamViewer.startUpdater();
        teamViewer.open();
    }

    public void openPlayerReport(@Nonnull Player viewer, @Nonnull PlayerReport report) {
        final Menu menu = new Menu(plugin, viewer, report.getUsername(), 6);

        final ItemStack stats = new ItemBuilder()
                .setMaterial(Material.PAPER)
                .setName(ChatColor.GREEN + "Match Statistics")
                .addLore(Arrays.asList(
                        ChatColor.GOLD + "Health" + ChatColor.YELLOW + ": " + Math.round((report.getHealth() / 2) * 2) / 2.0,
                        ChatColor.GOLD + "Remaining Health Potions" + ChatColor.YELLOW + ": " + report.getRemainingHealthPotions(),
                        ChatColor.GOLD + "Hits" + ChatColor.YELLOW + ": " + report.getHits(),
                        ChatColor.GOLD + "Damage" + ChatColor.YELLOW + ": " + Math.round(report.getDamage()),
                        ChatColor.GOLD + "Longest Shot" + ChatColor.YELLOW + ": " + Math.round(report.getLongestShot()),
                        ChatColor.GOLD + "Arrows Hit" + ChatColor.YELLOW + ": " + report.getArrowsHit(),
                        ChatColor.GOLD + "Total Arrows" + ChatColor.YELLOW + ": " + report.getArrowsFired(),
                        ChatColor.GOLD + "Bow Accuracy" + ChatColor.YELLOW + ": " + String.format("%.2f", report.getAccuracy()) + "%"
                ))
                .build();

        menu.addItem(new ClickableItem(stats, 53, click -> {}));

        // TODO: Fix
        for (int i = 0; i < report.getContents().length; i++) {
            final ItemStack item = report.getContents()[i];

            if (item == null || item.getType().equals(Material.AIR)) {
                continue;
            }

            menu.addItem(new ClickableItem(item, i, click -> {}));
        }

        for (int i = 45; i < report.getArmor().length; i++) {
            final ItemStack item = report.getArmor()[i];

            if (item == null || item.getType().equals(Material.AIR)) {
                continue;
            }

            menu.addItem(new ClickableItem(item, i, click -> {}));
        }

        menu.open();
    }

    public void openTeamReport(@Nonnull Player viewer, @Nonnull TeamReport report) {
        final Match match = plugin.getMatchManager().getMatchById(report.getMatchId());

        if (match == null) {
            viewer.sendMessage(ChatColor.RED + "Match not found");
            return;
        }

        final Menu menu = new Menu(plugin, viewer, report.getName(), 6);
        int slot = 0;

        final ItemStack stats = new ItemBuilder()
                .setMaterial(Material.PAPER)
                .setName(ChatColor.GREEN + "Team Statistics")
                .addLore(Arrays.asList(
                        ChatColor.GOLD + "Hits" + ChatColor.YELLOW + ": " + report.getHits(),
                        ChatColor.GOLD + "Total Damage" + ChatColor.YELLOW + ": " + Math.round(report.getDamage()),
                        ChatColor.GOLD + "Used Health Potions" + ChatColor.YELLOW + ": " + report.getUsedHealthPotions()
                ))
                .build();

        for (PlayerReport playerReport : match.getPlayerReports()) {
            if (playerReport.getTeamId() == null || !playerReport.getTeamId().equals(report.getUniqueId())) {
                continue;
            }

            final UUID memberId = playerReport.getUniqueId();
            final String memberName = playerReport.getUsername();
            final ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
            final SkullMeta meta = (SkullMeta)icon.getItemMeta();

            meta.setDisplayName(ChatColor.AQUA + memberName);
            meta.setLore(Collections.singletonList(ChatColor.YELLOW + "Click to view " + memberName + "'s Inventory"));

            if (Bukkit.getPlayer(memberName) != null) {
                meta.setOwner(memberName);
            }

            icon.setItemMeta(meta);

            menu.addItem(new ClickableItem(icon, slot, click -> {
                viewer.closeInventory();
                viewer.performCommand("am p " + memberId.toString() + " " + report.getMatchId().toString());
            }));

            slot++;
        }

        menu.addItem(new ClickableItem(stats, 49, click -> {}));

        menu.open();
    }

    public void openTeamChallengeMenu(@Nonnull Player viewer, @Nonnull Team team) {
        final TeamModeView teamModeView = new TeamModeView(plugin, viewer, "Select Mode", 3, team);
        int slot = 0;

        for (Mode mode : plugin.getModeManager().getSortedConfiguredModes()) {
            teamModeView.addItem(new ClickableItem(mode.getIcon(), slot, click -> {
                final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(viewer.getUniqueId());

                if (profile == null) {
                    viewer.sendMessage(ChatColor.RED + "Could not find your profile");
                    return;
                }

                if (profile.getTeam() == null) {
                    viewer.sendMessage(ChatColor.RED + "You are no longer on a team");
                    return;
                }

                if (!profile.getTeam().getLeader().equals(profile)) {
                    viewer.sendMessage(ChatColor.RED + "You are not the leader of the team");
                    return;
                }

                plugin.getChallengeHandler().sendChallenge(profile.getTeam(), team, mode, new SimplePromise() {
                    @Override
                    public void success() {
                        viewer.sendMessage(ChatColor.GREEN + "Duel request sent");
                        viewer.closeInventory();
                    }

                    @Override
                    public void failure(@Nonnull String reason) {
                        viewer.sendMessage(ChatColor.RED + reason);
                        viewer.closeInventory();
                    }
                });
            }));

            slot++;
        }

        teamModeView.open();
    }

    public void openDuelChallengeMenu(Player viewer, ArenaPlayer challenged) {
        final DuelModeView duelModeView = new DuelModeView(plugin, viewer, "Select Mode", 3, challenged);
        int slot = 0;

        for (Mode mode : plugin.getModeManager().getSortedConfiguredModes()) {
            duelModeView.addItem(new ClickableItem(mode.getIcon(), slot, click -> {
                final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(viewer.getUniqueId());

                if (profile == null) {
                    viewer.sendMessage(ChatColor.RED + "Could not obtain your profile");
                    return;
                }

                plugin.getChallengeHandler().sendChallenge(profile, challenged, mode, new SimplePromise() {
                    @Override
                    public void success() {
                        viewer.sendMessage(ChatColor.GREEN + "Duel request sent");
                        viewer.closeInventory();
                    }

                    @Override
                    public void failure(@Nonnull String reason) {
                        viewer.sendMessage(ChatColor.RED + reason);
                        viewer.closeInventory();
                    }
                });
            }));

            slot++;
        }

        duelModeView.open();
    }
}
