package com.riotmc.factions.claims.builder;

import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.Factions;
import com.riotmc.factions.claims.DefinedClaim;
import com.riotmc.factions.claims.pillars.ClaimPillar;
import com.riotmc.factions.factions.Faction;
import com.riotmc.factions.factions.PlayerFaction;
import com.riotmc.factions.factions.ServerFaction;
import com.riotmc.factions.players.FactionPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public final class DefinedClaimBuilder {
    @Getter
    public Factions plugin;

    @Getter
    public final Faction owner;

    @Getter
    public final Player claimer;

    @Getter
    public BLocatable cornerA;

    @Getter
    public BLocatable cornerB;

    public DefinedClaimBuilder(Factions plugin, Faction faction, Player claimer) {
        this.plugin = plugin;
        this.owner = faction;
        this.claimer = claimer;
        this.cornerA = null;
        this.cornerB = null;
    }

    public void setCornerA(BLocatable location) {
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(claimer.getUniqueId());

        this.cornerA = location;

        if (profile != null) {
            final ClaimPillar pillar = new ClaimPillar(claimer, ClaimPillar.ClaimPillarType.A, location);
            final ClaimPillar previous = profile.getExistingClaimPillar(ClaimPillar.ClaimPillarType.A);

            if (previous != null) {
                previous.hide();
                profile.getPillars().remove(previous);
            }

            pillar.draw();
            profile.getPillars().add(pillar);
        }

        claimer.sendMessage(ChatColor.GOLD + "Claim point A set at " + ChatColor.YELLOW + "(" + ChatColor.WHITE +
                location.getX() + " " + location.getY() + " " + location.getZ() + ChatColor.YELLOW + ")");

        if (cornerB != null) {
            claimer.sendMessage(ChatColor.GOLD + "Claim Value" + ChatColor.YELLOW + ": " + calculateValue());
        }
    }

    public void setCornerB(BLocatable location) {
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(claimer.getUniqueId());

        this.cornerB = location;

        if (profile != null) {
            final ClaimPillar pillar = new ClaimPillar(claimer, ClaimPillar.ClaimPillarType.B, location);
            final ClaimPillar previous = profile.getExistingClaimPillar(ClaimPillar.ClaimPillarType.B);

            if (previous != null) {
                previous.hide();
                profile.getPillars().remove(previous);
            }

            profile.getPillars().add(pillar);

            // Delay the sending of B pillars to prevent the bottom block being right-clicked quickly
            new Scheduler(plugin).sync(pillar::draw).delay(5L).run();
        }

        claimer.sendMessage(ChatColor.GOLD + "Claim point B set at " + ChatColor.YELLOW + "(" + ChatColor.WHITE +
                location.getX() + " " + location.getY() + " " + location.getZ() + ChatColor.YELLOW + ")");

        if (cornerA != null) {
            claimer.sendMessage(ChatColor.GOLD + "Claim Value" + ChatColor.YELLOW + ": " + calculateValue());
        }
    }

    public void resetCorners() {
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(claimer.getUniqueId());

        this.cornerA = null;
        this.cornerB = null;

        if (profile != null) {
            profile.hideAllClaimPillars();
        }

        claimer.sendMessage(ChatColor.YELLOW + "Claim reset");
    }

    private double calculateValue() {
        final double xMin = Math.min(cornerA.getX(), cornerB.getX());
        final double zMin = Math.min(cornerA.getZ(), cornerB.getZ());
        final double xMax = Math.max(cornerA.getX(), cornerB.getX());
        final double zMax = Math.max(cornerA.getZ(), cornerB.getZ());

        final double a = (int)Math.round(Math.abs(xMax - xMin));
        final double b = (int)Math.round(Math.abs(zMax - zMin));

        return a * b * plugin.getFactionConfig().getClaimBlockValue();
    }

    public void build(FailablePromise<DefinedClaim> promise) {
        final boolean admin = claimer.hasPermission("factions.admin");

        if (cornerA == null) {
            promise.failure("Corner A has not been set (Left-click)");
            return;
        }

        if (cornerB == null) {
            promise.failure("Corner B has not been set (Right-click)");
            return;
        }

        if (!cornerA.getWorldName().equals(cornerB.getWorldName())) {
            promise.failure("Worlds do not match for both corners");
            return;
        }

        new Scheduler(plugin).async(() -> {
            final DefinedClaim claim = new DefinedClaim(plugin, owner, cornerA, cornerB);
            final List<DefinedClaim> existingClaims = plugin.getClaimManager().getClaimsByOwner(owner);
            boolean isTouchingExisting = existingClaims.isEmpty() || owner instanceof ServerFaction;

            if (claim.getLxW()[0] < plugin.getFactionConfig().getClaimMinSize() || claim.getLxW()[1] < plugin.getFactionConfig().getClaimMinSize()) {
                new Scheduler(plugin).sync(() -> promise.failure("Claim must be at least " + plugin.getFactionConfig().getClaimMinSize() + "x" + plugin.getFactionConfig().getClaimMinSize())).run();
                return;
            }

            for (DefinedClaim existing : plugin.getClaimManager().getClaimRepository()) {
                if (existing.overlaps(claim.getX1(), claim.getZ1(), claim.getX2(), claim.getZ2(), claim.getWorldName())) {
                    new Scheduler(plugin).sync(() -> promise.failure("Claim is overlapping an existing claim")).run();
                    return;
                }
            }

            for (BLocatable perimeter : claim.getPerimeter(64)) {
                for (DefinedClaim nearby : plugin.getClaimManager().getClaimsNearby(perimeter)) {
                    if (!nearby.getOwnerId().equals(owner.getUniqueId())) {
                        new Scheduler(plugin).sync(() -> promise.failure("Claim is too close to an existing claim")).run();
                        return;
                    }
                }

                if (!isTouchingExisting) {
                    for (DefinedClaim existing : existingClaims) {
                        if (existing.touching(perimeter)) {
                            isTouchingExisting = true;
                        }
                    }
                }
            }

            if (!isTouchingExisting) {
                new Scheduler(plugin).sync(() -> promise.failure("Claim is not touching existing claims")).run();
                return;
            }

            if (owner instanceof PlayerFaction) {
                final PlayerFaction pf = (PlayerFaction)owner;

                if (pf.getBalance() < calculateValue() && !admin) {
                    new Scheduler(plugin).sync(() -> promise.failure("Your faction can not afford this claim")).run();
                    return;
                }

                pf.setBalance(pf.getBalance() - calculateValue());

                claim.setY1(0);
                claim.setY2(256);
            }

            new Scheduler(plugin).sync(() -> promise.success(claim)).run();
        }).run();
    }
}