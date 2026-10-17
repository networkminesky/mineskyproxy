package net.mineskyproxy.channel;

public final class ProxyChannel {

    // Canal principal no MongoDB Change Streams
    public static final String MAIN_CHANNEL = "minesky:network";

    // Subcanais: Velocity -> Paper (MineSkyEvents / Backend)
    public static final String SUB_START_EVENT = "start-event";
    public static final String SUB_STOP_EVENT = "stop-event";
    public static final String SUB_FORCE_STOP = "force-stop";
    public static final String SUB_PING = "ping";

    // Subcanais: Velocity -> Paper (MineSkyBridge - Survival)
    public static final String SUB_PLAYER_VAULT = "reward-vault";

    // Subcanais: Paper -> Velocity
    public static final String SUB_EVENT_STARTED = "event-started";
    public static final String SUB_EVENT_ENDED = "event-ended";
    public static final String SUB_EVENT_CANCELLED = "event-cancelled";
    public static final String SUB_REWARD = "event-reward";
    public static final String SUB_EVENT_STATUS = "event-status";
    public static final String SUB_PONG = "pong";
    public static final String SUB_CONNECT_HUB = "connect-hub";
    public static final String SUB_VANISH_UPDATE = "vanish-update";
    public static final String SUB_DISCORD = "discord-staff";

    private ProxyChannel() {}
}