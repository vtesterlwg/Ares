package com.playares.services.punishments;

import com.playares.commons.base.util.IPS;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.event.ProcessedChatEvent;
import com.playares.commons.bukkit.service.RiotService;
import com.playares.services.punishments.command.*;
import com.playares.services.punishments.data.Punishment;
import com.playares.services.punishments.data.PunishmentType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;
import java.util.UUID;

public final class PunishmentService implements RiotService, Listener {
    @Getter
    public final RiotPlugin owner;

    @Getter
    protected final PunishmentHandler punishmentHandler;

    @Getter
    protected final PunishmentManager punishmentManager;

    public PunishmentService(RiotPlugin owner) {
        this.owner = owner;
        this.punishmentHandler = new PunishmentHandler(this);
        this.punishmentManager = new PunishmentManager(this);
    }

    public void start() {
        registerListener(this);
        registerCommand(new BanCommand(this));
        registerCommand(new MuteCommand(this));
        registerCommand(new BlacklistCommand(this));
        registerCommand(new UnbanCommand(this));
        registerCommand(new UnmuteCommand(this));
        registerCommand(new UnblacklistCommand(this));
    }

    public void stop() {
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
        ProcessedChatEvent.getHandlerList().unregister(this);
    }

    public String getName() {
        return "Punishments";
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();
        final int address = IPS.toInt(event.getAddress().getHostAddress());
        final List<Punishment> blacklists = getPunishmentManager().getActivePunishments(uniqueId, address, PunishmentType.BLACKLIST);
        final List<Punishment> bans = getPunishmentManager().getActivePunishments(uniqueId, address, PunishmentType.BAN);

        if (!blacklists.isEmpty()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            event.setKickMessage(getPunishmentManager().getKickMessage(blacklists.get(0)));
            return;
        }

        if (!bans.isEmpty()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            event.setKickMessage(getPunishmentManager().getKickMessage(bans.get(0)));
            return;
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final List<Punishment> mutes = getPunishmentManager().getActivePunishments(player.getUniqueId(), IPS.toInt(player.getAddress().getAddress().getHostAddress()), PunishmentType.MUTE);

        if (!mutes.isEmpty()) {
            event.setCancelled(true);
            player.sendMessage(getPunishmentManager().getMuteMessage(mutes.get(0)));
        }
    }
}