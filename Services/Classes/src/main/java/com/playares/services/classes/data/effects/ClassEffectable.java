package com.playares.services.classes.data.effects;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

public interface ClassEffectable {
    Material getMaterial();

    PotionEffect getEffect();

    int getCooldown();
}
