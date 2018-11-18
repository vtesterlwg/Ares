package com.riotmc.services.punishments;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.promise.Promise;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.services.punishments.data.Punishment;
import com.riotmc.services.punishments.data.PunishmentDAO;
import com.riotmc.services.punishments.data.PunishmentType;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.UUID;

public final class PunishmentManager {
    @Getter
    public final PunishmentService service;

    public PunishmentManager(PunishmentService service) {
        this.service = service;
    }

    public String getKickMessage(Punishment punishment) {
        Preconditions.checkArgument(punishment.getType().equals(PunishmentType.BAN) ||
                punishment.getType().equals(PunishmentType.BLACKLIST), "Supplied punishment was not a ban or blacklist");

        final List<String> response = Lists.newArrayList();

        if (punishment.getType().equals(PunishmentType.BLACKLIST)) {
            response.add(ChatColor.RED + "Your account has been blacklisted from RiotMC");
        }

        if (punishment.getType().equals(PunishmentType.BAN)) {
            response.add(ChatColor.RED + "Your account has been banned from RiotMC");

            if (punishment.isForever()) {
                response.add(ChatColor.RED + "This punishment will " + ChatColor.RED + "" + ChatColor.UNDERLINE + "never" + ChatColor.RED + " expire");
            } else {
                response.add(ChatColor.RED + "This punishment will expire in " + Time.convertToRemaining(punishment.getExpireDate() - Time.now()));
            }
        }

        response.add(ChatColor.RED + "Appeal at https://www.riotmc.com/appeal");
        return Joiner.on(ChatColor.RESET + "\n").join(response);
    }

    public String getMuteMessage(Punishment punishment) {
        Preconditions.checkArgument(punishment.getType().equals(PunishmentType.MUTE), "Supplied punishment was not a mute");

        List<String> response = Lists.newArrayList();

        if (punishment.isForever()) {
            response.add(ChatColor.RED + "You have been silenced for: " + punishment.getReason());
            response.add(ChatColor.RED + "This punishment will not expire");
        } else {
            response.add(ChatColor.RED + "You have been temporarily silenced for: " + punishment.getReason());
            response.add(ChatColor.RED + "This punishment will expire in " + Time.convertToRemaining(punishment.getExpireDate() - Time.now()));
        }

        response.add(ChatColor.RED + "Appeal at https://www.riotmc.com/appeal");
        return Joiner.on(ChatColor.RESET + "\n").join(response);
    }

    public void getActivePunishments(UUID uniqueId, int address, PunishmentType type, Promise<ImmutableList<Punishment>> promise) {
        new Scheduler(getService().getOwner()).async(() -> {
            final ImmutableList<Punishment> punishments = PunishmentDAO.getPunishments(getService().getOwner().getMongo(),
                    Filters.or(Filters.eq("punished", uniqueId), Filters.eq("address", address)),

                    Filters.and(Filters.eq("type", type.toString()), Filters.eq("appealed", false),
                            Filters.or(Filters.eq("expire", 0L), Filters.gt("expire", Time.now()))));

            new Scheduler(getService().getOwner()).sync(() -> promise.ready(punishments)).run();
        }).run();
    }

    public ImmutableList<Punishment> getActivePunishments(UUID uniqueId, int address, PunishmentType type) {
        return PunishmentDAO.getPunishments(getService().getOwner().getMongo(),
                Filters.or(Filters.eq("punished", uniqueId), Filters.eq("address", address)),

                Filters.and(Filters.eq("type", type.toString()), Filters.eq("appealed", false),
                        Filters.or(Filters.eq("expire", 0L), Filters.gt("expire", Time.now()))));
    }
}
