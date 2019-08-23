package com.playares.factions.addons.states;

import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.spawnpoints.SpawnpointAddon;
import com.playares.factions.addons.spawnpoints.data.Spawnpoint;
import com.playares.factions.addons.states.data.ServerState;
import com.playares.factions.claims.dao.ClaimDAO;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.services.deathban.DeathbanService;
import com.playares.services.deathban.dao.DeathbanDAO;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Set;
import java.util.stream.Collectors;

public final class ServerStateAddon implements Addon {
    public static final String EOTW_PREFIX = ChatColor.GOLD + "[" + ChatColor.DARK_RED + "EOTW" + ChatColor.GOLD + "]" + ChatColor.RESET + " ";

    @Getter public final Factions plugin;
    @Getter @Setter public ServerState currentState;
    @Getter @Setter public int shrinkRadius;
    @Getter @Setter public int shrinkRate;
    @Getter @Setter public boolean phase2GracePeriod;

    public ServerStateAddon(Factions plugin) {
        this.plugin = plugin;
        this.phase2GracePeriod = true;
    }

    @Override
    public String getName() {
        return "Server States";
    }

    @Override
    public void prepare() {
        final YamlConfiguration config = getPlugin().getConfig("config");
        final ServerState state = ServerState.getType(config.getString("server-state.current"));

        shrinkRadius = config.getInt("server-state.eotw.radius");
        shrinkRadius = config.getInt("server-state.eotw.rate");

        if (state == null) {
            Logger.error("Invalid server state! Setting to NORMAL");
            currentState = ServerState.NORMAL;
            return;
        }

        currentState = state;
        Logger.print("Server state is now at: " + currentState.name());
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    public void performPhase(ServerState state) {
        if (state.equals(ServerState.NORMAL) || state.equals(ServerState.SOTW)) {
            return;
        }

        if (state.equals(ServerState.EOTW_PHASE_1)) {
            final DeathbanService deathbanService = (DeathbanService)getPlugin().getService(DeathbanService.class);

            if (deathbanService == null) {
                Logger.error("Failed to obtain Deathban Service while trying to clear deathbans");
                return;
            }

            new Scheduler(getPlugin()).async(() -> {
                DeathbanDAO.clearDeathbans(getPlugin().getMongo());
                new Scheduler(getPlugin()).sync(() -> Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.GREEN + "All deathbans have been removed. All future deathbans are now " + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "final"));
            }).run();

            return;
        }

        if (state.equals(ServerState.EOTW_PHASE_2)) {
            new Scheduler(getPlugin()).async(() -> {
                final Set<DefinedClaim> claims = getPlugin().getClaimManager().getClaimRepository().stream().filter(claim -> getPlugin().getFactionManager().getFactionById(claim.getOwnerId()) instanceof PlayerFaction).collect(Collectors.toSet());
                ClaimDAO.deleteDefinedClaims(getPlugin().getMongo(), claims);
                getPlugin().getClaimManager().getClaimRepository().removeAll(claims);

                new Scheduler(getPlugin()).sync(() -> {
                    Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.RED + "All player claims have been removed and claiming has been disabled for the remainder of the map.");
                    Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.RED + "Use of the Nether/End has been " + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "disabled." + ChatColor.RED + "If you are in the Nether/End you now have 5 minutes to exit before being killed.");

                    final SpawnpointAddon spawnpointAddon = (SpawnpointAddon)getPlugin().getAddonManager().getAddon(SpawnpointAddon.class);

                    if (spawnpointAddon != null && spawnpointAddon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD) != null) {
                        final Spawnpoint spawn = spawnpointAddon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD);
                        final WorldBorder border = spawnpointAddon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD).getBukkit().getWorld().getWorldBorder();
                        border.setSize(shrinkRadius, shrinkRate);

                        new Scheduler(getPlugin()).sync(() -> {
                            setPhase2GracePeriod(false);

                            Bukkit.getOnlinePlayers().forEach(player -> {
                                if (!player.getWorld().equals(spawn.getBukkit().getWorld())) {
                                    player.setHealth(0.0);
                                }
                            });

                            Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.GREEN + "All players in the Nether/End have been slain");
                        }).delay(300 * 20).run();

                        Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.DARK_AQUA + "The world border will now begin shrinking for the next " + ChatColor.AQUA + Time.convertToRemaining(shrinkRate * 1000));
                    }
                }).run();
            }).run();
        }
    }
}
