package com.riotmc.services.humbug.features.cont;

import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public final class CustomRecipes implements HumbugModule {
    @Getter
    public final HumbugService humbug;

    @Getter @Setter
    public boolean enabled;

    @Getter @Setter
    public boolean xpBottleEnabled;

    @Getter @Setter
    public boolean horseArmorEnabled;

    @Getter @Setter
    public boolean saddleEnabled;

    @Getter @Setter
    public boolean glistMelonEnabled;

    public CustomRecipes(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("custom-recipes.enabled");
        this.xpBottleEnabled = humbug.getHumbugConfig().getBoolean("custom-recipes.xp-bottle-enabled");
        this.horseArmorEnabled = humbug.getHumbugConfig().getBoolean("custom-recipes.horse-armor-enabled");
        this.saddleEnabled = humbug.getHumbugConfig().getBoolean("custom-recipes.saddle-enabled");
        this.glistMelonEnabled = humbug.getHumbugConfig().getBoolean("custom-recipes.glist-melon-enabled");
    }

    @Override
    public String getName() {
        return "Custom Recipes";
    }

    @Override
    public void start() {
        if (!isEnabled()) {
            return;
        }

        final ShapedRecipe xpBottleRecipe = new ShapedRecipe(new NamespacedKey(getHumbug().getOwner(),"ARES.EXP_BOTTLE"), new ItemStack(Material.EXPERIENCE_BOTTLE));
        final ShapedRecipe ironHorseRecipe = new ShapedRecipe(new NamespacedKey(getHumbug().getOwner(), "ARES.IRON_HORSE_ARMOR"), new ItemStack(Material.IRON_HORSE_ARMOR));
        final ShapedRecipe goldHorseRecipe = new ShapedRecipe(new NamespacedKey(getHumbug().getOwner(), "ARES.GOLDEN_HORSE_ARMOR"), new ItemStack(Material.GOLDEN_HORSE_ARMOR));
        final ShapedRecipe diamondHorseRecipe = new ShapedRecipe(new NamespacedKey(getHumbug().getOwner(), "ARES.DIAMOND_HORSE_ARMOR"), new ItemStack(Material.DIAMOND_HORSE_ARMOR));
        final ShapedRecipe saddleRecipe = new ShapedRecipe(new NamespacedKey(getHumbug().getOwner(), "ARES.SADDLE"), new ItemStack(Material.SADDLE));
        final ShapedRecipe glistMelonRecipe = new ShapedRecipe(new NamespacedKey(getHumbug().getOwner(), "ARES.GLIST_MELON"), new ItemStack(Material.GLISTERING_MELON_SLICE));

        xpBottleRecipe.shape("*");
        xpBottleRecipe.setIngredient('*', Material.EMERALD);

        ironHorseRecipe.shape("ASI", "BBL", "LLA");
        ironHorseRecipe
                .setIngredient('A', Material.AIR)
                .setIngredient('S', Material.SADDLE)
                .setIngredient('I', Material.IRON_INGOT)
                .setIngredient('B', Material.IRON_BLOCK)
                .setIngredient('L', Material.LEATHER);

        goldHorseRecipe.shape("ASG", "BBL", "LLA");
        goldHorseRecipe
                .setIngredient('A', Material.AIR)
                .setIngredient('S', Material.SADDLE)
                .setIngredient('G', Material.GOLD_INGOT)
                .setIngredient('B', Material.GOLD_BLOCK)
                .setIngredient('L', Material.LEATHER);

        diamondHorseRecipe.shape("ASD", "BBL", "LLA");
        diamondHorseRecipe
                .setIngredient('A', Material.AIR)
                .setIngredient('S', Material.SADDLE)
                .setIngredient('D', Material.DIAMOND)
                .setIngredient('B', Material.DIAMOND_BLOCK)
                .setIngredient('L', Material.LEATHER);

        saddleRecipe.shape("AAL", "LLL", "RAR");
        saddleRecipe
                .setIngredient('A', Material.AIR)
                .setIngredient('L', Material.LEATHER)
                .setIngredient('R', Material.LEAD);

        glistMelonRecipe.shape("MG");
        glistMelonRecipe
                .setIngredient('M', Material.MELON_SLICE)
                .setIngredient('G', Material.GOLD_NUGGET);

        // TODO: Find a better way to do this
        Bukkit.resetRecipes();

        if (xpBottleEnabled) {
            Bukkit.addRecipe(xpBottleRecipe);
        }

        if (horseArmorEnabled) {
            Bukkit.addRecipe(ironHorseRecipe);
            Bukkit.addRecipe(goldHorseRecipe);
            Bukkit.addRecipe(diamondHorseRecipe);
        }

        if (saddleEnabled) {
            Bukkit.addRecipe(saddleRecipe);
        }

        if (glistMelonEnabled) {
            Bukkit.addRecipe(glistMelonRecipe);
        }
    }

    @Override
    public void stop() {}
}