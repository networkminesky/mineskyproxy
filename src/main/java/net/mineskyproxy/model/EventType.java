package net.mineskyproxy.model;

import java.util.Arrays;
import java.util.Optional;

public enum EventType {
    SPLEEF("spleef", "Spleef", "Diário"),
    TIJOLAO_WARS("tijolao-wars", "Tijolão Wars", "Diário"),
    RUNNERS("runners", "Runners", "Diário"),
    CORRIDA("corrida", "Corrida", "Diário"),
    CORRIDA_BARCO("corrida-barco", "Corrida de Barco", "Diário"),
    TNT_RUN("tnt-run", "TNT RUN", "Diário"),
    TNT_TAG("tnt-tag", "TNT TAG", "Diário"),
    SUMO("sumo", "Sumo", "Diário"),
    CORRIDA_ELYTRA("corrida-elytra", "Corrida de Elytra", "Diário"),
    MINI_WARS("mini-wars", "Mini Wars", "Agendado"),
    ESCONDE_ESCONDE("esconde-esconde", "Esconde-Esconde", "Agendado");

    private final String id;
    private final String displayName;
    private final String category;

    EventType(String id, String displayName, String category) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    public static Optional<EventType> fromId(String id) {
        if (id == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(type -> type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(id))
                .findFirst();
    }

    public static String getDisplayNameOrDefault(String id) {
        return fromId(id).map(EventType::getDisplayName).orElse(id);
    }
}
