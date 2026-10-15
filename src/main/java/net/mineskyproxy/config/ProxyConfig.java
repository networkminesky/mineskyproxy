package net.mineskyproxy.config;

import net.kyori.adventure.text.Component;
import net.mineskyproxy.utils.TextUtil;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProxyConfig {

    private final Path dataDirectory;
    private final Logger logger;
    private final Path configFile;

    private String eventServer = "eventos";
    private List<String> hubServers = Arrays.asList("hub", "lobby");
    private String fallbackHub = "hub";

    private boolean broadcastOnStart = true;
    private boolean broadcastOnEnd = true;
    private boolean broadcastOnCancel = true;
    private boolean sendTitleOnStart = true;
    private int titleFadeIn = 10;
    private int titleStay = 70;
    private int titleFadeOut = 20;

    private boolean notifyReward = true;
    private boolean logRewards = true;

    private final Map<String, String> messages = new HashMap<>();
    private String prefix = "<gradient:#FF5555:#FFAA00><b>MineSky</b></gradient> <dark_gray>»</dark_gray>";

    public ProxyConfig(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.configFile = dataDirectory.resolve("config.yml");
    }

    public void load() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            if (!Files.exists(configFile)) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                    } else {
                        Files.createFile(configFile);
                    }
                }
            }

            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(configFile)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            ConfigurationNode root = loader.load();

            this.eventServer = root.node("event-server").getString("eventos");
            this.fallbackHub = root.node("fallback-hub").getString("hub");

            List<String> loadedHubs = root.node("hub-servers").getList(String.class);
            if (loadedHubs != null && !loadedHubs.isEmpty()) {
                this.hubServers = loadedHubs;
            }

            ConfigurationNode announcements = root.node("announcements");
            this.broadcastOnStart = announcements.node("broadcast-on-start").getBoolean(true);
            this.broadcastOnEnd = announcements.node("broadcast-on-end").getBoolean(true);
            this.broadcastOnCancel = announcements.node("broadcast-on-cancel").getBoolean(true);
            this.sendTitleOnStart = announcements.node("send-title-on-start").getBoolean(true);
            this.titleFadeIn = announcements.node("title-fade-in").getInt(10);
            this.titleStay = announcements.node("title-stay").getInt(70);
            this.titleFadeOut = announcements.node("title-fade-out").getInt(20);

            ConfigurationNode rewards = root.node("rewards");
            this.notifyReward = rewards.node("notify-player").getBoolean(true);
            this.logRewards = rewards.node("log-rewards").getBoolean(true);

            ConfigurationNode msgNode = root.node("messages");
            this.prefix = msgNode.node("prefix").getString(this.prefix);

            messages.clear();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : msgNode.childrenMap().entrySet()) {
                String key = String.valueOf(entry.getKey());
                String val = entry.getValue().getString();
                if (val != null) {
                    messages.put(key, val);
                }
            }

            logger.info("Configuracao carregada com sucesso.");
        } catch (IOException e) {
            logger.error("Erro ao carregar config.yml!", e);
        }
    }

    public String getRawMessage(String key, String def) {
        return messages.getOrDefault(key, def);
    }

    public Component getMessage(String key, Map<String, String> placeholders) {
        String raw = messages.getOrDefault(key, "");
        if (raw.isEmpty()) {
            return Component.empty();
        }

        Map<String, String> map = new HashMap<>();
        map.put("prefix", prefix);
        if (placeholders != null) {
            map.putAll(placeholders);
        }

        return TextUtil.parse(raw, map);
    }

    public Component getMessage(String key) {
        return getMessage(key, Collections.emptyMap());
    }

    public String getEventServer() {
        return eventServer;
    }

    public List<String> getHubServers() {
        return hubServers;
    }

    public String getFallbackHub() {
        return fallbackHub;
    }

    public boolean isBroadcastOnStart() {
        return broadcastOnStart;
    }

    public boolean isBroadcastOnEnd() {
        return broadcastOnEnd;
    }

    public boolean isBroadcastOnCancel() {
        return broadcastOnCancel;
    }

    public boolean isSendTitleOnStart() {
        return sendTitleOnStart;
    }

    public int getTitleFadeIn() {
        return titleFadeIn;
    }

    public int getTitleStay() {
        return titleStay;
    }

    public int getTitleFadeOut() {
        return titleFadeOut;
    }

    public boolean isNotifyReward() {
        return notifyReward;
    }

    public boolean isLogRewards() {
        return logRewards;
    }

    public String getPrefix() {
        return prefix;
    }
}
