package com.playares.factions.addons.states.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ServerState {
    SOTW("Start of the World"), NORMAL("Normal"), EOTW("End of the World");

    @Getter public final String displayName;
}
