package com.riotmc.arena.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.player.ArenaPlayer;
import com.riotmc.arena.player.PlayerStatus;
import com.riotmc.arena.team.Team;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public final class CreateTeamItem implements CustomItem {
    @Getter
    public final Arenas plugin;

    public CreateTeamItem(Arenas plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.CLOCK;
    }

    @Override
    public String getName() {
        return ChatColor.BLUE + "Create Team";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.AQUA + "Right-click while holding this item");
        lore.add(ChatColor.AQUA + "to create a new team");

        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {
            final ArenaPlayer player = plugin.getPlayerManager().getPlayer(who.getUniqueId());

            if (player == null) {
                who.sendMessage(ChatColor.RED + "Failed to obtain your profile");
                return;
            }

            if (player.getTeam() != null) {
                who.sendMessage(ChatColor.RED + "You are already on a team");
                return;
            }

            if (!player.getStatus().equals(PlayerStatus.LOBBY)) {
                who.sendMessage(ChatColor.RED + "You are not in the lobby");
                return;
            }

            plugin.getTeamHandler().createTeam(player, new FailablePromise<Team>() {
                @Override
                public void success(@Nonnull Team team) {
                    plugin.getTeamManager().getTeams().add(team);
                    plugin.getPlayerHandler().giveLobbyItems(player);
                    who.sendMessage(ChatColor.GREEN + "Team created");
                }

                @Override
                public void failure(@Nonnull String reason) {
                    who.sendMessage(ChatColor.RED + reason);
                }
            });
        };
    }
}