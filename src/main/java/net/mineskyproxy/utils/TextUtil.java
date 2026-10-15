package net.mineskyproxy.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private TextUtil() {}

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        // Se contiver tags de MiniMessage ou for formato padrão
        if (input.contains("<") && input.contains(">")) {
            return MINI_MESSAGE.deserialize(input);
        }

        // Se usar códigos & legados
        if (input.contains("&")) {
            return LEGACY_SERIALIZER.deserialize(input);
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
