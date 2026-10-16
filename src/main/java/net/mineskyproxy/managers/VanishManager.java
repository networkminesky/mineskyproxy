package net.mineskyproxy.managers;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishManager {

    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();

    public void setVanished(UUID uuid, boolean vanished) {
        if (uuid == null) return;
        if (vanished) {
            vanishedPlayers.add(uuid);
        } else {
            vanishedPlayers.remove(uuid);
        }
    }

    public boolean isVanished(UUID uuid) {
        if (uuid == null) return false;
        return vanishedPlayers.contains(uuid);
    }

    public void remove(UUID uuid) {
        if (uuid != null) {
            vanishedPlayers.remove(uuid);
        }
    }

    public void clear() {
        vanishedPlayers.clear();
    }
}