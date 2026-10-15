package net.mineskyproxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.mineskyproxy.config.ProxyConfig;
import net.mineskyproxy.managers.EventProxyManager;
import net.mineskyproxy.model.ActiveEventInfo;
import net.mineskyproxy.model.EventType;

import java.util.*;
import java.util.stream.Collectors;

public class EventoCommand implements SimpleCommand {

    public static final String ADMIN_PERMISSION = "minesky.events.admin";

    private final ProxyServer server;
    private final ProxyConfig config;
    private final EventProxyManager eventManager;

    public EventoCommand(ProxyServer server, ProxyConfig config, EventProxyManager eventManager) {
        this.server = server;
        this.config = config;
        this.eventManager = eventManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            if (source instanceof Player player) {
                eventManager.connectPlayerToEvent(player);
            } else {
                sendHelp(source);
            }
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "entrar", "join" -> {
                if (source instanceof Player player) {
                    eventManager.connectPlayerToEvent(player);
                } else {
                    source.sendMessage(config.getMessage("player-only"));
                }
            }

            case "status" -> {
                ActiveEventInfo active = eventManager.getActiveEvent();
                source.sendMessage(config.getMessage("status-header"));
                if (active != null) {
                    Map<String, String> map = new HashMap<>();
                    map.put("event_id", active.getEventId());
                    map.put("event_name", active.getDisplayName());
                    map.put("map_id", active.getMapId());
                    map.put("state", active.getState());
                    map.put("players", String.valueOf(active.getPlayerCount()));
                    map.put("duration", String.valueOf(active.getDurationSeconds()));
                    source.sendMessage(config.getMessage("status-active", map));
                } else {
                    source.sendMessage(config.getMessage("status-none"));
                }

                if (source.hasPermission(ADMIN_PERMISSION)) {
                    Map<String, String> pingMap = Map.of("server", config.getEventServer());
                    source.sendMessage(config.getMessage("status-ping", pingMap));
                    eventManager.sendPing(latency -> {
                        Map<String, String> pongMap = Map.of("server", config.getEventServer(), "ms", String.valueOf(latency));
                        source.sendMessage(config.getMessage("status-pong", pongMap));
                    });
                }
            }

            case "start" -> {
                if (!source.hasPermission(ADMIN_PERMISSION)) {
                    source.sendMessage(config.getMessage("admin-only"));
                    return;
                }

                if (args.length < 2) {
                    source.sendMessage(config.getMessage("help-start"));
                    return;
                }

                String eventId = args[1].toLowerCase();
                String mapId = args.length >= 3 ? args[2] : "";

                boolean sent = eventManager.sendStartEvent(eventId, mapId, true);
                if (sent) {
                    source.sendMessage(config.getMessage("prefix").append(
                            Component.text(" §aComando START_EVENT enviado para " + eventId + (mapId.isEmpty() ? "" : " (mapa: " + mapId + ")") + "!")
                    ));
                } else {
                    source.sendMessage(config.getMessage("server-offline"));
                }
            }

            case "stop" -> {
                if (!source.hasPermission(ADMIN_PERMISSION)) {
                    source.sendMessage(config.getMessage("admin-only"));
                    return;
                }

                boolean sent = eventManager.sendStopEvent();
                if (sent) {
                    source.sendMessage(config.getMessage("prefix").append(
                            Component.text(" §aComando STOP_EVENT enviado para o servidor de eventos.")
                    ));
                } else {
                    source.sendMessage(config.getMessage("server-offline"));
                }
            }

            case "forcestop" -> {
                if (!source.hasPermission(ADMIN_PERMISSION)) {
                    source.sendMessage(config.getMessage("admin-only"));
                    return;
                }

                boolean sent = eventManager.sendForceStop();
                if (sent) {
                    source.sendMessage(config.getMessage("prefix").append(
                            Component.text(" §cComando FORCE_STOP enviado com sucesso!")
                    ));
                } else {
                    source.sendMessage(config.getMessage("server-offline"));
                }
            }

            case "enviar", "send" -> {
                if (!source.hasPermission(ADMIN_PERMISSION)) {
                    source.sendMessage(config.getMessage("admin-only"));
                    return;
                }

                if (args.length < 2) {
                    source.sendMessage(config.getMessage("help-enviar"));
                    return;
                }

                String targetName = args[1];
                Optional<Player> targetPlayer = server.getPlayer(targetName);
                if (targetPlayer.isEmpty()) {
                    source.sendMessage(config.getMessage("player-not-found", Map.of("player", targetName)));
                    return;
                }

                eventManager.connectPlayerToEvent(targetPlayer.get());
                source.sendMessage(config.getMessage("sent-player", Map.of("player", targetPlayer.get().getUsername())));
            }

            case "puxartodos" -> {
                if (!source.hasPermission(ADMIN_PERMISSION)) {
                    source.sendMessage(config.getMessage("admin-only"));
                    return;
                }

                Optional<RegisteredServer> eventServerOpt = eventManager.getEventServer();
                if (eventServerOpt.isEmpty()) {
                    source.sendMessage(config.getMessage("server-offline"));
                    return;
                }

                RegisteredServer target = eventServerOpt.get();
                int count = 0;
                for (Player p : server.getAllPlayers()) {
                    boolean alreadyThere = p.getCurrentServer()
                            .map(curr -> curr.getServer().equals(target))
                            .orElse(false);
                    if (!alreadyThere) {
                        p.sendMessage(config.getMessage("connecting"));
                        p.createConnectionRequest(target).fireAndForget();
                        count++;
                    }
                }

                source.sendMessage(config.getMessage("sent-all"));
            }

            case "reload" -> {
                if (!source.hasPermission(ADMIN_PERMISSION)) {
                    source.sendMessage(config.getMessage("admin-only"));
                    return;
                }

                config.load();
                source.sendMessage(config.getMessage("reloaded"));
            }

            default -> sendHelp(source);
        }
    }

    private void sendHelp(CommandSource source) {
        source.sendMessage(config.getMessage("help-header"));
        source.sendMessage(config.getMessage("help-entrar"));
        source.sendMessage(config.getMessage("help-status"));

        if (source.hasPermission(ADMIN_PERMISSION)) {
            source.sendMessage(config.getMessage("help-start"));
            source.sendMessage(config.getMessage("help-stop"));
            source.sendMessage(config.getMessage("help-forcestop"));
            source.sendMessage(config.getMessage("help-enviar"));
            source.sendMessage(config.getMessage("help-puxartodos"));
            source.sendMessage(config.getMessage("help-reload"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length <= 1) {
            String current = args.length == 1 ? args[0].toLowerCase() : "";
            List<String> completions = new ArrayList<>();
            completions.add("entrar");
            completions.add("status");

            if (source.hasPermission(ADMIN_PERMISSION)) {
                completions.add("start");
                completions.add("stop");
                completions.add("forcestop");
                completions.add("enviar");
                completions.add("puxartodos");
                completions.add("reload");
            }

            return completions.stream()
                    .filter(s -> s.startsWith(current))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start") && source.hasPermission(ADMIN_PERMISSION)) {
            String current = args[1].toLowerCase();
            return Arrays.stream(EventType.values())
                    .map(EventType::getId)
                    .filter(id -> id.startsWith(current))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("enviar") && source.hasPermission(ADMIN_PERMISSION)) {
            String current = args[1].toLowerCase();
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
