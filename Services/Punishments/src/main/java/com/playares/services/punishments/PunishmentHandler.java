package com.playares.services.punishments;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.data.Punishment;
import com.playares.services.punishments.data.PunishmentDAO;
import com.playares.services.punishments.data.PunishmentType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class PunishmentHandler {
    @Getter
    public PunishmentService service;

    public PunishmentHandler(PunishmentService service) {
        this.service = service;
    }

    public void ban(CommandSender sender, String name, String reason, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)service.getOwner().getService(ProfileService.class);
        final UUID creatorId = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
        final String creatorName = sender.getName();

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(service.getOwner()).async(() ->
                profileService.getProfile(name, profile ->
                        new Scheduler(service.getOwner()).sync(() -> {

                            if (profile == null) {
                                promise.failure("Player not found");
                                return;
                            }

                            final Punishment punishment = new Punishment(
                                    PunishmentType.BAN,
                                    profile.getUniqueId(),
                                    creatorId,
                                    profile.getAddress(),
                                    reason,
                                    0L);

                            new Scheduler(service.getOwner()).async(() -> PunishmentDAO.savePunishment(getService().getOwner().getMongo(), punishment)).run();

                            Logger.print(profile.getUsername() + " has been banned by " + creatorName + "(" + creatorId + ")");

                            new Scheduler(service.getOwner()).sync(() -> {
                                final Player toKick = Bukkit.getPlayer(profile.getUniqueId());

                                if (toKick != null) {
                                    toKick.kickPlayer(service.getPunishmentManager().getKickMessage(punishment));
                                }

                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (!player.hasPermission("punishments.view")) {
                                        continue;
                                    }

                                    player.sendMessage(ChatColor.GRAY + profile.getUsername() + " has been banned by " + creatorName);
                                }

                                promise.success();
                            }).run();
        }).run())).run();
    }

    public void tempban(CommandSender sender, String name, String time, String reason, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)service.getOwner().getService(ProfileService.class);
        final UUID creatorId = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
        final String creatorName = sender.getName();
        final long duration;

        try {
            duration = Time.parseTime(time);
        } catch (NumberFormatException ex) {
            promise.failure("Invalid time format");
            return;
        }

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(service.getOwner()).async(() ->
                profileService.getProfile(name, profile ->
                        new Scheduler(service.getOwner()).sync(() -> {

                            if (profile == null) {
                                promise.failure("Player not found");
                                return;
                            }

                            final Punishment punishment = new Punishment(
                                    PunishmentType.BAN,
                                    profile.getUniqueId(),
                                    creatorId,
                                    profile.getAddress(),
                                    reason,
                                    Time.now() + duration);

                            new Scheduler(service.getOwner()).async(() -> PunishmentDAO.savePunishment(getService().getOwner().getMongo(), punishment)).run();

                            Logger.print(profile.getUsername() + " has been temp-banned by " + creatorName + "(" + creatorId + ") for " + Time.convertToRemaining(duration));

                            new Scheduler(service.getOwner()).sync(() -> {
                                final Player toKick = Bukkit.getPlayer(profile.getUniqueId());

                                if (toKick != null) {
                                    toKick.kickPlayer(service.getPunishmentManager().getKickMessage(punishment));
                                }

                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (!player.hasPermission("punishments.view")) {
                                        continue;
                                    }

                                    player.sendMessage(ChatColor.GRAY + profile.getUsername() + " has been temp-banned by " + creatorName + " for " + Time.convertToRemaining(duration));
                                }

                                promise.success();
                            }).run();
                        }).run())).run();
    }

    public void mute(CommandSender sender, String name, String reason, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)service.getOwner().getService(ProfileService.class);
        final UUID creatorId = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
        final String creatorName = sender.getName();

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(service.getOwner()).async(() ->
                profileService.getProfile(name, profile ->
                        new Scheduler(service.getOwner()).sync(() -> {

                            if (profile == null) {
                                promise.failure("Player not found");
                                return;
                            }

                            final Punishment punishment = new Punishment(
                                    PunishmentType.MUTE,
                                    profile.getUniqueId(),
                                    creatorId,
                                    profile.getAddress(),
                                    reason,
                                    0L);

                            new Scheduler(service.getOwner()).async(() -> PunishmentDAO.savePunishment(getService().getOwner().getMongo(), punishment)).run();

                            Logger.print(profile.getUsername() + " has been muted by " + creatorName + "(" + creatorId + ")");

                            new Scheduler(service.getOwner()).sync(() -> {
                                final Player muted = Bukkit.getPlayer(profile.getUniqueId());

                                if (muted != null) {
                                    muted.sendMessage(service.getPunishmentManager().getMuteMessage(punishment));
                                }

                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (!player.hasPermission("punishments.view")) {
                                        continue;
                                    }

                                    player.sendMessage(ChatColor.GRAY + profile.getUsername() + " has been muted by " + creatorName);
                                }

                                promise.success();
                            }).run();
                        }).run())).run();
    }

    public void tempmute(CommandSender sender, String name, String time, String reason, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)service.getOwner().getService(ProfileService.class);
        final UUID creatorId = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
        final String creatorName = sender.getName();
        final long duration;

        try {
            duration = Time.parseTime(time);
        } catch (NumberFormatException ex) {
            promise.failure("Invalid time format");
            return;
        }

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(service.getOwner()).async(() ->
                profileService.getProfile(name, profile ->
                        new Scheduler(service.getOwner()).sync(() -> {

                            if (profile == null) {
                                promise.failure("Player not found");
                                return;
                            }

                            final Punishment punishment = new Punishment(
                                    PunishmentType.MUTE,
                                    profile.getUniqueId(),
                                    creatorId,
                                    profile.getAddress(),
                                    reason,
                                    Time.now() + duration);

                            new Scheduler(service.getOwner()).async(() -> PunishmentDAO.savePunishment(getService().getOwner().getMongo(), punishment)).run();

                            Logger.print(profile.getUsername() + " has been temp-muted by " + creatorName + "(" + creatorId + ") for " + Time.convertToRemaining(duration));

                            new Scheduler(service.getOwner()).sync(() -> {
                                final Player muted = Bukkit.getPlayer(profile.getUniqueId());

                                if (muted != null) {
                                    muted.sendMessage(service.getPunishmentManager().getMuteMessage(punishment));
                                }

                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (!player.hasPermission("punishments.view")) {
                                        continue;
                                    }

                                    player.sendMessage(ChatColor.GRAY + profile.getUsername() + " has been temp-muted by " + creatorName + " for " + Time.convertToRemaining(duration));
                                }

                                promise.success();
                            }).run();
                        }).run())).run();
    }

    public void blacklist(CommandSender sender, String name, String reason, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)service.getOwner().getService(ProfileService.class);
        final UUID creatorId = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
        final String creatorName = sender.getName();

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(service.getOwner()).async(() ->
                profileService.getProfile(name, profile ->
                        new Scheduler(service.getOwner()).sync(() -> {

                            if (profile == null) {
                                promise.failure("Player not found");
                                return;
                            }

                            final Punishment punishment = new Punishment(
                                    PunishmentType.BLACKLIST,
                                    profile.getUniqueId(),
                                    creatorId,
                                    profile.getAddress(),
                                    reason,
                                    0L);

                            new Scheduler(service.getOwner()).async(() -> PunishmentDAO.savePunishment(getService().getOwner().getMongo(), punishment)).run();

                            Logger.print(profile.getUsername() + " has been blacklisted by " + creatorName + "(" + creatorId + ")");

                            new Scheduler(service.getOwner()).sync(() -> {
                                final Player toKick = Bukkit.getPlayer(profile.getUniqueId());

                                if (toKick != null) {
                                    toKick.kickPlayer(service.getPunishmentManager().getKickMessage(punishment));
                                }

                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (!player.hasPermission("punishments.view")) {
                                        continue;
                                    }

                                    player.sendMessage(ChatColor.GRAY + profile.getUsername() + " has been blacklisted by " + creatorName);
                                }

                                promise.success();
                            }).run();
                        }).run())).run();
    }

    public void unban(CommandSender sender, String name, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)service.getOwner().getService(ProfileService.class);
        final String senderName = sender.getName();

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(service.getOwner()).async(() -> profileService.getProfile(name, profile -> {
            if (profile == null) {
                new Scheduler(getService().getOwner()).sync(() -> promise.failure("Player not found")).run();
                return;
            }

            final List<Punishment> bans = service.getPunishmentManager().getActivePunishments(profile.getUniqueId(), profile.getAddress(), PunishmentType.BAN);

            if (bans.isEmpty()) {
                new Scheduler(getService().getOwner()).sync(() -> promise.failure("Player is not banned")).run();
                return;
            }

            bans.forEach(ban -> {
                ban.setAppealed(true);
                PunishmentDAO.savePunishment(getService().getOwner().getMongo(), ban);
            });

            new Scheduler(service.getOwner()).sync(() -> {
                Logger.print(senderName + " unbanned " + profile.getUsername());

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("punishments.view")) {
                        continue;
                    }

                    player.sendMessage(ChatColor.GRAY + profile.getUsername() + " has been unbanned by " + senderName);
                }

                promise.success();
            }).run();
        })).run();
    }

    public void unmute(CommandSender sender, String name, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)service.getOwner().getService(ProfileService.class);
        final String senderName = sender.getName();

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(service.getOwner()).async(() -> profileService.getProfile(name, profile -> {
            if (profile == null) {
                new Scheduler(getService().getOwner()).sync(() -> promise.failure("Player not found")).run();
                return;
            }

            final List<Punishment> mutes = service.getPunishmentManager().getActivePunishments(profile.getUniqueId(), profile.getAddress(), PunishmentType.MUTE);

            if (mutes.isEmpty()) {
                new Scheduler(getService().getOwner()).sync(() -> promise.failure("Player is not muted")).run();
                return;
            }

            mutes.forEach(mute -> {
                mute.setAppealed(true);
                PunishmentDAO.savePunishment(getService().getOwner().getMongo(), mute);
            });

            new Scheduler(service.getOwner()).sync(() -> {
                Logger.print(senderName + " unmuted " + profile.getUsername());

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("punishments.view")) {
                        continue;
                    }

                    player.sendMessage(ChatColor.GRAY + profile.getUsername() + " has been unmuted by " + senderName);
                }

                promise.success();
            }).run();
        })).run();
    }

    public void unblacklist(CommandSender sender, String name, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)service.getOwner().getService(ProfileService.class);
        final String senderName = sender.getName();

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(service.getOwner()).async(() -> profileService.getProfile(name, profile -> {
            if (profile == null) {
                new Scheduler(getService().getOwner()).sync(() -> promise.failure("Player not found")).run();
                return;
            }

            final List<Punishment> blacklists = service.getPunishmentManager().getActivePunishments(profile.getUniqueId(), profile.getAddress(), PunishmentType.BLACKLIST);

            if (blacklists.isEmpty()) {
                new Scheduler(getService().getOwner()).sync(() -> promise.failure("Player is not blacklisted")).run();
                return;
            }

            blacklists.forEach(blacklist -> {
                blacklist.setAppealed(true);
                PunishmentDAO.savePunishment(getService().getOwner().getMongo(), blacklist);
            });

            new Scheduler(service.getOwner()).sync(() -> {
                Logger.print(senderName + " unblacklisted " + profile.getUsername());

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("punishments.view")) {
                        continue;
                    }

                    player.sendMessage(ChatColor.GRAY + profile.getUsername() + " has been unblacklisted by " + senderName);
                }

                promise.success();
            }).run();
        })).run();
    }
}