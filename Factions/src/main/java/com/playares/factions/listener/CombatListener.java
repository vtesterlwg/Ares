package com.playares.factions.listener;

import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.event.PlayerLingeringSplashPlayerEvent;
import com.playares.commons.bukkit.event.PlayerSplashPlayerEvent;
import com.playares.factions.Factions;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.factions.ServerFaction;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class CombatListener implements Listener {
    @Getter
    public final Factions plugin;

    public CombatListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerAttackPlayer(PlayerDamagePlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        final FactionPlayer attackerProfile = plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null) {
            attacker.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            event.setCancelled(true);
            return;
        }

        if (attackedProfile == null) {
            attacker.sendMessage(ChatColor.RED + "Failed to obtain this players profile");
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            attacker.sendMessage(ChatColor.RED + "You can not attack players while you have PvP Protection");
            event.setCancelled(true);
            return;
        }

        if (attackedProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            attacked.sendMessage(ChatColor.RED + "This player has PvP Protection");
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackerProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    attacker.sendMessage(ChatColor.RED + "PvP is disabled in " + ChatColor.RESET + sf.getDisplayName());
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackedProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    attacker.sendMessage(ChatColor.RED + "PvP is disabled in " + ChatColor.RESET + sf.getDisplayName());
                    event.setCancelled(true);
                    return;
                }
            }
        }

        final PlayerFaction attackerFaction = plugin.getFactionManager().getFactionByPlayer(attacker.getUniqueId());

        if (attackerFaction != null && attackerFaction.getMember(attacked.getUniqueId()) != null) {
            attacker.sendMessage(ChatColor.RED + "PvP is disabled between " + ChatColor.RESET + "Faction Members");
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerSplashPlayer(PlayerSplashPlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final ThrownPotion potion = event.getPotion();
        boolean isDebuff = false;

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        for (PotionEffect effect : potion.getEffects()) {
            if (effect.getType().equals(PotionEffectType.POISON) ||
            effect.getType().equals(PotionEffectType.SLOW) ||
            effect.getType().equals(PotionEffectType.WEAKNESS) ||
            effect.getType().equals(PotionEffectType.HARM)) {
                isDebuff = true;
                break;
            }
        }

        if (!isDebuff) {
            return;
        }

        final FactionPlayer attackerProfile = plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null || attackedProfile == null) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION) || attackedProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackerProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackedProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerLingeringSplash(PlayerLingeringSplashPlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final AreaEffectCloud cloud = event.getCloud();

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        if (!cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.HARM) &&
        !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.WEAKNESS) &&
        !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.SLOW) &&
        !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.POISON)) {
            return;
        }

        final FactionPlayer attackerProfile = plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null || attackedProfile == null) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION) || attackedProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackerProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackedProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}