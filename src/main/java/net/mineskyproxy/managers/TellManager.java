package net.mineskyproxy.managers;

import com.velocitypowered.api.proxy.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TellManager {

    // Guarda o último jogador que conversou com quem (UUID de quem enviou -> UUID de quem recebeu)
    private final Map<UUID, UUID> lastTell = new ConcurrentHashMap<>();

    // Jogadores com SocialSpy ativo
    private final Set<UUID> playersWithSpy = ConcurrentHashMap.newKeySet();

    public void setLastTell(UUID player, UUID target) {
        if (player == null || target == null) return;
        lastTell.put(player, target);
    }

    public UUID getLastTell(UUID player) {
        if (player == null) return null;
        return lastTell.get(player);
    }

    public void removePlayer(UUID player) {
        if (player == null) return;
        lastTell.remove(player);
        lastTell.values().removeIf(target -> target.equals(player));
        playersWithSpy.remove(player);
    }

    public boolean hasSpy(UUID player) {
        return player != null && playersWithSpy.contains(player);
    }

    public boolean toggleSpy(UUID player) {
        if (player == null) return false;
        if (playersWithSpy.contains(player)) {
            playersWithSpy.remove(player);
            return false;
        } else {
            playersWithSpy.add(player);
            return true;
        }
    }

    public Set<UUID> getPlayersWithSpy() {
        return playersWithSpy;
    }
}