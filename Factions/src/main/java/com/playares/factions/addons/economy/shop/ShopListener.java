package com.playares.factions.addons.economy.shop;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.addons.economy.EconomyAddon;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class ShopListener implements Listener {
    @Getter public final EconomyAddon addon;

    public ShopListener(EconomyAddon addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final String l1 = event.getLine(0), l2 = event.getLine(1), l3 = event.getLine(2), l4 = event.getLine(3);

        if (!player.hasPermission("economy.shop.admin")) {
            return;
        }

        if (l1.equalsIgnoreCase("buysign")) {
            if (!addon.getShopHandler().isValidItem(l2)) {
                player.sendMessage(ChatColor.RED + "Item not found");
                addon.getShopHandler().setInvalidSign(event);
                return;
            }

            final int amount;
            final double price;

            try {
                amount = Integer.parseInt(l3);
                price = Integer.parseInt(l4);
            } catch (NumberFormatException ex) {
                player.sendMessage(ChatColor.RED + "Amount/Price is invalid");
                addon.getShopHandler().setInvalidSign(event);
                return;
            }

            event.setLine(0, ChatColor.DARK_GREEN + "[Buy]");
            event.setLine(1, StringUtils.capitalize(l2.toLowerCase()));
            event.setLine(2, "" + amount);
            event.setLine(3, "$" + String.format("%.2f", price));

            Logger.print(player.getName() + " created a new buy sign for " + amount + " " + l2 + " for $" + String.format("%.2f", price));

            return;
        }

        if (l1.equalsIgnoreCase("sellsign")) {
            if (!addon.getShopHandler().isValidItem(l2)) {
                player.sendMessage(ChatColor.RED + "Item not found");
                addon.getShopHandler().setInvalidSign(event);
                return;
            }

            final int amount;
            final double price;

            try {
                amount = Integer.parseInt(l3);
                price = Integer.parseInt(l4);
            } catch (NumberFormatException ex) {
                player.sendMessage(ChatColor.RED + "Amount/Price is invalid");
                addon.getShopHandler().setInvalidSign(event);
                return;
            }

            event.setLine(0, ChatColor.RED + "[Sell]");
            event.setLine(1, StringUtils.capitalize(l2.toLowerCase()));
            event.setLine(2, "" + amount);
            event.setLine(3, "$" + String.format("%.2f", price));

            Logger.print(player.getName() + " created a new sell sign for " + amount + " " + l2 + " for $" + String.format("%.2f", price));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (block == null || !block.getType().equals(Material.WALL_SIGN) && !block.getType().equals(Material.SIGN)) {
            return;
        }

        final Sign sign = (Sign)block.getState();
        final String l1 = sign.getLine(0), l2 = sign.getLine(1), l3 = sign.getLine(2), l4 = sign.getLine(3);

        if (l1.equals(ChatColor.DARK_GREEN + "[Buy]")) {
            final CustomItem byCustomItem = getAddon().getShopHandler().getByCustomItem(l2);
            final ItemStack byMaterialName = getAddon().getShopHandler().getByName(l2.toUpperCase());
            final ItemStack byItemId = getAddon().getShopHandler().getById(l2);
            final int amount = Integer.parseInt(l3);
            final double price = Double.parseDouble(l4.replace("$", ""));

            getAddon().getHandler().getOrCreatePlayer(player.getUniqueId(), economyPlayer -> getAddon().getHandler().subtractFromBalance(player.getUniqueId(), price, new SimplePromise() {
                @Override
                public void success() {
                    if (byCustomItem != null) {
                        final ItemStack item = byCustomItem.getItem();
                        item.setAmount(amount);
                        player.getInventory().addItem(item);
                        getAddon().getShopHandler().flashPurchase(player, sign, amount, price, ChatColor.stripColor(byCustomItem.getName()));
                        player.sendMessage(ChatColor.GREEN + "Purchased " + amount + "x " + byCustomItem.getName() + ChatColor.GREEN + " for $" + String.format("%.2f", price));
                        return;
                    }

                    if (byMaterialName != null) {
                        byMaterialName.setAmount(amount);
                        player.getInventory().addItem(byMaterialName);
                        getAddon().getShopHandler().flashPurchase(player, sign, amount, price, StringUtils.capitaliseAllWords(byMaterialName.getType().name().toLowerCase().replace("_", " ")));
                        return;
                    }

                    if (byItemId != null) {
                        byItemId.setAmount(amount);
                        player.getInventory().addItem(byItemId);
                        getAddon().getShopHandler().flashPurchase(player, sign, amount, price, StringUtils.capitaliseAllWords(byItemId.getType().name().toLowerCase().replace("_", " ")));
                        return;
                    }

                    player.sendMessage(ChatColor.RED + "Failed to find item");
                }

                @Override
                public void failure(@Nonnull String reason) {
                    player.sendMessage(ChatColor.RED + reason);
                }
            }));

            return;
        }

        if (l1.equals(ChatColor.RED + "[Sell]")) {
            final CustomItemService customItemService = (CustomItemService)addon.getPlugin().getService(CustomItemService.class);
            final CustomItem byCustomItem = getAddon().getShopHandler().getByCustomItem(l2);
            final ItemStack byMaterialName = getAddon().getShopHandler().getByName(l2.toUpperCase());
            final ItemStack byItemId = getAddon().getShopHandler().getById(l2);
            final int amount = Integer.parseInt(l3);
            final double price = Double.parseDouble(l4.replace("$", ""));

            // BY CUSTOM ITEM
            if (customItemService != null && byCustomItem != null) {
                final ItemStack hand = player.getInventory().getItemInMainHand();
                final CustomItem item = customItemService.getItem(hand).orElse(null);

                // CUSTOM ITEM WAS FOUND, WE'RE NOW PROCEEDING WITH THIS ROUTE ONLY
                if (item != null) {
                    getAddon().getHandler().getOrCreatePlayer(player.getUniqueId(), economyPlayer -> {
                        // THEY HAVE LESS THAN THE AMOUNT, BUT WE WANT TO SELL AS A PORTION
                        if (hand.getAmount() <= amount) {
                            final double dollarValuePerItem = price / amount;
                            final double value = dollarValuePerItem * hand.getAmount();

                            // AMOUNT IS LESS THAN THE PORTION, TAKE IT ALL
                            player.getInventory().setItemInMainHand(null);

                            // REWARD MONEY
                            getAddon().getHandler().addToBalance(player.getUniqueId(), value, new SimplePromise() {
                                @Override
                                public void success() {
                                    getAddon().getShopHandler().flashSale(player, sign, amount, value, ChatColor.stripColor(item.getName()));
                                }

                                @Override
                                public void failure(@Nonnull String reason) {
                                    player.sendMessage(ChatColor.RED + reason);
                                }
                            });

                            return;
                        }

                        // THEY HAVE MORE THAN THE AMOUNT, ONLY SUBTRACTING FROM STACK
                        hand.setAmount(hand.getAmount() - amount);

                        getAddon().getHandler().addToBalance(player.getUniqueId(), price, new SimplePromise() {
                            @Override
                            public void success() {
                                getAddon().getShopHandler().flashSale(player, sign, amount, price, ChatColor.stripColor(item.getName()));
                            }

                            @Override
                            public void failure(@Nonnull String reason) {
                                player.sendMessage(ChatColor.RED + reason);
                            }
                        });
                    });

                    return;
                }
            }

            // BY MATERIAL NAME
            if (byMaterialName != null) {
                final ItemStack hand = player.getInventory().getItemInMainHand();

                // ITEM BY MATERIAL WAS FOUND, WE'RE NOW PROCEEDING WITH THIS ROUTE ONLY
                if (hand.getType().equals(byMaterialName.getType()) && hand.getDurability() == byMaterialName.getDurability()) {
                    getAddon().getHandler().getOrCreatePlayer(player.getUniqueId(), economyPlayer -> {
                        if (hand.getAmount() <= amount) {
                            final double dollarValuePerItem = price / amount;
                            final double value = dollarValuePerItem * hand.getAmount();

                            player.getInventory().setItemInMainHand(null);

                            getAddon().getHandler().addToBalance(player.getUniqueId(), value, new SimplePromise() {
                                @Override
                                public void success() {
                                    getAddon().getShopHandler().flashSale(player, sign, amount, value, StringUtils.capitaliseAllWords(byMaterialName.getType().name().toLowerCase().replace("_", " ")));
                                }

                                @Override
                                public void failure(@Nonnull String reason) {
                                    player.sendMessage(ChatColor.RED + reason);
                                }
                            });

                            return;
                        }

                        hand.setAmount(hand.getAmount() - amount);

                        getAddon().getHandler().addToBalance(player.getUniqueId(), price, new SimplePromise() {
                            @Override
                            public void success() {
                                getAddon().getShopHandler().flashSale(player, sign, amount, price, StringUtils.capitaliseAllWords(byMaterialName.getType().name().toLowerCase().replace("_", " ")));
                            }

                            @Override
                            public void failure(@Nonnull String reason) {
                                player.sendMessage(ChatColor.RED + reason);
                            }
                        });
                    });
                }
            }

            if (byItemId != null) {
                final ItemStack hand = player.getInventory().getItemInMainHand();

                // ITEM BY MATERIAL WAS FOUND, WE'RE NOW PROCEEDING WITH THIS ROUTE ONLY
                if (hand.getType().equals(byItemId.getType()) && hand.getData() == byItemId.getData()) {
                    getAddon().getHandler().getOrCreatePlayer(player.getUniqueId(), economyPlayer -> {
                        if (hand.getAmount() <= amount) {
                            final double dollarValuePerItem = price / amount;
                            final double value = dollarValuePerItem * hand.getAmount();

                            player.getInventory().setItemInMainHand(null);

                            getAddon().getHandler().addToBalance(player.getUniqueId(), value, new SimplePromise() {
                                @Override
                                public void success() {
                                    getAddon().getShopHandler().flashSale(player, sign, amount, value, StringUtils.capitaliseAllWords(byItemId.getType().name().toLowerCase().replace("_", " ")));
                                }

                                @Override
                                public void failure(@Nonnull String reason) {
                                    player.sendMessage(ChatColor.RED + reason);
                                }
                            });

                            return;
                        }

                        hand.setAmount(hand.getAmount() - amount);

                        getAddon().getHandler().addToBalance(player.getUniqueId(), price, new SimplePromise() {
                            @Override
                            public void success() {
                                getAddon().getShopHandler().flashSale(player, sign, amount, price, StringUtils.capitaliseAllWords(byItemId.getType().name().toLowerCase().replace("_", " ")));
                            }

                            @Override
                            public void failure(@Nonnull String reason) {
                                player.sendMessage(ChatColor.RED + reason);
                            }
                        });
                    });
                }
            }
        }
    }
}
