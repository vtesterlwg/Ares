package com.playares.services.ranks.data;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.ranks.RankService;
import lombok.Getter;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.manager.UserManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class RankDataHandler {
    @Getter public RankService service;

    public RankDataHandler(RankService service) {
        this.service = service;
    }

    public void setName(CommandSender sender, Rank rank, String name) {
        rank.setName(name);

        save(rank, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Rank name has been updated to " + name);
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    public void setDisplayName(CommandSender sender, Rank rank, String name) {
        rank.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        save(rank, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Rank display name has been updated to " + rank.getDisplayName());
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    public void setPrefix(CommandSender sender, Rank rank, String prefix) {
        rank.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));

        save(rank, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Rank prefix has been updated to " + rank.getPrefix());
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    public void setPermission(CommandSender sender, Rank rank, String permission) {
        rank.setPermission(permission);

        save(rank, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Rank permission has been updated to " + rank.getPermission());
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    public void setWeight(CommandSender sender, Rank rank, String weightValue) {
        int weight;

        try {
            weight = Integer.parseInt(weightValue);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Rank weight must be a whole number value");
            return;
        }

        rank.setWeight(weight);

        save(rank, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Rank weight has been updated to " + rank.getWeight());
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    public void setStaff(CommandSender sender, Rank rank, String value) {
        if (value.equalsIgnoreCase("true")) {
            rank.setStaff(true);
        } else if (value.equalsIgnoreCase("false")) {
            rank.setStaff(false);
        } else {
            sender.sendMessage(ChatColor.RED + "Rank staff value must be true or false");
            return;
        }

        save(rank, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Rank staff level has been updated to " + rank.isStaff());
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    public void setDefault(CommandSender sender, Rank rank, String value) {
        if (value.equalsIgnoreCase("true")) {
            rank.setEveryone(true);
        } else if (value.equalsIgnoreCase("false")) {
            rank.setEveryone(false);
        } else {
            sender.sendMessage(ChatColor.RED + "Rank default value must be true or false");
            return;
        }

        save(rank, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Rank default value has been updated to " + rank.isEveryone());
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    public void create(CommandSender sender, String name) {
        if (getService().getRankByName(name) != null) {
            sender.sendMessage(ChatColor.RED + "Rank name is already in use");
            return;
        }

        final Rank rank = new Rank(name);
        getService().getRanks().add(rank);

        new Scheduler(service.getOwner()).async(() -> {
            RankDAO.create(service.getOwner().getMongo(), rank);

            new Scheduler(service.getOwner()).sync(() -> sender.sendMessage(ChatColor.GREEN + "Rank " + rank.getName() + " has been created")).run();
        }).run();
    }

    public void delete(CommandSender sender, String name) {
        final Rank rank = service.getRankByName(name);

        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Rank not found");
            return;
        }

        new Scheduler(service.getOwner()).async(() -> {
            RankDAO.delete(service.getOwner().getMongo(), rank);

            new Scheduler(service.getOwner()).sync(() -> sender.sendMessage(ChatColor.GREEN + "Rank " + rank.getName() + " has been deleted")).run();
        }).run();
    }

    public void applyRank(CommandSender sender, String username, String rankName) {
        final Rank rank = service.getRankByName(rankName);

        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Rank not found");
            return;
        }

        if (service.getLuckPerms() == null) {
            Logger.error("Attempted to remove a rank but LuckPermsApi was not found");
            return;
        }

        final UserManager userManager = service.getLuckPerms().getUserManager();

        new Scheduler(getService().getOwner()).async(() -> {
            final UUID uniqueId = userManager.lookupUuid(username).join();
            final User user = (uniqueId != null) ? userManager.loadUser(uniqueId).join() : null;

            new Scheduler(getService().getOwner()).sync(() -> {
                if (user == null) {
                    sender.sendMessage("Player not found");
                    Logger.error("Failed to apply rank to " + username + ", user was not found");
                    return;
                }

                user.setPermission(getService().getLuckPerms().getNodeFactory().newBuilder(rank.getPermission()).build());
                userManager.saveUser(user);

                sender.sendMessage(rank.getDisplayName() + ChatColor.GREEN + " has been applied to " + username);
                Logger.print("Applied rank '" + rank.getName() + "' to " + username);
            }).run();
        }).run();
    }

    public void removeRank(CommandSender sender, String username, String rankName) {
        final Rank rank = service.getRankByName(rankName);

        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Rank not found");
            return;
        }

        if (service.getLuckPerms() == null) {
            Logger.error("Attempted to remove a rank but LuckPermsApi was not found");
            return;
        }

        final User permUser = service.getLuckPerms().getUser(username);

        if (permUser == null) {
            Logger.error("Failed to obtain LuckPerms user for player " + username);
            return;
        }

        new Scheduler(getService().getOwner()).async(() -> {
            permUser.unsetPermission(service.getLuckPerms().getNodeFactory().newBuilder(rank.getPermission()).build());

            new Scheduler(getService().getOwner()).sync(() -> {
                sender.sendMessage(rank.getDisplayName() + ChatColor.GREEN + " has been removed from " + username);
                Logger.print("Removed rank '" + rank.getName() + "' from " + username);
            });
        }).run();
    }

    private void save(Rank rank, SimplePromise promise) {
        new Scheduler(service.getOwner()).async(() -> {
            RankDAO.update(service.getOwner().getMongo(), rank);

            new Scheduler(service.getOwner()).sync(promise::success).run();
        }).run();
    }
}