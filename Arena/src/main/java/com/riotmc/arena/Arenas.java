package com.riotmc.arena;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.riotmc.arena.arena.ArenaHandler;
import com.riotmc.arena.arena.ArenaManager;
import com.riotmc.arena.challenge.ChallengeHandler;
import com.riotmc.arena.challenge.ChallengeManager;
import com.riotmc.arena.command.*;
import com.riotmc.arena.items.*;
import com.riotmc.arena.listener.CombatListener;
import com.riotmc.arena.listener.DataListener;
import com.riotmc.arena.listener.LoadoutListener;
import com.riotmc.arena.listener.PlayerListener;
import com.riotmc.arena.loadout.LoadoutHandler;
import com.riotmc.arena.loadout.LoadoutManager;
import com.riotmc.arena.match.MatchHandler;
import com.riotmc.arena.match.MatchManager;
import com.riotmc.arena.menu.MenuHandler;
import com.riotmc.arena.mode.ModeHandler;
import com.riotmc.arena.mode.ModeManager;
import com.riotmc.arena.player.PlayerHandler;
import com.riotmc.arena.player.PlayerManager;
import com.riotmc.arena.spectator.SpectatorHandler;
import com.riotmc.arena.team.TeamHandler;
import com.riotmc.arena.team.TeamManager;
import com.riotmc.services.classes.ClassService;
import com.riotmc.services.customevents.CustomEventService;
import com.riotmc.services.customitems.CustomItemService;
import com.riotmc.services.essentials.EssentialsService;
import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.profiles.ProfileService;
import com.riotmc.services.punishments.PunishmentService;
import com.riotmc.services.ranks.RankService;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Arenas extends RiotPlugin {
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

        final PaperCommandManager commandManager = new PaperCommandManager(this);
        registerCommandManager(commandManager);

        registerMongo(new MongoDB("mongodb+srv://dev:vIwpVwYNc4WTQkRN@ares-zny4z.mongodb.net/test?retryWrites=true")); //  mongodb://localhost
        registerProtocol(ProtocolLibrary.getProtocolManager());
        getMongo().openConnection();

        registerService(new CustomEventService(this));
        registerService(new CustomItemService(this));
        registerService(new HumbugService(this));
        registerService(new EssentialsService(this));
        registerService(new PunishmentService(this));
        registerService(new ProfileService(this));
        registerService(new RankService(this));
        registerService(new ClassService(this));
        startServices();

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
        registerCommand(new PingCommand());

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

        stopServices();

        if (getMongo() != null) {
            getMongo().closeConnection();
        }
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
