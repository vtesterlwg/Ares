package com.riotmc.services.essentials;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.service.RiotService;
import com.riotmc.services.essentials.command.*;
import com.riotmc.services.essentials.data.kit.Kit;
import com.riotmc.services.essentials.data.kit.KitHandler;
import com.riotmc.services.essentials.data.kit.KitManager;
import com.riotmc.services.essentials.data.warp.Warp;
import com.riotmc.services.essentials.data.warp.WarpHandler;
import com.riotmc.services.essentials.data.warp.WarpManager;
import com.riotmc.services.essentials.vanish.VanishHandler;
import com.riotmc.services.essentials.vanish.VanishManager;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public final class EssentialsService implements RiotService {
    @Getter public final RiotPlugin owner;
    @Getter protected KitManager kitManager;
    @Getter protected KitHandler kitHandler;
    @Getter protected WarpManager warpManager;
    @Getter protected WarpHandler warpHandler;
    @Getter protected VanishManager vanishManager;
    @Getter protected VanishHandler vanishHandler;
    @Getter protected FileConfiguration essentialsConfig;

    public EssentialsService(RiotPlugin owner) {
        this.owner = owner;
    }

    public void start() {
        this.kitManager = new KitManager(getOwner());
        this.kitHandler = new KitHandler(this);
        this.warpManager = new WarpManager(getOwner());
        this.warpHandler = new WarpHandler(this);
        this.vanishManager = new VanishManager();
        this.vanishHandler = new VanishHandler(this);
        this.essentialsConfig = getOwner().getConfig("essentials");

        getOwner().getCommandManager().getCommandCompletions().registerAsyncCompletion("potions", c -> {
            final List<String> potions = Lists.newArrayList();

            for (PotionEffectType type : PotionEffectType.values()) {
                if (type == null) {
                    continue;
                }

                potions.add(type.getName().toLowerCase());
            }

            return ImmutableList.copyOf(potions);
        });

        getOwner().getCommandManager().getCommandCompletions().registerAsyncCompletion("enchantments", c -> {
           final List<String> enchants = Lists.newArrayList();

           for (Enchantment enchant : Enchantment.values()) {
               enchants.add(enchant.getKey().getKey().toLowerCase());
           }

           return ImmutableList.copyOf(enchants);
        });

        getOwner().getCommandManager().getCommandCompletions().registerAsyncCompletion("items", c -> {
           final List<String> items = Lists.newArrayList();

           for (Material material : Material.values()) {
               if (material.name().startsWith("LEGACY_")) {
                   continue;
               }

               items.add(material.name().toLowerCase());
           }

           return ImmutableList.copyOf(items);
        });

        getOwner().getCommandManager().getCommandCompletions().registerAsyncCompletion("kits", c -> {
            final List<String> kits = Lists.newArrayList();

            for (Kit kit : kitManager.getKits()) {
                kits.add(kit.getName());
            }

            return ImmutableList.copyOf(kits);
        });

        getOwner().getCommandManager().getCommandCompletions().registerAsyncCompletion("warps", c -> {
            final List<String> warps = Lists.newArrayList();

            for (Warp warp : warpManager.getWarps()) {
                warps.add(warp.getName());
            }

            return ImmutableList.copyOf(warps);
        });

        registerCommand(new GamemodeCommand());
        registerCommand(new HealCommand());
        registerCommand(new ClearCommand());
        registerCommand(new EffectCommand());
        registerCommand(new EnchantCommand());
        registerCommand(new RenameCommand());
        registerCommand(new RepairCommand());
        registerCommand(new ShopCommand());
        registerCommand(new TeleportCommand());
        registerCommand(new WeatherCommand());
        registerCommand(new ItemCommand());
        registerCommand(new InvSeeCommand(owner));
        registerCommand(new ListCommand(owner));
        registerCommand(new WarpCommand(this));
        registerCommand(new KitCommand(this));
        registerCommand(new BroadcastCommand(this));
        registerCommand(new VanishCommand(this));
    }

    public void stop() {
        kitManager.getKits().clear();
        warpManager.getWarps().clear();
        vanishManager.getVanished().clear();
    }

    public String getName() {
        return "Essentials";
    }
}
