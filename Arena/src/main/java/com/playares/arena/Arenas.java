package com.playares.arena;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.playares.arena.arena.ArenaManager;
import com.playares.arena.command.*;
import com.playares.arena.duel.DuelManager;
import com.playares.arena.item.*;
import com.playares.arena.kit.KitManager;
import com.playares.arena.listener.*;
import com.playares.arena.match.MatchManager;
import com.playares.arena.player.PlayerManager;
import com.playares.arena.queue.QueueManager;
import com.playares.arena.report.ReportManager;
import com.playares.arena.spawn.SpawnManager;
import com.playares.arena.spectate.SpectateManager;
import com.playares.arena.team.TeamManager;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.automatedrestarts.AutomatedRestartService;
import com.playares.services.chatrestrictions.ChatRestrictionService;
import com.playares.services.customevents.CustomEventService;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.humbug.HumbugService;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.PunishmentService;
import com.playares.services.ranks.RankService;
import com.playares.services.serversync.ServerSyncService;
import com.playares.services.serversync.data.Server;
import lombok.Getter;

public final class Arenas extends AresPlugin {
    @Getter protected ArenasConfig arenasConfig;

    @Getter public ArenaManager arenaManager;
    @Getter public PlayerManager playerManager;
    @Getter public TeamManager teamManager;
    @Getter public KitManager kitManager;
    @Getter public QueueManager queueManager;
    @Getter public MatchManager matchManager;
    @Getter public ReportManager reportManager;
    @Getter public SpectateManager spectateManager;
    @Getter public DuelManager duelManager;
    @Getter public SpawnManager spawnManager;

    @Override
    public void onEnable() {
        arenasConfig = new ArenasConfig(this);
        arenasConfig.loadValues();

        arenaManager = new ArenaManager(this);
        playerManager = new PlayerManager(this);
        teamManager = new TeamManager(this);
        kitManager = new KitManager(this);
        queueManager = new QueueManager(this);
        matchManager = new MatchManager(this);
        reportManager = new ReportManager(this);
        spectateManager = new SpectateManager(this);
        duelManager = new DuelManager(this);
        spawnManager = new SpawnManager(this);

        final PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        registerCommandManager(commandManager);

        registerMongo(new MongoDB(arenasConfig.getDatabaseURI()));
        registerProtocol(ProtocolLibrary.getProtocolManager());

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getMongo().openConnection();

        registerListener(new PlayerConnectionListener(this));
        registerListener(new KitListener(this));
        registerListener(new CombatListener(this));
        registerListener(new ReportListener(this));
        registerListener(new ArenaListener(this));
        registerListener(new ClassListener(this));
        registerListener(new SpectatorListener(this));

        registerCommand(new ArenaCommand(this));
        registerCommand(new TeamCommand(this));
        registerCommand(new KitCommand(this));
        registerCommand(new ReportCommand(this));
        registerCommand(new SpectateCommand(this));
        registerCommand(new DuelCommand(this));
        registerCommand(new SpawnCommand(this));
        registerCommand(new TeamDuelCommand(this));

        registerService(new AutomatedRestartService(this, 86400));
        registerService(new ChatRestrictionService(this));
        registerService(new PlayerClassService(this));

        registerService(new ServerSyncService(this, new Server(this,
                getArenasConfig().getServerId(),
                getArenasConfig().getBungeeName(),
                getArenasConfig().getDisplayName(),
                getArenasConfig().getDescription(),
                getArenasConfig().getServerType(),
                getArenasConfig().getPremiumAllocatedSlots())));

        registerService(new CustomEventService(this));
        registerService(new CustomItemService(this));
        registerService(new HumbugService(this));
        registerService(new EssentialsService(this));
        registerService(new PunishmentService(this));
        registerService(new ProfileService(this));
        registerService(new RankService(this));
        startServices();

        final CustomItemService customItemService = (CustomItemService)getService(CustomItemService.class);

        if (customItemService != null) {
            customItemService.registerNewItem(new CreateTeamItem(this));
            customItemService.registerNewItem(new LeaveDisbandTeamItem(this));
            customItemService.registerNewItem(new RankedQueueItem(this));
            customItemService.registerNewItem(new UnrankedQueueItem(this));
            customItemService.registerNewItem(new OtherTeamItem(this));
            customItemService.registerNewItem(new LeaveQueueItem(this));
            customItemService.registerNewItem(new LeaveSpectatorItem(this));
        } else {
            Logger.error("Failed to obtain Custom Item Service!");
        }

        spawnManager.load();
        kitManager.load();
        arenaManager.load();
        queueManager.load();
    }

    @Override
    public void onDisable() {
        playerManager.save(true);

        stopServices();

        if (getMongo() != null) {
            getMongo().closeConnection();
        }
    }
}