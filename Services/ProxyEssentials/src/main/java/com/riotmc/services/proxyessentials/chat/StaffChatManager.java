package com.riotmc.services.proxyessentials.chat;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Set;
import java.util.UUID;

public final class StaffChatManager {
    @Getter
    public final Set<UUID> staffChat;

    public StaffChatManager() {
        this.staffChat = Sets.newConcurrentHashSet();
    }

    public boolean isInStaffChat(ProxiedPlayer player) {
        return staffChat.contains(player.getUniqueId());
    }
}
