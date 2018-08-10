package com.playares.arena;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.playares.arena.arena.ArenaHandler;
import com.playares.arena.arena.ArenaManager;
import com.playares.arena.challenge.ChallengeHandler;
import com.playares.arena.challenge.ChallengeManager;
import com.playares.arena.command.*;
import com.playares.arena.items.*;
import com.playares.arena.listener.CombatListener;
import com.playares.arena.listener.DataListener;
import com.playares.arena.listener.LoadoutListener;
import com.playares.arena.listener.PlayerListener;
import com.playares.arena.loadout.LoadoutHandler;
import com.playares.arena.loadout.LoadoutManager;
import com.playares.arena.match.MatchHandler;
import com.playares.arena.match.MatchManager;
import com.playares.arena.menu.MenuHandler;
import com.playares.arena.mode.ModeHandler;
import com.playares.arena.mode.ModeManager;
import com.playares.arena.player.PlayerHandler;
import com.playares.arena.player.PlayerManager;
import com.playares.arena.spectator.SpectatorHandler;
import com.playares.arena.team.TeamHandler;
import com.playares.arena.team.TeamManager;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.customevents.CustomEventService;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.humbug.HumbugService;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.PunishmentService;
import com.playares.services.ranks.RankService;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Arenas extends AresPlugin {
    @Getter protected MatchManager matchManager;
    @Getter protected MatchHandler matchHandler;
    @Getter protected PlayerManager playerManager;
    @Getter protected PlayerHandler playerHandler;
    @Getter protected TeamManager teamManager;
    @Getter protected TeamHandler teamHandler;
    @Getter protected ArenaManager arenaManager;
    @Getter protected ArenaHandler arenaHandler;
    @Getter protected MenuHandler menuHandler;
    @Getter protected ModeManager modeManager;
    @Getter protected ModeHandler modeHandler;
    @Getter protected LoadoutHandler loadoutHandler;
    @Getter protected LoadoutManager loadoutManager;
    @Getter protected ChallengeHandler challengeHandler;
    @Getter protected ChallengeManager challengeManager;
    @Getter protected SpectatorHandler spectatorHandler;

    @Getter protected YamlConfiguration mainConfig;

    @Override
    public void onEnable() {
        this.mainConfig = getConfig("config");

        this.matchManager = new MatchManager(this);
        this.matchHandler = new MatchHandler(this);
        this.playerManager = new PlayerManager(this);
        this.playerHandler = new PlayerHandler(this);
        this.teamManager = new TeamManager(this);
        this.teamHandler = new TeamHandler(this);
        this.arenaManager = new ArenaManager(this);
        this.arenaHandler = new ArenaHandler(this);
        this.menuHandler = new MenuHandler(this);
        this.loadoutHandler = new LoadoutHandler(this);
        this.loadoutManager = new LoadoutManager(this);
        this.modeManager = new ModeManager(this);
        this.modeHandler = new ModeHandler(this);
        this.challengeHandler = new ChallengeHandler(this);
        this.challengeManager = new ChallengeManager(this);
        this.spectatorHandler = new SpectatorHandler(this);

        final PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.getCommandCompletions().registerCompletion("arenalist", c -> this.arenaManager.getArenaList());
        registerCommandManager(commandManager);

        registerMongo(new MongoDB("mongodb://localhost"));
        registerProtocol(ProtocolLibrary.getProtocolManager());
        getMongo().openConnection();

        registerListener(new CombatListener(this));
        registerListener(new DataListener(this));
        registerListener(new PlayerListener(this));
        registerListener(new LoadoutListener(this));

        registerCommand(new ModeCommand(this));
        registerCommand(new LoadoutCommand(this));
        registerCommand(new AcceptCommand(this));
        registerCommand(new ArenaCommand(this));
        registerCommand(new DuelCommand(this));
        registerCommand(new AftermatchCommand(this));
        registerCommand(new SpectateCommand(this));
        registerCommand(new SetLobbyCommand(this));
        registerCommand(new TeamCommand(this));

        registerService(new CustomEventService(this));
        registerService(new CustomItemService(this));
        registerService(new HumbugService(this));
        registerService(new EssentialsService(this));
        registerService(new PunishmentService(this));
        registerService(new ProfileService(this));
        registerService(new RankService(this));
        startServices();

        setupItems();
    }

    @Override
    public void onDisable() {
        this.arenaManager.getArenas().clear();
        this.matchManager.getMatches().clear();
        this.playerManager.getPlayers().clear();
        this.teamManager.getTeams().clear();
        this.modeManager.getModes().clear();
        this.loadoutManager.getLoadouts().clear();
    }

    private void setupItems() {
        final CustomItemService customItemService = (CustomItemService)getService(CustomItemService.class);

        if (customItemService == null) {
            Logger.error("Failed to create custom items!");
            return;
        }

        customItemService.registerNewItem(new CreateTeamItem(this));
        customItemService.registerNewItem(new LeaveTeamItem(this));
        customItemService.registerNewItem(new ViewTeamItem(this));
        customItemService.registerNewItem(new ExitSpectatorItem(this));
        customItemService.registerNewItem(new TeamStatusItem(this));
    }
}
