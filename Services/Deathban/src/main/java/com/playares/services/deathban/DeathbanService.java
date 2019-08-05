package com.playares.services.deathban;

import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.deathban.command.LivesCommand;
import com.playares.services.deathban.command.ReviveCommand;
import com.playares.services.deathban.dao.DeathbanDAO;
import com.playares.services.deathban.dao.LivesDAO;
import com.playares.services.deathban.data.Deathban;
import com.playares.services.deathban.data.LivesPlayer;
import com.playares.services.deathban.listener.DeathbanListener;
import com.playares.services.profiles.ProfileService;
import com.playares.services.profiles.data.AresProfile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

public final class DeathbanService implements AresService {
    @Getter public final AresPlugin owner;
    @Getter public DeathbanConfig configuration;
    @Getter public DeathbanListener deathbanListener;
    @Getter public final Set<UUID> recentlyKicked;

    public DeathbanService(AresPlugin owner) {
        this.owner = owner;
        this.configuration = new DeathbanConfig(this);
        this.deathbanListener = new DeathbanListener(this);
        this.recentlyKicked = Sets.newConcurrentHashSet();
    }

    @Override
    public void start() {
        configuration.loadValues();

        registerCommand(new LivesCommand(this));
        registerCommand(new ReviveCommand(this));
        registerListener(deathbanListener);
    }

    @Override
    public void stop() {
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(deathbanListener);
        PlayerJoinEvent.getHandlerList().unregister(deathbanListener);
    }

    @Override
    public String getName() {
        return "Deathbans";
    }

    public void getLives(UUID uniqueId, FailablePromise<LivesPlayer> promise) {
        new Scheduler(owner).async(() -> {
            final LivesPlayer player = LivesDAO.getLivesPlayer(owner.getMongo(), Filters.eq("id", uniqueId));

            new Scheduler(owner).sync(() -> {
                if (player == null) {
                    promise.failure("Player not found");
                    return;
                }

                promise.success(player);
            }).run();
        }).run();
    }

    public void getLives(String username, FailablePromise<LivesPlayer> promise) {
        final ProfileService profileService = (ProfileService)owner.getService(ProfileService.class);

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(owner).async(() -> {
            final AresProfile profile = profileService.getProfileBlocking(username);

            new Scheduler(owner).sync(() -> {
                if (profile == null) {
                    promise.failure("Player not found");
                    return;
                }

                getLives(profile.getUniqueId(), promise);
            }).run();
        }).run();
    }

    public void giveLives(CommandSender commandSender, String username, int amount, boolean soulbound, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)owner.getService(ProfileService.class);
        final boolean bypass = commandSender.hasPermission("deathbans.bypass");

        // Player added -s to the end of the command but doesn't have perms
        if (soulbound && !bypass) {
            promise.failure("You do not have permission to give players Soulbound lives");
            return;
        }

        // Profile Service is needed to obtain UUID from Username
        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        if (amount <= 0) {
            promise.failure("Invalid amount");
            return;
        }

        new Scheduler(owner).async(() -> {
            // Retrieving the receivers Profile so we can obtain their UUID
            final AresProfile receiverProfile = profileService.getProfileBlocking(username);

            // Player does not exist in DB
            if (receiverProfile == null) {
                new Scheduler(owner).sync(() -> promise.failure("Player not found")).run();
                return;
            }

            final LivesPlayer receiver = LivesDAO.getLivesPlayer(owner.getMongo(), Filters.eq("id"));
            final LivesPlayer sender = (commandSender instanceof Player) ? LivesDAO.getLivesPlayer(owner.getMongo(), Filters.eq("id", ((Player)commandSender).getUniqueId())) : null;

            new Scheduler(owner).sync(() -> {
                // Receiver has connected to the network, but has never logged in to a server with Deathbans enabled
                if (receiver == null) {
                    promise.failure("Player not found");
                    return;
                }

                // The sender is a player and does not have bypass, so we should subtract lives
                if (sender != null && !bypass) {
                    // Sender doesn't have enough lives
                    if (sender.getStandardLives() < amount) {
                        promise.failure("You do not have enough lives");
                        return;
                    }

                    // Updating the lives for the sender
                    sender.setStandardLives(sender.getStandardLives() - amount);

                    new Scheduler(owner).async(() -> {
                        LivesDAO.saveLivesPlayer(owner.getMongo(), sender);

                        new Scheduler(owner).sync(() -> {
                            final Player senderPlayer = Bukkit.getPlayer(sender.getUniqueId());

                            if (senderPlayer != null) {
                                senderPlayer.sendMessage(ChatColor.YELLOW + "" + amount + " lives have been subtracted from your account");
                            }
                        }).run();
                    }).run();
                }

                // Updating the lives for the receiver
                if (soulbound) {
                    receiver.setSoulboundLives(receiver.getSoulboundLives() + amount);
                } else {
                    receiver.setStandardLives(receiver.getStandardLives() + amount);
                }

                new Scheduler(owner).async(() -> {
                    LivesDAO.saveLivesPlayer(owner.getMongo(), receiver);

                    new Scheduler(owner).sync(() -> {
                        final Player receiverPlayer = Bukkit.getPlayer(receiverProfile.getUniqueId());

                        if (receiverPlayer != null) {
                            receiverPlayer.sendMessage(ChatColor.GREEN + "You have received " + amount + " lives from " + commandSender.getName());

                            if (soulbound) {
                                receiverPlayer.sendMessage(ChatColor.YELLOW + "These lives are Soulbound and can not be given to others");
                            }
                        }
                    }).run();
                }).run();

                promise.success();
            }).run();
        }).run();
    }

    public void setLives(String username, int amount, boolean soulbound, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)owner.getService(ProfileService.class);

        // Profile Service is needed to obtain UUID from Username
        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        if (amount <= 0) {
            promise.failure("Invalid amount");
            return;
        }

        new Scheduler(owner).async(() -> {
            // Retrieving the receivers Profile so we can obtain their UUID
            final AresProfile receiverProfile = profileService.getProfileBlocking(username);

            // Player does not exist in DB
            if (receiverProfile == null) {
                new Scheduler(owner).sync(() -> promise.failure("Player not found")).run();
                return;
            }

            final LivesPlayer receiver = LivesDAO.getLivesPlayer(owner.getMongo(), Filters.eq("id"));

            new Scheduler(owner).sync(() -> {
                // Receiver has connected to the network, but has never logged in to a server with Deathbans enabled
                if (receiver == null) {
                    promise.failure("Player not found");
                    return;
                }

                // Updating the lives for the receiver
                if (soulbound) {
                    receiver.setSoulboundLives(amount);
                } else {
                    receiver.setStandardLives(amount);
                }

                new Scheduler(owner).async(() -> {
                    LivesDAO.saveLivesPlayer(owner.getMongo(), receiver);

                    new Scheduler(owner).sync(() -> {
                        final Player receiverPlayer = Bukkit.getPlayer(receiverProfile.getUniqueId());

                        if (receiverPlayer != null) {
                            receiverPlayer.sendMessage(ChatColor.YELLOW + "Your lives have been modified");
                        }
                    }).run();
                }).run();

                promise.success();
            }).run();
        }).run();
    }

    public void createDeathban(UUID uniqueId, long duration, boolean permanent) {
        final Deathban deathban = new Deathban(uniqueId, Time.now(), duration, permanent);

        new Scheduler(owner).async(() -> {
            final Deathban existing = DeathbanDAO.getDeathban(owner.getMongo(), uniqueId);

            if (existing != null) {
                DeathbanDAO.deleteDeathban(owner.getMongo(), existing);
            }

            DeathbanDAO.saveDeathban(owner.getMongo(), deathban);

            new Scheduler(owner).sync(() -> {
                final Player player = Bukkit.getPlayer(uniqueId);

                if (player != null) {
                    player.kickPlayer(getDeathbanKickMessage(deathban));
                }
            }).run();
        }).run();
    }

    public void removeDeathban(UUID uniqueId, SimplePromise promise) {
        new Scheduler(owner).async(() -> {
            final Deathban deathban = DeathbanDAO.getDeathban(owner.getMongo(), uniqueId);

            if (deathban == null) {
                new Scheduler(owner).sync(() -> promise.failure("Player is not deathbanned")).run();
                return;
            }

            DeathbanDAO.deleteDeathban(owner.getMongo(), deathban);

            new Scheduler(owner).sync(promise::success).run();
        }).run();
    }

    public void getDeathban(UUID uniqueId, FailablePromise<Deathban> promise) {
        new Scheduler(owner).async(() -> {
            final Deathban deathban = DeathbanDAO.getDeathban(owner.getMongo(), uniqueId);

            new Scheduler(owner).sync(() -> {
                if (deathban == null) {
                    promise.failure("Player is not deathbanned");
                    return;
                }

                promise.success(deathban);
            }).run();
        }).run();
    }

    public void revive(Player player, SimplePromise promise) {
        final boolean bypass = player.hasPermission("deathbans.bypass");

        getDeathban(player.getUniqueId(), new FailablePromise<Deathban>() {
            @Override
            public void success(@Nonnull Deathban deathban) {
                if (deathban.isPermanent() && !bypass) {
                    promise.failure("Your are deathbanned for the remainder of the map. Thank you for playing!");
                    return;
                }

                if ((int)(deathban.getTimeSinceCreated() / 1000) < configuration.getLifeUseDelay() && !bypass) {
                    final long remainingWait = (configuration.getLifeUseDelay() - (int)(deathban.getTimeSinceCreated() / 1000)) * 1000L;
                    promise.failure("You must wait " + Time.convertToRemaining(remainingWait) + " before using a life");
                    return;
                }

                getLives(player.getUniqueId(), new FailablePromise<LivesPlayer>() {
                    @Override
                    public void success(@Nonnull LivesPlayer livesPlayer) {
                        if (livesPlayer.getSoulboundLives() <= 0 && livesPlayer.getStandardLives() <= 0 && !bypass) {
                            promise.failure("You do not have any lives");
                            return;
                        }

                        if (livesPlayer.getSoulboundLives() > 0) {
                            livesPlayer.setSoulboundLives(livesPlayer.getSoulboundLives() - 1);
                            player.sendMessage(ChatColor.GRAY + "Consuming 1 Soulbound Life");
                        } else {
                            livesPlayer.setStandardLives(livesPlayer.getStandardLives() - 1);
                            player.sendMessage(ChatColor.GRAY + "Consuming 1 Standard Life");
                        }

                        new Scheduler(owner).async(() -> {
                            DeathbanDAO.deleteDeathban(owner.getMongo(), deathban);
                            LivesDAO.saveLivesPlayer(owner.getMongo(), livesPlayer);

                            new Scheduler(owner).sync(() -> {
                                Logger.print(player.getName() + " revived themselves");
                                promise.success();
                            }).run();
                        }).run();
                    }

                    @Override
                    public void failure(@Nonnull String reason) {
                        promise.failure("Failed to obtain your Lives Profile: " + reason);
                    }
                });
            }

            @Override
            public void failure(@Nonnull String reason) {
                promise.failure(reason);
            }
        });
    }

    public void revive(CommandSender sender, String username, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)owner.getService(ProfileService.class);
        final boolean bypass = sender.hasPermission("deathbans.bypass");

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        profileService.getProfile(username, riotProfile -> {
            if (riotProfile == null) {
                promise.failure("Player not found");
                return;
            }

            getDeathban(riotProfile.getUniqueId(), new FailablePromise<Deathban>() {
                @Override
                public void success(@Nonnull Deathban deathban) {
                    if ((int)(deathban.getTimeSinceCreated() / 1000) < configuration.getLifeUseDelay() && !bypass) {
                        final long remainingWait = (configuration.getLifeUseDelay() - (int)(deathban.getTimeSinceCreated() / 1000)) * 1000L;
                        promise.failure("You must wait " + Time.convertToRemaining(remainingWait) + " before this player can be revived");
                        return;
                    }

                    new Scheduler(owner).async(() -> {
                        final LivesPlayer senderLives = (bypass ? null : LivesDAO.getLivesPlayer(owner.getMongo(), Filters.eq("id", ((Player)sender).getUniqueId())));

                        if (!bypass) {
                            if (senderLives == null) {
                                new Scheduler(owner).sync(() -> promise.failure("Failed to obtain your lives profile")).run();
                                return;
                            }

                            if (senderLives.getStandardLives() <= 0) {
                                new Scheduler(owner).sync(() -> promise.failure("You do not have enough lives")).run();
                                return;
                            }

                            senderLives.setStandardLives(senderLives.getStandardLives() - 1);
                            LivesDAO.saveLivesPlayer(owner.getMongo(), senderLives);
                        }

                        DeathbanDAO.deleteDeathban(owner.getMongo(), deathban);

                        new Scheduler(owner).sync(() -> {
                            Logger.print(sender.getName() + " revived " + riotProfile.getUsername());
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

    public void deathban(UUID uniqueId, int seconds, boolean sotw, boolean permanent) {
        if (!getConfiguration().isDeathbanEnforced()) {
            return;
        }

        // TODO: Finish

        new Scheduler(owner).async(() -> {
            final Deathban existing = DeathbanDAO.getDeathban(owner.getMongo(), uniqueId);

            if (existing != null) {
                DeathbanDAO.deleteDeathban(owner.getMongo(), existing);
            }
        }).run();
    }

    public String getDeathbanKickMessage(Deathban deathban) {
        if (!deathban.isPermanent()) {
            return  ChatColor.RED + "You are deathbanned!" + "\n" +
                    ChatColor.GOLD + "Your deathban will expire in " + "\n" +
                    ChatColor.YELLOW + Time.convertToRemaining(deathban.getTimeUntilUndeathban()) + "\n" +
                    ChatColor.RESET + " " + "\n" +
                    ChatColor.YELLOW + "Bypass this deathban by using a life" + "\n" +
                    ChatColor.YELLOW + "Lives can be purchased at " + ChatColor.GOLD + "www.hcfrevival.net/store";
        } else {
            return ChatColor.RED + "You are deathbanned!" + "\n" +
                    ChatColor.GOLD + "You will be able to login at" + "\n" +
                    ChatColor.GOLD + "the beginning of the next map!" + "\n" +
                    ChatColor.RESET + " " + "\n" +
                    ChatColor.GREEN + "Thanks for playing!";
        }
    }
}
