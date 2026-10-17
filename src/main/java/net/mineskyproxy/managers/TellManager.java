package net.mineskyproxy.managers;

import net.minesky.api.database.PlayerDatabase;
import net.minesky.api.database.UpdatedData;
import net.minesky.core.databridge.callbacks.ErrorType;
import net.minesky.core.databridge.callbacks.FindOneCallback;
import net.minesky.core.databridge.callbacks.SetOneCallback;
import org.bson.Document;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TellManager {
    private final Map<UUID, UUID> lastTell = new ConcurrentHashMap<>();

    private static final Set<UUID> playersWithSpy = ConcurrentHashMap.newKeySet();

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
            setSpy(player, false);
            return false;
        } else {
            playersWithSpy.add(player);
            setSpy(player, true);
            return true;
        }
    }

    public Set<UUID> getPlayersWithSpy() {
        return playersWithSpy;
    }

    public static void addPlayer(UUID p) {
        playersWithSpy.add(p);
    }

    public interface PlayerBooleanReturnCallback {
        void onQueryDone(boolean value);
    }

    public void setSpy(UUID uuid, boolean enabled) {
        PlayerDatabase.setPlayerData(uuid.toString(), new UpdatedData("spy", enabled), new SetOneCallback() {
            @Override public void onSetDone() {}
            @Override public void onSetError(ErrorType errorType) {}
        });
    }

    public static void getSpy(UUID uuid, PlayerBooleanReturnCallback callback) {
        PlayerDatabase.getPlayerDataAsync(uuid.toString(), new FindOneCallback() {
            @Override
            public void onQueryError(ErrorType errorType) {
                callback.onQueryDone(true);
            }

            @Override
            public void onQueryDone(Document document) {
                callback.onQueryDone(document == null || document.getBoolean("spy", false));
            }
        });
    }
}