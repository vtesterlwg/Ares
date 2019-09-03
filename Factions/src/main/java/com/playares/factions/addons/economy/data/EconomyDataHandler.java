package com.playares.factions.addons.economy.data;

import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.Promise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.economy.EconomyAddon;
import com.playares.services.profiles.ProfileService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class EconomyDataHandler {
    @Getter public final EconomyAddon addon;

    public EconomyDataHandler(EconomyAddon addon) {
        this.addon = addon;
    }

    public void getOrCreatePlayer(UUID uniqueId, Promise<EconomyPlayer> promise) {
        new Scheduler(addon.getPlugin()).async(() -> {
            final EconomyPlayer profile = EconomyDAO.getPlayer(addon, addon.getPlugin().getMongo(), uniqueId);

            new Scheduler(addon.getPlugin()).sync(() -> {
                if (profile == null) {
                    promise.ready(new EconomyPlayer(addon, uniqueId));
                    return;
                }

                promise.ready(profile);
            }).run();
        }).run();
    }

    public void getOrCreatePlayer(String username, FailablePromise<EconomyPlayer> promise) {
        final ProfileService profileService = (ProfileService)getAddon().getPlugin().getService(ProfileService.class);

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        profileService.getProfile(username, profile -> {
            if (profile == null) {
                promise.failure("Profile for " + username + " not found");
                return;
            }

            getOrCreatePlayer(profile.getUniqueId(), promise::success);
        });
    }

    public void savePlayer(EconomyPlayer player) {
        new Scheduler(getAddon().getPlugin()).async(() -> EconomyDAO.savePlayer(getAddon().getPlugin().getMongo(), player)).run();
    }

    public void getBalance(UUID uniqueId, Promise<Double> promise) {
        getOrCreatePlayer(uniqueId, economyPlayer -> promise.ready(economyPlayer.getBalance()));
    }

    public void getBalance(String username, Promise<Double> promise) {
        getOrCreatePlayer(username, new FailablePromise<EconomyPlayer>() {
            @Override
            public void success(@Nonnull EconomyPlayer economyPlayer) {
                promise.ready(economyPlayer.getBalance());
            }

            @Override
            public void failure(@Nonnull String reason) {
                promise.ready(0.0);
            }
        });
    }

    public void addToBalance(UUID uniqueId, double amount, SimplePromise promise) {
        getOrCreatePlayer(uniqueId, profile -> {
            profile.add(amount);

            new Scheduler(getAddon().getPlugin()).async(() -> {
                EconomyDAO.savePlayer(addon.getPlugin().getMongo(), profile);

                new Scheduler(getAddon().getPlugin()).sync(() -> {
                    if (Bukkit.getPlayer(profile.getUniqueId()) != null) {
                        Bukkit.getPlayer(profile.getUniqueId()).sendMessage(ChatColor.GREEN + "$" + String.format("%.2f", amount) + " has been added to your balance");
                        Bukkit.getPlayer(profile.getUniqueId()).sendMessage(ChatColor.GREEN + "Your balance is now $" + String.format("%.2f", profile.getBalance()));
                    }

                    promise.success();
                }).run();
            }).run();
        });
    }

    public void addToBalance(String username, double amount, SimplePromise promise) {
        getOrCreatePlayer(username, new FailablePromise<EconomyPlayer>() {
            @Override
            public void success(@Nonnull EconomyPlayer profile) {
                profile.add(amount);

                new Scheduler(getAddon().getPlugin()).async(() -> {
                    EconomyDAO.savePlayer(addon.getPlugin().getMongo(), profile);

                    new Scheduler(getAddon().getPlugin()).sync(() -> {
                        if (Bukkit.getPlayer(profile.getUniqueId()) != null) {
                            Bukkit.getPlayer(profile.getUniqueId()).sendMessage(ChatColor.GREEN + "$" + String.format("%.2f", amount) + " has been added to your balance");
                            Bukkit.getPlayer(profile.getUniqueId()).sendMessage(ChatColor.GREEN + "Your balance is now $" + String.format("%.2f", profile.getBalance()));
                        }

                        promise.success();
                    }).run();
                }).run();
            }

            @Override
            public void failure(@Nonnull String reason) {
                promise.failure(reason);
            }
        });
    }

    public void subtractFromBalance(UUID uniqueId, double amount, SimplePromise promise) {
        getOrCreatePlayer(uniqueId, profile -> {
            if (!profile.canAfford(amount)) {
                promise.failure("Insufficient funds");
                return;
            }

            profile.subtract(amount);

            new Scheduler(getAddon().getPlugin()).async(() -> {
                EconomyDAO.savePlayer(addon.getPlugin().getMongo(), profile);

                new Scheduler(getAddon().getPlugin()).sync(() -> {
                    if (Bukkit.getPlayer(profile.getUniqueId()) != null) {
                        Bukkit.getPlayer(profile.getUniqueId()).sendMessage(ChatColor.GREEN + "$" + String.format("%.2f", amount) + " has been subtracted from your balance");
                        Bukkit.getPlayer(profile.getUniqueId()).sendMessage(ChatColor.GREEN + "Your balance is now $" + String.format("%.2f", profile.getBalance()));
                    }

                    promise.success();
                }).run();
            }).run();
        });
    }

    public void subtractFromBalance(String username, double amount, SimplePromise promise) {
        getOrCreatePlayer(username, new FailablePromise<EconomyPlayer>() {
            @Override
            public void success(@Nonnull EconomyPlayer profile) {
                if (!profile.canAfford(amount)) {
                    promise.failure("Insufficient funds");
                    return;
                }

                profile.subtract(amount);

                new Scheduler(getAddon().getPlugin()).async(() -> {
                    EconomyDAO.savePlayer(addon.getPlugin().getMongo(), profile);

                    new Scheduler(getAddon().getPlugin()).sync(() -> {
                        if (Bukkit.getPlayer(profile.getUniqueId()) != null) {
                            Bukkit.getPlayer(profile.getUniqueId()).sendMessage(ChatColor.GREEN + "$" + String.format("%.2f", amount) + " has been subtracted from your balance");
                            Bukkit.getPlayer(profile.getUniqueId()).sendMessage(ChatColor.GREEN + "Your balance is now $" + String.format("%.2f", profile.getBalance()));
                        }

                        promise.success();
                    }).run();
                }).run();
            }

            @Override
            public void failure(@Nonnull String reason) {
                promise.failure(reason);
            }
        });
    }

    public void transferBalance(UUID uniqueId, String receiver, double amount, SimplePromise promise) {
        getOrCreatePlayer(uniqueId, profile -> {
            if (!profile.canAfford(amount)) {
                promise.failure("Insufficient funds");
                return;
            }

            getOrCreatePlayer(receiver, new FailablePromise<EconomyPlayer>() {
                @Override
                public void success(@Nonnull EconomyPlayer receiverProfile) {
                    profile.transferTo(receiverProfile, amount);

                    new Scheduler(getAddon().getPlugin()).async(() -> {
                        EconomyDAO.savePlayer(getAddon().getPlugin().getMongo(), profile);
                        EconomyDAO.savePlayer(getAddon().getPlugin().getMongo(), receiverProfile);

                        new Scheduler(getAddon().getPlugin()).sync(() -> {
                            final Player senderPlayer = Bukkit.getPlayer(uniqueId);
                            final Player receiverPlayer = Bukkit.getPlayer(receiverProfile.getUniqueId());

                            if (senderPlayer != null) {
                                senderPlayer.sendMessage(ChatColor.GREEN + "Balance transfer successful");
                                senderPlayer.sendMessage(ChatColor.GREEN + "Balance is now $" + String.format("%.2f", profile.getBalance()));
                            }

                            if (receiverPlayer != null) {
                                receiverPlayer.sendMessage(ChatColor.GREEN + "You have received $" + String.format("%.2f", amount) + ((senderPlayer != null) ? " from " + senderPlayer.getName() : ""));
                                receiverPlayer.sendMessage(ChatColor.GREEN + "Balance is now: $" + String.format("%.2f", receiverProfile.getBalance()));
                            }

                            promise.success();
                        }).run();
                    }).run();
                }

                @Override
                public void failure(@Nonnull String reason) {
                    promise.failure(reason);
                }
            });
        });
    }

    public void setBalance(CommandSender sender, String receiver, double amount, SimplePromise promise) {
        getOrCreatePlayer(receiver, new FailablePromise<EconomyPlayer>() {
            @Override
            public void success(@Nonnull EconomyPlayer profile) {
                profile.setBalance(amount);

                new Scheduler(addon.getPlugin()).async(() -> {
                    EconomyDAO.savePlayer(getAddon().getPlugin().getMongo(), profile);

                    new Scheduler(addon.getPlugin()).sync(() -> {
                        if (Bukkit.getPlayer(profile.getUniqueId()) != null) {
                            Bukkit.getPlayer(profile.getUniqueId()).sendMessage(ChatColor.YELLOW + "Your balance has been manually adjusted to " + ChatColor.GREEN + "$" + String.format("%.2f", profile.getBalance()));
                        }

                        promise.success();
                    }).run();
                }).run();
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}