package net.mineskyproxy.managers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.mineskyproxy.config.ProxyConfig;
import net.mineskyproxy.model.EventType;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class RewardManager {

    private final ProxyServer server;
    private final ProxyConfig config;
    private final Logger logger;
    private Consumer<RewardRecord> customRewardHandler;

    public record RewardRecord(String playerName, int amount, String eventId, int placement, long timestamp) {}

    public RewardManager(ProxyServer server, ProxyConfig config, Logger logger) {
        this.server = server;
        this.config = config;
        this.logger = logger;
    }

    public void setCustomRewardHandler(Consumer<RewardRecord> handler) {
        this.customRewardHandler = handler;
    }

    public void processReward(String playerName, int amount, String eventId, int placement) {
        RewardRecord record = new RewardRecord(playerName, amount, eventId, placement, System.currentTimeMillis());

        if (config.isLogRewards()) {
            logger.info("[REWARD] Premiacao recebida: Jogador '{}' ganhou ${} no evento '{}' ({}o lugar)",
                    playerName, amount, eventId, placement);
        }

        // Executa handler customizado (ex: insercao em banco de dados, API web, etc.)
        if (customRewardHandler != null) {
            try {
                customRewardHandler.accept(record);
            } catch (Exception e) {
                logger.error("[REWARD] Falha ao processar reward customizado para {}", playerName, e);
            }
        }

        // Se o jogador estiver online na proxy, envia a mensagem de notificacao
        if (config.isNotifyReward()) {
            Optional<Player> optionalPlayer = server.getPlayer(playerName);
            if (optionalPlayer.isPresent()) {
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
    }
}
