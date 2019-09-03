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
                final StructureModifier<Integer> ints = packet.getIntegers();

                if (event.getPlayer() == null) {
                    return;
                }

                if (event.getPlayer().hasPermission("humbug.bypass")) {
                    return;
                }

                if (event.getPlayer().getEntityId() == ints.read(0)) {
                    return;
                }

                packet.getBytes().write(1, (byte)0);
                ints.write(1, 0);
            }
        });

        /* HIDES ENTITY METADATA SUCH AS ARMOR */
        humbug.getOwner().getProtocol().addPacketListener(new PacketAdapter(getHumbug().getOwner(), PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!isEnabled()) {
                    return;
                }

                final PacketContainer packet = event.getPacket();
                final Player player = event.getPlayer();
                final Entity entity = packet.getEntityModifier(event).read(0);
                final StructureModifier<List<WrappedWatchableObject>> modifier = packet.getWatchableCollectionModifier();
                final List<WrappedWatchableObject> read = modifier.read(0);

                if (player == null || entity == null) {
                    return;
                }

                if (event.getPlayer().hasPermission("humbug.bypass")) {
                    return;
                }

                if (
                        player.getUniqueId().equals(entity.getUniqueId()) ||
                        !(entity instanceof LivingEntity) ||
                        entity instanceof EnderDragon ||
                        entity instanceof Wither ||
                        entity.getPassengers().contains(player)) {

                    return;

                }

                for (WrappedWatchableObject object : read) {
                    if (object.getIndex() != 7) {
                        continue;
                    }

                    final float value = (float)object.getValue();

                    if (value > 0) {
                        object.setValue(1f);
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
