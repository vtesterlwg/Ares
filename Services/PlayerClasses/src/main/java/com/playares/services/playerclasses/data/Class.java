package com.playares.services.playerclasses.data;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public abstract class Class {
    @Getter public Map<PotionEffectType, Integer> passiveEffects;

    public void activate(Player player) {

    }

    public void deactive(Player player) {

    }
}
