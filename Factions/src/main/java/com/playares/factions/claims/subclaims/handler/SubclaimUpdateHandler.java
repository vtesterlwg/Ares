package com.playares.factions.claims.subclaims.handler;

import com.google.common.collect.Lists;
import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.claims.subclaims.manager.SubclaimManager;
import com.playares.factions.claims.subclaims.menu.SubclaimMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
public final class SubclaimUpdateHandler {
    @Getter public final SubclaimManager manager;

    public void performUpdate(Subclaim subclaim) {
        if (!manager.getActiveEditors().containsKey(subclaim)) {
            return;
        }

        final List<SubclaimMenu> activeMenus = manager.getActiveEditors().get(subclaim);

        activeMenus.forEach(SubclaimMenu::update);
    }

    public void performDelete(Player issuer, Subclaim subclaim) {
        if (!manager.getActiveEditors().containsKey(subclaim)) {
            return;
        }

        final List<SubclaimMenu> activeMenus = manager.getActiveEditors().get(subclaim);

        activeMenus.stream().filter(menu -> !menu.getPlayer().getUniqueId().equals(issuer.getUniqueId())).forEach(menu -> {
            menu.getPlayer().closeInventory();
            menu.getPlayer().sendMessage(ChatColor.RED + "Subclaim has been deleted by " + issuer.getName());
        });
    }

    public void openMenu(SubclaimMenu menu) {
        if (manager.getActiveEditors().containsKey(menu.getSubclaim())) {
            manager.getActiveEditors().get(menu.getSubclaim()).add(menu);
        } else {
            final List<SubclaimMenu> menus = Lists.newArrayList();
            menus.add(menu);
            manager.getActiveEditors().put(menu.getSubclaim(), menus);
        }

        menu.open();
        menu.update();
    }

    public void closeMenu(SubclaimMenu menu) {
        if (!manager.getActiveEditors().containsKey(menu.getSubclaim())) {
            return;
        }

        manager.getActiveEditors().get(menu.getSubclaim()).remove(menu);

        if (manager.getActiveEditors().get(menu.getSubclaim()).isEmpty()) {
            manager.getActiveEditors().remove(menu.getSubclaim());
        }
    }
}