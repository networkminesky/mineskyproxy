package net.mineskyproxy.managers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.minesky.api.messaging.Messaging;
import net.minesky.core.messaging.MessageType;
import net.mineskyproxy.config.ProxyConfig;
import net.mineskyproxy.model.EventType;
import org.bson.Document;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class RewardManager {

    private final ProxyServer server;
    private final ProxyConfig config;
    private final Logger logger;
    private Consumer<RewardRecord> customRewardHandler;

    public record RewardRecord(String playerName, UUID playerUUID, int amount, String eventId, int placement, long timestamp) {}

    public RewardManager(ProxyServer server, ProxyConfig config, Logger logger) {
        this.server = server;
        this.config = config;
        this.logger = logger;
    }

    public void setCustomRewardHandler(Consumer<RewardRecord> handler) {
        this.customRewardHandler = handler;
    }

    public void processReward(String playerName, int amount, String eventId, int placement) {
        Optional<Player> optionalPlayer = server.getPlayer(playerName);
        UUID playerUUID = optionalPlayer.map(Player::getUniqueId)
                .orElseGet(() -> UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes()));

        RewardRecord record = new RewardRecord(playerName, playerUUID, amount, eventId, placement, System.currentTimeMillis());

        if (config.isLogRewards()) {
            logger.info("[REWARD] Premiacao recebida: Jogador '{}' ({}) ganhou ${} no evento '{}' ({}o lugar)",
                    playerName, playerUUID, amount, eventId, placement);
        }

        Document rewardDoc = new Document("playerName", playerName)
                .append("uuid", playerUUID.toString())
                .append("amount", amount)
                .append("eventId", eventId)
                .append("placement", placement);

        Messaging.publish("minesky:network", "reward-vault", MessageType.DOCUMENT, rewardDoc);
        logger.info("[REWARD] Recompensa de ${} para '{}' enviada via Mainframe Messaging.", amount, playerName);

        if (customRewardHandler != null) {
            try {
                customRewardHandler.accept(record);
            } catch (Exception e) {
                logger.error("[REWARD] Falha ao processar reward customizado para {}", playerName, e);
            }
        }

        if (config.isNotifyReward() && optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            placeholders.put("amount", String.valueOf(amount));
            placeholders.put("event_id", eventId);
            placeholders.put("event_name", EventType.getDisplayNameOrDefault(eventId));
            placeholders.put("placement", String.valueOf(placement));

            player.sendMessage(config.getMessage("reward-notification", placeholders));
        }
    }

    @Deprecated
    public void flushPendingRewards() {}
}