package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.playares.arena.Arenas;
import com.playares.arena.challenge.Challenge;
import com.playares.arena.challenge.cont.DuelChallenge;
import com.playares.arena.challenge.cont.TeamChallenge;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class AcceptCommand extends BaseCommand {
    @Nonnull @Getter
    public final Arenas plugin;

    public AcceptCommand(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("accept")
    @Description("Accept a challenge")
    public void onAccept(Player player, String matchId) {
        final UUID id;

        try {
            id = UUID.fromString(matchId);
        } catch (IllegalArgumentException ex) {
            player.sendMessage(ChatColor.RED + "Invalid match ID");
            return;
        }

        final Challenge challenge = plugin.getChallengeManager().getChallenge(id);

        if (challenge == null) {
            player.sendMessage(ChatColor.RED + "Challenge has expired");
            return;
        }

        if (challenge instanceof DuelChallenge) {
            final DuelChallenge duel = (DuelChallenge)challenge;
            plugin.getChallengeHandler().acceptChallenge(duel.getChallenger(), duel.getChallenged(), duel.getMode());
        }

        if (challenge instanceof TeamChallenge) {
            final TeamChallenge teamfight = (TeamChallenge)challenge;
            plugin.getChallengeHandler().acceptChallenge(teamfight.getChallenger(), teamfight.getChallenged(), teamfight.getMode());
        }
    }
}