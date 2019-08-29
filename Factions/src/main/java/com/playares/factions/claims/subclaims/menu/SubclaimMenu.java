package com.playares.factions.claims.subclaims.menu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.services.profiles.ProfileService;
import com.playares.services.profiles.data.AresProfile;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SubclaimMenu extends Menu {
    @Getter public final Factions factions;
    @Getter public final Subclaim subclaim;

    public SubclaimMenu(@Nonnull Factions plugin, @Nonnull Player player, @Nonnull Subclaim subclaim) {
        super(plugin, player, "Subclaim Editor", 6);
        this.factions = plugin;
        this.subclaim = subclaim;
    }

    public void update() {
        final PlayerFaction faction = subclaim.getFaction();
        final ProfileService profileService = (ProfileService)plugin.getService(ProfileService.class);

        if (profileService == null) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Failed to obtain Profile Service");
            return;
        }

        if (faction == null) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Faction not found");
            return;
        }

        final PlayerFaction.FactionProfile playerProfile = faction.getMember(player.getUniqueId());

        if (playerProfile == null) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Failed to obtain your Faction Profile");
            return;
        }

        /*
        DELETE 45
        RANK ACCESS LEVEL 53
         */

        new Scheduler(plugin).async(() -> {
            final Map<PlayerFaction.FactionProfile, AresProfile> profiles = Maps.newHashMap();
            final Map<UUID, OfflinePlayer> offlinePlayers = Maps.newHashMap();

            for (PlayerFaction.FactionProfile factionProfile : faction.getMembers()) {
                final AresProfile profile = profileService.getProfileBlocking(factionProfile.getUniqueId());

                if (profile == null) {
                    continue;
                }

                profiles.put(factionProfile, profile);
                offlinePlayers.put(profile.getUniqueId(), Bukkit.getOfflinePlayer(profile.getUniqueId()));
            }

            new Scheduler(plugin).sync(() -> {
                clearInventory();

                final ItemStack deleteIcon = new ItemBuilder()
                        .setMaterial(Material.BARRIER)
                        .setName(ChatColor.RED + "Delete Subclaim")
                        .addLore(ChatColor.GRAY + "This action can " + ChatColor.RED + "" + ChatColor.UNDERLINE + "not" + ChatColor.GRAY + " be undone!")
                        .build();

                final ItemStack accessLevelIcon = new ItemBuilder()
                        .setMaterial(Material.EMERALD)
                        .setName(ChatColor.GOLD + "Access Level" + ChatColor.YELLOW + ": " + WordUtils.capitalize(subclaim.getAccessLevel().name().toLowerCase().replace("_", " ")))
                        .addLore(Arrays.asList(ChatColor.YELLOW + "All members of the faction at or", ChatColor.YELLOW + "above " + WordUtils.capitalize(subclaim.getAccessLevel().name().toLowerCase().replace("_", " ")) + " can access the subclaim"))
                        .build();

                addItem(new ClickableItem(deleteIcon, 45, click -> {
                    player.closeInventory();

                    factions.getSubclaimManager().getDeletionHandler().deleteSubclaim(player, subclaim, new SimplePromise() {
                        @Override
                        public void success() {
                            player.sendMessage(ChatColor.RED + "Subclaim deleted");
                            getFactions().getSubclaimManager().getUpdateHandler().performDelete(player, subclaim);
                        }

                        @Override
                        public void failure(@Nonnull String reason) {
                            player.sendMessage(ChatColor.RED + reason);
                        }
                    });
                }));

                addItem(new ClickableItem(accessLevelIcon, 53, click -> {
                    PlayerFaction.FactionRank nextLevel = subclaim.getAccessLevel().getNext();

                    if (nextLevel == null || nextLevel.isHigher(playerProfile.getRank())) {
                        nextLevel = PlayerFaction.FactionRank.MEMBER;
                    }

                    subclaim.setAccessLevel(nextLevel);

                    player.sendMessage(ChatColor.YELLOW + "Subclaim Access Level has been changed to " + ChatColor.BLUE + WordUtils.capitalize(nextLevel.name().toLowerCase().replace("_", " ")));

                    factions.getSubclaimManager().getUpdateHandler().performUpdate(subclaim);
                }));

                int pos = 0;

                for (PlayerFaction.FactionProfile factionProfile : profiles.keySet()) {
                    final AresProfile aresProfile = profiles.get(factionProfile);
                    final boolean canAccess = subclaim.canAccess(factionProfile.getUniqueId(), factionProfile.getRank());
                    final OfflinePlayer offlinePlayer = offlinePlayers.get(aresProfile.getUniqueId());
                    final ItemStack icon = new ItemStack(Material.SKULL_ITEM);
                    final SkullMeta meta = (SkullMeta)icon.getItemMeta();
                    final List<String> lore = Lists.newArrayList();

                    icon.setDurability((short)3);
                    meta.setDisplayName((subclaim.canAccess(factionProfile.getUniqueId(), factionProfile.getRank()) ? ChatColor.GREEN + aresProfile.getUsername() : ChatColor.RED + aresProfile.getUsername()));
                    meta.setOwningPlayer(offlinePlayer);

                    if (canAccess) {
                        meta.setDisplayName(ChatColor.GREEN + aresProfile.getUsername());
                        lore.add(ChatColor.GREEN + "This player can access this subclaim");
                    } else {
                        meta.setDisplayName(ChatColor.RED + aresProfile.getUsername());
                        lore.add(ChatColor.RED + "This player can not access this subclaim");
                    }

                    meta.setLore(lore);
                    icon.setItemMeta(meta);

                    addItem(new ClickableItem(icon, pos, click -> {
                        if (canAccess) {
                            if (!subclaim.getAccessPlayers().contains(aresProfile.getUniqueId())) {
                                player.sendMessage(ChatColor.RED + "This player is able to access this subclaim because their rank is " + WordUtils.capitalize(factionProfile.getRank().name().toLowerCase().replace("_", " ")));
                                return;
                            }

                            subclaim.getAccessPlayers().remove(aresProfile.getUniqueId());
                            player.sendMessage(ChatColor.DARK_GREEN + aresProfile.getUsername() + ChatColor.YELLOW + " can " + ChatColor.RED + "no longer" + ChatColor.YELLOW + " access this subclaim");
                            factions.getSubclaimManager().getUpdateHandler().performUpdate(subclaim);
                            return;
                        }

                        subclaim.getAccessPlayers().add(aresProfile.getUniqueId());
                        player.sendMessage(ChatColor.DARK_GREEN + aresProfile.getUsername() + ChatColor.YELLOW + " can " + ChatColor.GREEN + "now" + ChatColor.YELLOW + " access this subclaim");
                        factions.getSubclaimManager().getUpdateHandler().performUpdate(subclaim);
                    }));

                    pos++;
                }

                player.updateInventory();
            }).run();
        }).run();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);
        factions.getSubclaimManager().getUpdateHandler().closeMenu(this);
    }
}