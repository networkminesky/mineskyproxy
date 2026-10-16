package net.mineskyproxy.channel;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import net.mineskyproxy.managers.EventProxyManager;
import net.mineskyproxy.managers.RewardManager;
import net.mineskyproxy.managers.VanishManager;
import org.slf4j.Logger;

import java.util.UUID;

public class PluginMessageListener {

    private final EventProxyManager eventManager;
    private final RewardManager rewardManager;
    private final VanishManager vanishManager;
    private final Logger logger;

    public PluginMessageListener(EventProxyManager eventManager, RewardManager rewardManager, VanishManager vanishManager, Logger logger) {
        this.eventManager = eventManager;
        this.rewardManager = rewardManager;
        this.vanishManager = vanishManager;
        this.logger = logger;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(ProxyChannel.IDENTIFIER)) {
            return;
        }

        // Marca a mensagem como tratada pelo proxy para evitar encaminhamento indevido
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String subChannel = in.readUTF();

            switch (subChannel) {
                case ProxyChannel.MSG_EVENT_STARTED -> {
                    String eventId = in.readUTF();
                    String mapId = in.readUTF();
                    int playerCount = in.readInt();
                    eventManager.handleEventStarted(eventId, mapId, playerCount);
                }
                case ProxyChannel.MSG_EVENT_ENDED -> {
                    String eventId = in.readUTF();
                    String mapId = in.readUTF();
                    eventManager.handleEventEnded(eventId, mapId);
                }
                case ProxyChannel.MSG_EVENT_CANCELLED -> {
                    String eventId = in.readUTF();
                    String reason = in.readUTF();
                    eventManager.handleEventCancelled(eventId, reason);
                }
                case ProxyChannel.MSG_REWARD -> {
                    String playerName = in.readUTF();
                    int amount = in.readInt();
                    String eventId = in.readUTF();
                    int placement = in.readInt();
                    rewardManager.processReward(playerName, amount, eventId, placement);
                }
                case ProxyChannel.MSG_EVENT_STATUS -> {
                    String eventId = in.readUTF();
                    String state = in.readUTF();
                    int players = in.readInt();
                    eventManager.handleEventStatus(eventId, state, players);
                }
                case ProxyChannel.MSG_PONG -> {
                    String serverName = in.readUTF();
                    eventManager.handlePong(serverName);
                }
                case ProxyChannel.MSG_CONNECT_HUB -> {
                    String playerName = in.readUTF();
                    String targetServer = in.readUTF();
                    eventManager.handleConnectHub(playerName, targetServer);
                }
                case ProxyChannel.MSG_VANISH_UPDATE -> {
                    String uuidString = in.readUTF();
                    boolean vanished = in.readBoolean();
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        vanishManager.setVanished(uuid, vanished);
                        logger.debug("[VANISH] Atualizado status de vanish para {}: {}", uuid, vanished);
                    } catch (IllegalArgumentException ex) {
                        logger.warn("[VANISH] UUID inválido recebido em VanishUpdate: {}", uuidString);
                    }
                }
                default -> logger.warn("Sub-canal desconhecido recebido em minesky:proxy: {}", subChannel);
            }
        } catch (Exception e) {
            logger.error("Erro ao processar PluginMessage em minesky:proxy", e);
        }
    }
}