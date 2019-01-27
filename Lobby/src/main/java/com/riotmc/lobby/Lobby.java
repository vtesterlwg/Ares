package com.riotmc.lobby;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.commons.base.promise.Promise;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.services.profiles.ProfileService;

public final class Lobby extends RiotPlugin {
    @Override
    public void onEnable() {
        final PaperCommandManager commandManager = new PaperCommandManager(this);

        registerCommandManager(commandManager);
        registerProtocol(ProtocolLibrary.getProtocolManager());

        registerService(new ProfileService(this));
    }

    /**
     * Examples for Alfie
     * TODO: Remove this when starting development
     */

    public void obtainingAService() {
        final ProfileService profileService = (ProfileService)getService(ProfileService.class);
    }

    public void usingPromises() {
        simplePromise(new SimplePromise() {
            public void success() {
                Logger.print("yippie!");
            }

            public void failure(String reason) {
                Logger.error(reason);
            }
        });

        failablePromise(new FailablePromise<String>() {
            public void success(String s) {
                Logger.print(s);
            }

            public void failure(String reason) {
                Logger.print(reason);
            }
        });

        promise(new Promise<Integer>() {
            public void ready(Integer integer) {
                Logger.print("" + integer);
            }
        });
    }

    /**
     * Simple Promises return an empty success body but contain a String for a failure reason
     * @param promise SimplePromise
     */
    private void simplePromise(SimplePromise promise) {

    }

    /**
     * Failable Promises return a type with a String for a failure reason
     * @param promise FailablePromise
     */
    private void failablePromise(FailablePromise<String> promise) {

    }

    /**
     * Promises can't fail and return a type
     * @param promise Promise
     */
    private void promise(Promise<Integer> promise) {

    }
}
