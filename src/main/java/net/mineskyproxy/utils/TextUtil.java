package net.mineskyproxy.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private TextUtil() {}

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        if (input.startsWith("#") && input.length() == 7) {
            TextColor hexColor = TextColor.fromHexString(input);
            if (hexColor != null) {
                return Component.empty().color(hexColor);
            }
        }

        if (input.contains("<") && input.contains(">")) {
            return MINI_MESSAGE.deserialize(input);
        }

        if (input.contains("&") || input.contains("§")) {
            return LEGACY_SERIALIZER.deserialize(input.replace('§', '&'));
        }

        return MINI_MESSAGE.deserialize(input);
    }

    public static Component parse(String input, Map<String, String> placeholders) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        String formatted = input;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue() != null ? entry.getValue() : "";
                formatted = formatted.replace("<" + key + ">", val);
                formatted = formatted.replace("{" + key + "}", val);
                formatted = formatted.replace("%" + key + "%", val);
            }
        }

        return parse(formatted);
    }
}
