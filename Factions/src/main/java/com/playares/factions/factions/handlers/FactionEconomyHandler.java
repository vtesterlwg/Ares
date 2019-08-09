package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.addons.economy.EconomyAddon;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.factions.manager.FactionManager;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class FactionEconomyHandler {
    /** Owning Manager **/
    @Getter public FactionManager manager;

    public FactionEconomyHandler(FactionManager manager) {
        this.manager = manager;
    }

    /**
     * Deposits whole player balance in to faction balance
     * @param player Player
     * @param promise Promise
     */
    public void depositAll(Player player, SimplePromise promise) {
        final EconomyAddon addon = (EconomyAddon)manager.getPlugin().getAddonManager().getAddon(EconomyAddon.class);

        if (addon == null) {
            promise.failure("Failed to obtain Economy addon");
            return;
        }

        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        addon.getHandler().getOrCreatePlayer(player.getUniqueId(), economyPlayer -> {
            if (economyPlayer.getBalance() <= 0.0) {
                promise.failure("Insufficient funds");
                return;
            }

            final double toSubtract = economyPlayer.getBalance();

            addon.getHandler().subtractFromBalance(player.getUniqueId(), toSubtract, new SimplePromise() {
                @Override
                public void success() {
                    faction.setBalance(faction.getBalance() + toSubtract);
                    faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.YELLOW + " deposited " + ChatColor.GREEN + "$" + String.format("%.2f", toSubtract) + ChatColor.YELLOW + " in to the faction balance");

                    promise.success();
                }

                @Override
                public void failure(@Nonnull String reason) {
                    promise.failure(reason);
                }
            });
        });
    }

    /**
     * Withdraws all faction balance in to the player balance
     * @param player Player
     * @param promise Promise
     */
    public void withdrawAll(Player player, SimplePromise promise) {
        final EconomyAddon addon = (EconomyAddon)manager.getPlugin().getAddonManager().getAddon(EconomyAddon.class);

        if (addon == null) {
            promise.failure("Failed to obtain Economy addon");
            return;
        }

        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !player.hasPermission("factions.admin")) {
            promise.failure("You must be an officer or higher to perform this action");
            return;
        }

        final double toAdd = faction.getBalance();

        addon.getHandler().addToBalance(player.getUniqueId(), toAdd, new SimplePromise() {
            @Override
            public void success() {
                faction.setBalance(0.0);
                faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.YELLOW + " withdrew " + ChatColor.GREEN + "$" + String.format("%.2f", toAdd) + ChatColor.YELLOW + " from the faction balance");
                promise.success();
            }

            @Override
            public void failure(@Nonnull String reason) {
                promise.failure(reason);
            }
        });
    }

    /**
     * Deposits a defined amount in to the faction balance
     * @param player Player
     * @param amount Amount
     * @param promise Promise
     */
    public void deposit(Player player, double amount, SimplePromise promise) {
        final EconomyAddon addon = (EconomyAddon)manager.getPlugin().getAddonManager().getAddon(EconomyAddon.class);

        if (addon == null) {
            promise.failure("Failed to obtain Economy addon");
            return;
        }

        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        addon.getHandler().getOrCreatePlayer(player.getUniqueId(), economyPlayer -> {
            if (economyPlayer.getBalance() <= amount) {
                promise.failure("Insufficient funds");
                return;
            }

            addon.getHandler().subtractFromBalance(player.getUniqueId(), amount, new SimplePromise() {
                @Override
                public void success() {
                    faction.setBalance(faction.getBalance() + amount);
                    faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.YELLOW + " deposited " + ChatColor.GREEN + "$" + String.format("%.2f", amount) + ChatColor.YELLOW + " in to the faction balance");

                    promise.success();
                }

                @Override
                public void failure(@Nonnull String reason) {
                    promise.failure(reason);
                }
            });
        });
    }

    /**
     * Withdraws a defined amount from the faction balance
     * @param player Player
     * @param amount Amount
     * @param promise Promise
     */
    public void withdraw(Player player, double amount, SimplePromise promise) {
        final EconomyAddon addon = (EconomyAddon)manager.getPlugin().getAddonManager().getAddon(EconomyAddon.class);

        if (addon == null) {
            promise.failure("Failed to obtain Economy addon");
            return;
        }

        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER)) {
            promise.failure("You must be an officer or higher to perform this action");
            return;
        }

        if (faction.getBalance() < amount) {
            promise.failure("Faction balance insufficient");
            return;
        }

        addon.getHandler().addToBalance(player.getUniqueId(), amount, new SimplePromise() {
            @Override
            public void success() {
                faction.setBalance(faction.getBalance() - amount);
                faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.YELLOW + " withdrew " + ChatColor.GREEN + "$" + String.format("%.2f", amount) + ChatColor.YELLOW + " from the faction balance");
                promise.success();
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
       });
    }
}
