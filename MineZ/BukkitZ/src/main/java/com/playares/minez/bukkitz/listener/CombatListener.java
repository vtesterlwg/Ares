package com.playares.minez.bukkitz.listener;

import com.playares.minez.bukkitz.MineZ;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public final class CombatListener implements Listener {
    @Getter public final MineZ plugin;

    public CombatListener(MineZ plugin) {
        this.plugin = plugin;
    }

    @EventHandler /* Stopping health regen from being full hunger */
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.EATING) ||
            event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) {
            event.setCancelled(true);
        }
    }

    @EventHandler /* Adds 0.5 health to player when consuming food */
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final double preFoodLevel = player.getFoodLevel();
        final double postFoodLevel = event.getFoodLevel();

        if (postFoodLevel > preFoodLevel && player.getHealth() < 20.0) {
            player.setHealth(player.getHealth() + 1.0);
        }
    }
}
