package com.playares.services.ranks.data;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.ranks.RankService;
import lombok.Getter;
import me.lucko.luckperms.api.Group;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.manager.GroupManager;
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
        if (getService().getLuckPerms() == null) {
            sender.sendMessage(ChatColor.RED + "LuckPermsApi not found");
            Logger.error("Attempted to apply a rank but LuckPermsApi was not found");
            return;
        }

        final GroupManager groupManager = getService().getLuckPerms().getGroupManager();
        final Group group = groupManager.getGroup(rank.getName());

        if (group == null) {
            sender.sendMessage(ChatColor.RED + "Failed to find group matching rank '" + rank.getName() + "'");
            return;
        }

        new Scheduler(service.getOwner()).async(() -> {
            Group newGroup;

            newGroup = groupManager.createAndLoadGroup(name).join();
            groupManager.deleteGroup(group).join();

            for (Node node : group.getAllNodes()) {
                newGroup.setPermission(node);
            }

            Logger.print("Transferred " + group.getAllNodes().size() + " nodes to " + newGroup.getName());

            groupManager.saveGroup(newGroup);
        }).run();

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
        final GroupManager groupManager = getService().getLuckPerms().getGroupManager();
        final Group group = groupManager.getGroup(rank.getName());

        if (group != null) {
            Node toRemove = null;

            for (Node node : group.getPermissions()) {
                if (rank.getPermission() != null) {
                    if (node.getPermission().equals(rank.getPermission())) {
                        toRemove = node;
                        break;
                    }
                } else {
                    if (node.getPermission().equals("rank." +rank.getName())) {
                        toRemove = node;
                        break;
                    }
                }
            }

            if (toRemove != null) {
                group.unsetPermission(toRemove);
                Logger.warn("Unset old rank permission for " + group.getName());
            }

            group.setPermission(getService().getLuckPerms().getNodeFactory().newBuilder(permission).build());
            Logger.print("Updated permission for " + rank.getName() + " to use permission " + permission);
        }

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
        if (service.getLuckPerms() == null) {
            Logger.error("Attempted to create a rank but LuckPermsApi was not found");
            return;
        }

        if (getService().getRankByName(name) != null) {
            sender.sendMessage(ChatColor.RED + "Rank name is already in use");
            return;
        }

        final GroupManager groupManager = getService().getLuckPerms().getGroupManager();
        final Rank rank = new Rank(name);
        final Group group = groupManager.getGroup(rank.getName());

        getService().getRanks().add(rank);

        new Scheduler(service.getOwner()).async(() -> {
            if (group == null) {
                Logger.print("Creating new LuckPerms group for new rank '" + rank.getName() + "'");
                groupManager.createAndLoadGroup(rank.getName());

                final Group created = groupManager.getGroup(rank.getName());

                if (created != null) {
                    created.setPermission(getService().getLuckPerms().getNodeFactory().newBuilder("rank." + rank.getName()).build());
                }
            }

            RankDAO.create(service.getOwner().getMongo(), rank);
            new Scheduler(service.getOwner()).sync(() -> sender.sendMessage(ChatColor.GREEN + "Rank " + rank.getName() + " has been created")).run();
        }).run();
    }

    public void delete(CommandSender sender, String name) {
        if (service.getLuckPerms() == null) {
            Logger.error("Attempted to create a rank but LuckPermsApi was not found");
            return;
        }

        final GroupManager groupManager = getService().getLuckPerms().getGroupManager();
        final Rank rank = service.getRankByName(name);

        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Rank not found");
            return;
        }

        final Group group = groupManager.getGroup(rank.getName());

        service.getRanks().remove(rank);

        new Scheduler(service.getOwner()).async(() -> {
            if (group == null) {
                Logger.print("LuckPerms group did not exist for " + name);
            } else {
                groupManager.deleteGroup(group).join();
                Logger.warn("Deleted LuckPerms group '" + group.getName() + "'");
            }

            RankDAO.delete(service.getOwner().getMongo(), rank);
            new Scheduler(service.getOwner()).sync(() -> sender.sendMessage(ChatColor.GREEN + "Rank " + rank.getName() + " has been deleted")).run();
            Logger.warn("Rank '" + rank.getName() + "' has been deleted");
        }).run();
    }

    public void applyRank(CommandSender sender, String username, String rankName) {
        final Rank rank = service.getRankByName(rankName);

        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Rank not found");
            return;
        }

        if (service.getLuckPerms() == null) {
            Logger.error("Attempted to apply a rank but LuckPermsApi was not found");
            return;
        }

        final UserManager userManager = service.getLuckPerms().getUserManager();
        final Group group = service.getLuckPerms().getGroupManager().getGroup(rank.getName());

        if (group == null) {
            sender.sendMessage(ChatColor.RED + "LuckPerms group not found");
            return;
        }

        new Scheduler(getService().getOwner()).async(() -> {
            final UUID uniqueId = userManager.lookupUuid(username).join();
            final User user = (uniqueId != null) ? userManager.loadUser(uniqueId).join() : null;

            new Scheduler(getService().getOwner()).sync(() -> {
                if (user == null) {
                    sender.sendMessage("Player not found");
                    Logger.error("Failed to apply rank to " + username + ", user was not found");
                    return;
                }

                user.setPermission(getService().getLuckPerms().getNodeFactory().makeGroupNode(group).build());
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

        final UserManager userManager = service.getLuckPerms().getUserManager();
        final GroupManager groupManager = service.getLuckPerms().getGroupManager();

        new Scheduler(getService().getOwner()).async(() -> {
            final UUID uniqueId = userManager.lookupUuid(username).join();
            final User user = (uniqueId != null) ? userManager.loadUser(uniqueId).join() : null;
            final Group group = groupManager.getGroup(rank.getName());

            new Scheduler(getService().getOwner()).sync(() -> {
                if (user == null) {
                    sender.sendMessage("Player not found");
                    Logger.error("Failed to remove rank from " + username + ", user was not found");
                    return;
                }

                if (group == null) {
                    sender.sendMessage("Group not found");
                    Logger.error("Failed to remove rank from " + user + ", group was not found");
                    return;
                }

                for (Node node : user.getPermissions()) {
                    if (node.getPermission().equalsIgnoreCase("group." + group.getName())) {
                        user.unsetPermission(node);
                        Logger.print("Successfully removed group permission for " + username);
                        break;
                    }
                }

                userManager.saveUser(user);
                sender.sendMessage(rank.getDisplayName() + ChatColor.GREEN + " has been removed from " + username);
                Logger.print("Removed rank '" + rank.getName() + "' from " + username);
            }).run();
        }).run();
    }

    private void save(Rank rank, SimplePromise promise) {
        new Scheduler(service.getOwner()).async(() -> {
            RankDAO.update(service.getOwner().getMongo(), rank);

            new Scheduler(service.getOwner()).sync(promise::success).run();
        }).run();
    }
}