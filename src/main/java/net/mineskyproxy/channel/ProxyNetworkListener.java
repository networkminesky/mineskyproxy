package net.mineskyproxy.channel;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minesky.core.messaging.MessageType;
import net.minesky.core.messaging.MessagingListener;
import net.minesky.core.messaging.NetworkMessage;
import net.mineskyproxy.commands.StaffChatCommand;
import net.mineskyproxy.managers.EventProxyManager;
import net.mineskyproxy.managers.RewardManager;
import net.mineskyproxy.managers.VanishManager;
import org.bson.Document;
import org.slf4j.Logger;

import java.util.UUID;

public class ProxyNetworkListener extends MessagingListener {

    private final ProxyServer proxy;
    private final EventProxyManager eventManager;
    private final RewardManager rewardManager;
    private final VanishManager vanishManager;
    private final Logger logger;

    public ProxyNetworkListener(ProxyServer proxy, EventProxyManager eventManager, RewardManager rewardManager, VanishManager vanishManager, Logger logger) {
        super(ProxyChannel.MAIN_CHANNEL);
        this.proxy = proxy;
        this.eventManager = eventManager;
        this.rewardManager = rewardManager;
        this.vanishManager = vanishManager;
        this.logger = logger;
    }

    @Override
    public void onMessage(NetworkMessage message) {
        String subChannel = message.subchannel();

        try {
            switch (subChannel) {
                case ProxyChannel.SUB_EVENT_STARTED -> {
                    Document doc = message.asDocument();
                    String eventId = doc.getString("eventId");
                    String mapId = doc.getString("mapId");
                    int playerCount = doc.getInteger("playerCount", 0);
                    eventManager.handleEventStarted(eventId, mapId, playerCount);
                }

                case ProxyChannel.SUB_EVENT_ENDED -> {
                    Document doc = message.asDocument();
                    String eventId = doc.getString("eventId");
                    String mapId = doc.getString("mapId");
                    eventManager.handleEventEnded(eventId, mapId);
                }

                case ProxyChannel.SUB_EVENT_CANCELLED -> {
                    Document doc = message.asDocument();
                    String eventId = doc.getString("eventId");
                    String reason = doc.getString("reason");
                    eventManager.handleEventCancelled(eventId, reason);
                }

                case ProxyChannel.SUB_REWARD -> {
                    Document doc = message.asDocument();
                    String playerName = doc.getString("playerName");
                    int amount = doc.getInteger("amount");
                    String eventId = doc.getString("eventId");
                    int placement = doc.getInteger("placement", 1);
                    rewardManager.processReward(playerName, amount, eventId, placement);
                }

                case ProxyChannel.SUB_EVENT_STATUS -> {
                    Document doc = message.asDocument();
                    String eventId = doc.getString("eventId");
                    String state = doc.getString("state");
                    int players = doc.getInteger("players", 0);
                    eventManager.handleEventStatus(eventId, state, players);
                }

                case ProxyChannel.SUB_PONG -> {
                    String serverName = (message.type() == MessageType.DOCUMENT)
                            ? message.asDocument().getString("serverName")
                            : message.asString();
                    eventManager.handlePong(serverName);
                }

                case ProxyChannel.SUB_CONNECT_HUB -> {
                    Document doc = message.asDocument();
                    String playerName = doc.getString("playerName");
                    String targetServer = doc.getString("targetServer");
                    eventManager.handleConnectHub(playerName, targetServer);
                }

                case ProxyChannel.SUB_VANISH_UPDATE -> {
                    Document doc = message.asDocument();
                    String uuidString = doc.getString("uuid");
                    boolean vanished = doc.getBoolean("vanished", false);
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        vanishManager.setVanished(uuid, vanished);
                        logger.debug("[VANISH] Atualizado status de vanish para {}: {}", uuid, vanished);
                    } catch (IllegalArgumentException ex) {
                        logger.warn("[VANISH] UUID inválido recebido em vanish-update: {}", uuidString);
                    }
                }

                case ProxyChannel.SUB_DISCORD -> {
                    Document doc = message.asDocument();
                    String jsonMessage = doc.getString("component");
                    Component chatComponent = GsonComponentSerializer.gson().deserialize(jsonMessage);

                    proxy.getConsoleCommandSource().sendMessage(chatComponent);

                    for (Player player : proxy.getAllPlayers()) {
                        if (!player.hasPermission(StaffChatCommand.PERMISSION)) {
                            continue;
                        }
                        player.sendMessage(chatComponent);
                    }
                }

                default -> logger.warn("Sub-canal desconhecido recebido em {}: {}", ProxyChannel.MAIN_CHANNEL, subChannel);
            }
        } catch (Exception e) {
            logger.error("Erro ao processar mensagem do Mainframe no sub-canal '{}'", subChannel, e);
        }
    }

    @Override
    public void onMessage(String subchannel, String value) {

    }
}