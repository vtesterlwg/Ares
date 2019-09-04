package com.playares.services.humbug.features.cont;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;

import java.util.List;

public final class AntiAttributes implements HumbugModule {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;

    public AntiAttributes(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public String getName() {
        return "Anti Attributes";
    }

    @Override
    public void loadValues() {
        final YamlConfiguration config = getHumbug().getOwner().getConfig("humbug");

        if (config == null) {
            Logger.error("Failed to obtain humbug.yml");
            return;
        }

        this.enabled = config.getBoolean("anti-attributes.enabled");
    }

    @Override
    public void start() {
        humbug.getOwner().getProtocol().addPacketListener(new PacketAdapter(getHumbug().getOwner(), PacketType.Play.Server.ENTITY_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!isEnabled()) {
                    return;
                }

                final PacketContainer packet = event.getPacket();

                if (event.getPlayer().hasPermission("humbug.bypass")) {
                    return;
                }

                final StructureModifier<Integer> ints = packet.getIntegers();

                if (event.getPlayer().getEntityId() == ints.read(0)) {
                    return;
                }

                packet.getBytes().write(1, (byte)0);
                ints.write(1, 0);
            }
        });

        /* HIDES ENTITY METADATA SUCH AS ARMOR AND HEALTH */
        humbug.getOwner().getProtocol().addPacketListener(new PacketAdapter(getHumbug().getOwner(), PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!isEnabled()) {
                    return;
                }

                final PacketContainer packet = event.getPacket();
                final Player player = event.getPlayer();

                if (player.hasPermission("humbug.bypass")) {
                    return;
                }

                final Entity entity = packet.getEntityModifier(event).read(0);
                final StructureModifier<List<WrappedWatchableObject>> modifier = packet.getWatchableCollectionModifier();
                final List<WrappedWatchableObject> read = modifier.read(0);

                if (
                        !(entity instanceof LivingEntity) ||
                        player.getUniqueId().equals(entity.getUniqueId()) ||
                        entity instanceof EnderDragon ||
                        entity instanceof Wither ||
                        entity.getPassengers().contains(player)) {

                    return;

                }

                for (WrappedWatchableObject obj : read) {
                    if (obj.getIndex() == 7) {
                        final float value = (float)obj.getValue();

                        if (value > 0) {
                            obj.setValue(1f);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void stop() {
        setEnabled(false);
    }
}
