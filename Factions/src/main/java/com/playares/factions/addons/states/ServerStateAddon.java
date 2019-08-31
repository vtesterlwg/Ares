package com.playares.factions.addons.states;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.spawnpoints.SpawnpointAddon;
import com.playares.factions.addons.spawnpoints.data.Spawnpoint;
import com.playares.factions.addons.states.command.ServerStateCommand;
import com.playares.factions.addons.states.data.ServerState;
import com.playares.factions.addons.states.listener.EOTWListener;
import com.playares.factions.claims.dao.ClaimDAO;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.claims.subclaims.dao.SubclaimDAO;
import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.services.deathban.DeathbanService;
import com.playares.services.deathban.dao.DeathbanDAO;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Set;
import java.util.stream.Collectors;

public final class ServerStateAddon implements Addon {
    private static final String EOTW_PREFIX = ChatColor.GOLD + "[" + ChatColor.DARK_RED + "EOTW" + ChatColor.GOLD + "]" + ChatColor.RESET + " ";

    @Getter public final Factions plugin;
    @Getter @Setter public ServerState currentState;
    @Getter @Setter public int shrinkRadius;
    @Getter @Setter public int shrinkRate;
    @Getter @Setter public boolean phase2GracePeriod;
    @Getter @Setter public int phaseTwoGracePeriodDuration;

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
        shrinkRate = config.getInt("server-state.eotw.rate");
        phaseTwoGracePeriodDuration = config.getInt("server-state.eotw.phase-two-grace-period");

        if (state == null) {
            Logger.error("Invalid server state! Setting to NORMAL");
            currentState = ServerState.NORMAL;
            return;
        }

        currentState = state;
        Logger.print("Server state is now at: " + currentState.name());
    }

    @Override
    public void start() {
        getPlugin().registerCommand(new ServerStateCommand(this));
        getPlugin().registerListener(new EOTWListener(this));

        performStateChange(currentState);
    }

    @Override
    public void stop() {}

    public void performUpdate(CommandSender sender, String name, SimplePromise promise) {
        ServerState state = null;

        if (name.equalsIgnoreCase("sotw")) {
            state = ServerState.SOTW;
        } else if (name.equalsIgnoreCase("normal")) {
            state = ServerState.NORMAL;
        } else if (name.equalsIgnoreCase("ep1")) {
            state = ServerState.EOTW_PHASE_1;
        } else if (name.equalsIgnoreCase("ep2")) {
            state = ServerState.EOTW_PHASE_2;
        }

        if (state == null) {
            promise.failure("Invalid server state. Valid types: " + ChatColor.YELLOW + "sotw, normal, ep1, ep2");
            return;
        }

        if (state.equals(currentState)) {
            promise.failure("This state is already set");
            return;
        }

        currentState = state;
        performStateChange(state);
        Logger.print(sender.getName() + " updated the state of the server to: " + state.name());
        promise.success();
    }

    public void performStateChange(ServerState state) {
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
                new Scheduler(getPlugin()).sync(() -> Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.GREEN + "All deathbans have been cleared. All future deathbans are now final.")).run();
            }).run();

            return;
        }

        if (state.equals(ServerState.EOTW_PHASE_2)) {
            new Scheduler(getPlugin()).async(() -> {
                final Set<DefinedClaim> claims = getPlugin().getClaimManager().getClaimRepository().stream().filter(claim -> getPlugin().getFactionManager().getFactionById(claim.getOwnerId()) instanceof PlayerFaction).collect(Collectors.toSet());
                final Set<Subclaim> subclaims = getPlugin().getSubclaimManager().getSubclaimRepository();

                ClaimDAO.deleteDefinedClaims(getPlugin().getMongo(), claims);
                SubclaimDAO.deleteSubclaims(getPlugin().getMongo(), subclaims);

                getPlugin().getClaimManager().getClaimRepository().removeAll(claims);
                getPlugin().getSubclaimManager().getSubclaimRepository().removeAll(subclaims);

                new Scheduler(getPlugin()).sync(() -> {
                    Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.YELLOW + "All player claims have been removed. Claiming is now disabled for the remainder of the map.");
                    Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.DARK_RED + "Use of the Nether & End is now being disabled! If you are in the Nether or End you now have " + Time.convertToRemaining(phaseTwoGracePeriodDuration * 1000L) + " to exit before being automatically killed!");

                    final SpawnpointAddon spawnpointAddon = (SpawnpointAddon)getPlugin().getAddonManager().getAddon(SpawnpointAddon.class);

                    if (spawnpointAddon != null && spawnpointAddon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD) != null) {
                        final Spawnpoint spawn = spawnpointAddon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD);
                        final WorldBorder border = spawnpointAddon.getManager().getSpawnpoint(Spawnpoint.SpawnpointType.OVERWORLD).getBukkit().getWorld().getWorldBorder();
                        border.setSize(shrinkRadius, shrinkRate);

                        new Scheduler(getPlugin()).sync(() -> {
                            setPhase2GracePeriod(false);

                            Bukkit.getOnlinePlayers().forEach(player -> {
                                if (!player.getWorld().equals(spawn.getBukkit().getWorld()) && !player.hasPermission("factions.serverstates.bypass")) {
                                    player.setHealth(0.0);
                                }
                            });

                            Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.DARK_RED + "All players in the Nether & The End have been slain");
                        }).delay(phaseTwoGracePeriodDuration * 20).run();

                        Bukkit.broadcastMessage(EOTW_PREFIX + ChatColor.DARK_AQUA + "The world border will now begin shrinking for the next " + ChatColor.AQUA + Time.convertToRemaining(shrinkRate * 1000));
                        Bukkit.broadcastMessage(ChatColor.GREEN + "Good luck!");
                    }
                }).run();
            }).run();
        }
    }
}