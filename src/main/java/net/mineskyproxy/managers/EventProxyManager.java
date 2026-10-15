package net.mineskyproxy.managers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.mineskyproxy.channel.ProxyChannel;
import net.mineskyproxy.config.ProxyConfig;
import net.mineskyproxy.model.ActiveEventInfo;
import net.mineskyproxy.model.EventType;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventProxyManager {

    private final ProxyServer server;
    private final ProxyConfig config;
    private final Logger logger;
    private final RewardManager rewardManager;

    private volatile ActiveEventInfo activeEvent;
    private long lastPingSentAt = 0;
    private final List<Consumer<Long>> pendingPingCallbacks = Collections.synchronizedList(new ArrayList<>());

    public EventProxyManager(ProxyServer server, ProxyConfig config, Logger logger, RewardManager rewardManager) {
        this.server = server;
        this.config = config;
        this.logger = logger;
        this.rewardManager = rewardManager;
    }

    public Optional<RegisteredServer> getEventServer() {
        return server.getServer(config.getEventServer());
    }

    public boolean sendToEventServer(byte[] data) {
        Optional<RegisteredServer> targetServer = getEventServer();
        if (targetServer.isEmpty()) {
            logger.warn("Servidor de eventos '{}' nao foi encontrado nas configuracoes do Velocity!", config.getEventServer());
            return false;
        }

        return targetServer.get().sendPluginMessage(ProxyChannel.IDENTIFIER, data);
    }

    // Velocity -> Paper: START_EVENT
    public boolean sendStartEvent(String eventId, String mapId, boolean manual) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(ProxyChannel.MSG_START_EVENT);
        out.writeUTF(eventId);
        out.writeUTF(mapId != null ? mapId : "");
        out.writeBoolean(manual);

        boolean sent = sendToEventServer(out.toByteArray());
        if (sent) {
            logger.info("Enviado START_EVENT (evento={}, mapa={}, manual={})", eventId, mapId, manual);
        }
        return sent;
    }

    // Velocity -> Paper: STOP_EVENT
    public boolean sendStopEvent() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(ProxyChannel.MSG_STOP_EVENT);

        boolean sent = sendToEventServer(out.toByteArray());
        if (sent) {
            logger.info("Enviado STOP_EVENT para o servidor de eventos.");
        }
        return sent;
    }

    // Velocity -> Paper: FORCE_STOP
    public boolean sendForceStop() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(ProxyChannel.MSG_FORCE_STOP);

        boolean sent = sendToEventServer(out.toByteArray());
        if (sent) {
            logger.info("Enviado FORCE_STOP para o servidor de eventos.");
        }
        return sent;
    }

    // Velocity -> Paper: PING
    public boolean sendPing(Consumer<Long> callback) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(ProxyChannel.MSG_PING);

        if (callback != null) {
            pendingPingCallbacks.add(callback);
        }
        this.lastPingSentAt = System.currentTimeMillis();

        return sendToEventServer(out.toByteArray());
    }

    // Paper -> Velocity: EVENT_STARTED
    public void handleEventStarted(String eventId, String mapId, int playerCount) {
        this.activeEvent = new ActiveEventInfo(eventId, mapId, playerCount, "Inscrições");
        logger.info("[EVENT_STARTED] Evento '{}' (mapa: '{}') iniciado com {} jogadores.", eventId, mapId, playerCount);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("event_id", eventId);
        placeholders.put("event_name", EventType.getDisplayNameOrDefault(eventId));
        placeholders.put("map_id", mapId != null && !mapId.isBlank() ? mapId : "Padrão");
        placeholders.put("players", String.valueOf(playerCount));

        if (config.isBroadcastOnStart()) {
            Component broadcast = config.getMessage("event-started-broadcast", placeholders);
            server.getAllPlayers().forEach(p -> p.sendMessage(broadcast));
        }

        if (config.isSendTitleOnStart()) {
            Component titleComp = config.getMessage("title-started", placeholders);
            Component subComp = config.getMessage("subtitle-started", placeholders);

            Title.Times times = Title.Times.times(
                    Duration.ofMillis(config.getTitleFadeIn() * 50L),
                    Duration.ofMillis(config.getTitleStay() * 50L),
                    Duration.ofMillis(config.getTitleFadeOut() * 50L)
            );

            Title title = Title.title(titleComp, subComp, times);
            server.getAllPlayers().forEach(p -> p.showTitle(title));
        }
    }

    // Paper -> Velocity: EVENT_ENDED
    public void handleEventEnded(String eventId, String mapId) {
        logger.info("[EVENT_ENDED] Evento '{}' finalizado.", eventId);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("event_id", eventId);
        placeholders.put("event_name", EventType.getDisplayNameOrDefault(eventId));
        placeholders.put("map_id", mapId != null && !mapId.isBlank() ? mapId : "Padrão");

        if (config.isBroadcastOnEnd()) {
            Component broadcast = config.getMessage("event-ended-broadcast", placeholders);
            server.getAllPlayers().forEach(p -> p.sendMessage(broadcast));
        }

        this.activeEvent = null;
    }

    // Paper -> Velocity: EVENT_CANCELLED
    public void handleEventCancelled(String eventId, String reason) {
        logger.info("[EVENT_CANCELLED] Evento '{}' cancelado. Motivo: {}", eventId, reason);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("event_id", eventId);
        placeholders.put("event_name", EventType.getDisplayNameOrDefault(eventId));
        placeholders.put("reason", reason != null ? reason : "Desconhecido");

        if (config.isBroadcastOnCancel()) {
            Component broadcast = config.getMessage("event-cancelled-broadcast", placeholders);
            server.getAllPlayers().forEach(p -> p.sendMessage(broadcast));
        }

        this.activeEvent = null;
    }

    // Paper -> Velocity: EVENT_STATUS
    public void handleEventStatus(String eventId, String state, int players) {
        if (this.activeEvent != null && this.activeEvent.getEventId().equalsIgnoreCase(eventId)) {
            this.activeEvent.setState(state);
            this.activeEvent.setPlayerCount(players);
        } else {
            this.activeEvent = new ActiveEventInfo(eventId, "", players, state);
        }
        logger.debug("[EVENT_STATUS] Evento '{}' | Estado: {} | Jogadores: {}", eventId, state, players);
    }

    // Paper -> Velocity: PONG
    public void handlePong(String serverName) {
        long latency = lastPingSentAt > 0 ? (System.currentTimeMillis() - lastPingSentAt) : 0;
        logger.info("[PONG] Resposta recebida de '{}' em {}ms.", serverName, latency);

        synchronized (pendingPingCallbacks) {
            for (Consumer<Long> cb : pendingPingCallbacks) {
                try {
                    cb.accept(latency);
                } catch (Exception e) {
                    logger.error("Erro ao executar callback de ping", e);
                }
            }
            pendingPingCallbacks.clear();
        }
    }

    // Paper -> Velocity: CONNECT_HUB
    public void handleConnectHub(String playerName, String targetServerName) {
        logger.info("[CONNECT_HUB] Jogador '{}' solicitou redirecionamento para hub (alvo: '{}').", playerName, targetServerName);

        Optional<Player> optionalPlayer = server.getPlayer(playerName);
        if (optionalPlayer.isEmpty()) {
            logger.warn("[CONNECT_HUB] Jogador '{}' nao encontrado online na proxy.", playerName);
            return;
        }

        Player player = optionalPlayer.get();
        RegisteredServer targetServer = null;

        if (targetServerName != null && !targetServerName.isBlank()) {
            targetServer = server.getServer(targetServerName).orElse(null);
        }

        if (targetServer == null) {
            for (String hubName : config.getHubServers()) {
                Optional<RegisteredServer> hub = server.getServer(hubName);
                if (hub.isPresent()) {
                    targetServer = hub.get();
                    break;
                }
            }
        }

        if (targetServer == null) {
            targetServer = server.getServer(config.getFallbackHub()).orElse(null);
        }

        if (targetServer != null) {
            player.sendMessage(config.getMessage("hub-redirect"));
            player.createConnectionRequest(targetServer).fireAndForget();
        } else {
            logger.error("[CONNECT_HUB] Nenhum servidor de hub valido encontrado para redirecionar {}", playerName);
        }
    }

    public boolean connectPlayerToEvent(Player player) {
        Optional<RegisteredServer> eventServerOpt = getEventServer();
        if (eventServerOpt.isEmpty()) {
            player.sendMessage(config.getMessage("server-offline"));
            return false;
        }

        RegisteredServer eventServer = eventServerOpt.get();
        if (player.getCurrentServer().map(current -> current.getServer().equals(eventServer)).orElse(false)) {
            player.sendMessage(config.getMessage("already-connected"));
            return false;
        }

        player.sendMessage(config.getMessage("connecting"));
        player.createConnectionRequest(eventServer).fireAndForget();
        return true;
    }

    public ActiveEventInfo getActiveEvent() {
        return activeEvent;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }
}
