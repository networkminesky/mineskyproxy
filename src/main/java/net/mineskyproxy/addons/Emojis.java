package net.mineskyproxy.addons;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.mineskyproxy.utils.TextUtil;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Emojis {

    private static final Map<String, String> EMOJIS = new LinkedHashMap<>();
    private static Path emojisPath;

    static {
        loadFromResource(null);
    }

    public static void load(Path dataDirectory, Logger logger) {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            emojisPath = dataDirectory.resolve("emojis.yml");
            if (!Files.exists(emojisPath)) {
                try (InputStream in = Emojis.class.getResourceAsStream("/emojis.yml")) {
                    if (in != null) {
                        Files.copy(in, emojisPath);
                    }
                }
            }

            if (Files.exists(emojisPath)) {
                YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                        .path(emojisPath)
                        .nodeStyle(NodeStyle.BLOCK)
                        .build();

                ConfigurationNode root = loader.load();
                loadFromNode(root);
                if (logger != null) {
                    logger.info("Carregados {} emojis de emojis.yml.", EMOJIS.size());
                }
                return;
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Erro ao carregar emojis.yml da pasta de dados, tentando carregar dos recursos...", e);
            }
        }

        loadFromResource(logger);
    }

    public static void loadFromResource(Logger logger) {
        try (InputStream in = Emojis.class.getResourceAsStream("/emojis.yml")) {
            if (in != null) {
                YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                        .source(() -> new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
                        .nodeStyle(NodeStyle.BLOCK)
                        .build();

                ConfigurationNode root = loader.load();
                loadFromNode(root);
                if (logger != null) {
                    logger.info("Carregados {} emojis a partir dos recursos.", EMOJIS.size());
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Erro ao carregar emojis.yml dos recursos!", e);
            }
        }
    }

    private static void loadFromNode(ConfigurationNode root) {
        EMOJIS.clear();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : root.childrenMap().entrySet()) {
            String key = String.valueOf(entry.getKey());
            String val = entry.getValue().getString();
            if (val != null) {
                EMOJIS.put(key, val);
            }
        }
    }

    public static Set<String> getKeys() {
        return EMOJIS.keySet();
    }

    public static String getString(String key) {
        return EMOJIS.get(key);
    }

    public static Map<String, String> getEmojis() {
        return Collections.unmodifiableMap(EMOJIS);
    }

    public static boolean hasEmojiPermission(CommandSource p, String emojiKey) {
        if (p == null) {
            return true;
        }
        return p.hasPermission("mineskycore.emoji." + emojiKey) || p.hasPermission("mineskyproxy.emoji." + emojiKey);
    }

    public static String format(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        for (String key : getKeys()) {
            String icon = getString(key);
            if (icon != null) {
                s = s.replace(":" + key + ":", "§f" + icon + "§e");
            }
        }

        return s;
    }

    public static Component formatToComponent(String s, String color, CommandSource p) {
        if (s == null || s.isEmpty()) {
            return Component.empty();
        }

        Component baseColor = TextUtil.parse(color);
        Style baseStyle = baseColor.style();

        Component finalComponent = Component.empty().style(baseStyle).append(baseColor);

        String novo = s;

        String startMarker = "µ189204";
        String endMarker = "µ24723";

        for (String e : getKeys()) {
            novo = novo.replace(":" + e + ":", startMarker + e + endMarker);
        }

        String[] emotes = novo.split(Pattern.quote(startMarker));

        Component firstPart = TextUtil.parse(emotes[0]);
        if (firstPart.style().color() == null && baseStyle.color() != null) {
            firstPart = firstPart.style(baseStyle.merge(firstPart.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
        }
        finalComponent = finalComponent.append(firstPart);

        if (emotes.length > 1) {
            for (int i = 1; i < emotes.length; i++) {
                String[] splitEnd = emotes[i].split(Pattern.quote(endMarker), 2);
                String emojiKey = splitEnd[0];
                String remainingText = (splitEnd.length > 1) ? splitEnd[1] : "";

                if (!hasEmojiPermission(p, emojiKey)) {
                    finalComponent = finalComponent.append(TextUtil.parse("&7:" + emojiKey + ":"));
                } else {
                    String icon = getString(emojiKey);
                    if (icon == null) {
                        icon = "";
                    }
                    Component emo = TextUtil.parse("&f" + icon)
                            .hoverEvent(HoverEvent.showText(TextUtil.parse("&7:" + emojiKey + ":")));

                    finalComponent = finalComponent.append(emo);
                }

                Component remainingPart = TextUtil.parse(remainingText);
                if (remainingPart.style().color() == null && baseStyle.color() != null) {
                    remainingPart = remainingPart.style(baseStyle.merge(remainingPart.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
                }
                finalComponent = finalComponent.append(baseColor).append(remainingPart);
            }
        }

        return finalComponent;
    }
}
