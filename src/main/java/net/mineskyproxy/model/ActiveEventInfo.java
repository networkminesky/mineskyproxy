package net.mineskyproxy.model;

public class ActiveEventInfo {
    private final String eventId;
    private final String mapId;
    private int playerCount;
    private String state;
    private final long startedAt;

    public ActiveEventInfo(String eventId, String mapId, int playerCount, String state) {
        this.eventId = eventId;
        this.mapId = mapId != null && !mapId.isBlank() ? mapId : "Padrão";
        this.playerCount = playerCount;
        this.state = state != null ? state : "Inscrições";
        this.startedAt = System.currentTimeMillis();
    }

    public String getEventId() {
        return eventId;
    }

    public String getDisplayName() {
        return EventType.getDisplayNameOrDefault(eventId);
    }

    public String getMapId() {
        return mapId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getDurationSeconds() {
        return (System.currentTimeMillis() - startedAt) / 1000;
    }
}
