package net.mineskyproxy.channel;

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

public final class ProxyChannel {

    public static final String CHANNEL_NAME = "minesky:proxy";
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(CHANNEL_NAME);

    // Mensagens: Velocity -> Paper
    public static final String MSG_START_EVENT = "START_EVENT";
    public static final String MSG_STOP_EVENT = "STOP_EVENT";
    public static final String MSG_FORCE_STOP = "FORCE_STOP";
    public static final String MSG_PING = "PING";

    // Mensagens: Paper -> Velocity
    public static final String MSG_EVENT_STARTED = "EVENT_STARTED";
    public static final String MSG_EVENT_ENDED = "EVENT_ENDED";
    public static final String MSG_EVENT_CANCELLED = "EVENT_CANCELLED";
    public static final String MSG_REWARD = "REWARD";
    public static final String MSG_EVENT_STATUS = "EVENT_STATUS";
    public static final String MSG_PONG = "PONG";
    public static final String MSG_CONNECT_HUB = "CONNECT_HUB";
    public static final String MSG_VANISH_UPDATE = "VanishUpdate";
    public static final String MSG_DISCORD = "Discord";

    private ProxyChannel() {}
}