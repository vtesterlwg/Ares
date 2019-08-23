package com.playares.factions.addons.states.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ServerState {
    SOTW("Start of the World"),
    NORMAL("Normal"),
    EOTW_PHASE_1("End of the World: Phase #1"),
    EOTW_PHASE_2("End of the World: Phase #2");

    @Getter public final String displayName;

    public static ServerState getType(String name) {
        for (ServerState value : values()) {
            if (value.name().toLowerCase().replace("_", "").equalsIgnoreCase(name)) {
                return value;
            }
        }

        return null;
    }
}
