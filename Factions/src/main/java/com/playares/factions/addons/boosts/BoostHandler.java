package com.playares.factions.addons.boosts;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.boosts.data.ActiveBoost;
import com.playares.factions.addons.boosts.data.Boost;
import com.playares.factions.addons.boosts.data.BoostDAO;
import com.playares.services.profiles.ProfileService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
public final class BoostHandler {
    @Getter public final BoostAddon addon;

    public void openMenu(Player player) {
        final Menu menu = new Menu(addon.getPlugin(), player, "Your Boosts", 3);

        new Scheduler(getAddon().getPlugin()).async(() -> {
            final List<Boost> boosts = BoostDAO.getBoosts(addon.getPlugin().getMongo(), player.getUniqueId());

            new Scheduler(getAddon().getPlugin()).sync(() -> {
                if (boosts != null && !boosts.isEmpty()) {
                    int pos = 0;

                    for (Boost boost : boosts) {
                        final ItemBuilder icon = new ItemBuilder()
                                .setMaterial(boost.getType().getIcon())
                                .setName(ChatColor.AQUA + boost.getType().getDisplayName())
                                .addLore(ChatColor.GRAY + boost.getType().getDescription())
                                .addLore(ChatColor.RESET + " ")
                                .addLore(ChatColor.GOLD + "Duration" + ChatColor.YELLOW + ": " + Time.convertToRemaining(boost.getDuration() * 1000));

                        menu.addItem(new ClickableItem(icon.build(), pos, click -> {
                            if (getAddon().getActiveBoost() != null) {
                                player.sendMessage(ChatColor.RED + "A server boost is already active. You may use your boost when it expires in " + Time.convertToRemaining(addon.getActiveBoost().getExpire() - Time.now()));
                                return;
                            }

                            player.closeInventory();
                            applyBoost(boost, player.getName());
                        }));

                        pos++;
                    }
                }

                menu.open();
            }).run();
        }).run();
    }

    public void give(CommandSender sender, String username, String typeName, int duration, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)getAddon().getPlugin().getService(ProfileService.class);
        final Boost.BoostType type;

        try {
            type = Boost.BoostType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            promise.failure("Invalid boost type");
            return;
        }

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        profileService.getProfile(username, aresProfile -> {
            if (aresProfile == null) {
                promise.failure("Player not found");
                return;
            }

            final Boost boost = new Boost(aresProfile.getUniqueId(), type, duration);

            new Scheduler(getAddon().getPlugin()).async(() -> {
                BoostDAO.saveBoost(getAddon().getPlugin().getMongo(), boost);

                new Scheduler(getAddon().getPlugin()).sync(() -> {
                    if (Bukkit.getPlayer(aresProfile.getUniqueId()) != null) {
                        Bukkit.getPlayer(aresProfile.getUniqueId()).sendMessage(ChatColor.GREEN + "You have been granted a " + type.getDisplayName() + " Booster!");
                    }

                    sender.sendMessage(ChatColor.GREEN + "You gave " + aresProfile.getUsername() + " a " + type.getDisplayName() + " booster");
                    Logger.print(sender.getName() + " granted " + aresProfile.getUsername() + " a " + type.getDisplayName() + " booster!");
                    promise.success();
                }).run();
            }).run();
        });
    }

    public void applyBoost(Boost boost, String username) {
        new Scheduler(getAddon().getPlugin()).async(() -> {
            BoostDAO.deleteBoost(getAddon().getPlugin().getMongo(), boost);

            new Scheduler(getAddon().getPlugin()).sync(() -> {
                getAddon().setActiveBoost(new ActiveBoost(boost, username, boost.getDuration()));

                addon.setCompletionTask(new Scheduler(getAddon().getPlugin()).sync(() -> {
                    Bukkit.broadcastMessage(ChatColor.GOLD + addon.getActiveBoost().getUsername() + "'s " + ChatColor.RED + " server boost has finished");
                    addon.setActiveBoost(null);
                }).delay(boost.getDuration() * 20).run());

                Bukkit.broadcastMessage(ChatColor.AQUA + "Server is being boosted with " + ChatColor.RESET + addon.getActiveBoost().getBoost().getType().getDisplayName() +
                        ChatColor.AQUA + " by " + ChatColor.RESET + addon.getActiveBoost().getUsername());
            }).run();
        }).run();
    }
}