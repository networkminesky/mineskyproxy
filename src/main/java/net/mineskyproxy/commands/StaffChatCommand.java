package net.mineskyproxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mineskyproxy.addons.Emojis;
import net.mineskyproxy.config.ProxyConfig;
import net.mineskyproxy.hooks.LuckPermsHook;
import net.mineskyproxy.managers.VanishManager;
import net.mineskyproxy.utils.DiscordWebhook;
import net.mineskyproxy.utils.TextUtil;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StaffChatCommand implements SimpleCommand {

    public static final String PERMISSION = "mineskyproxy.staffchat";
    private static final TextColor BASE_COLOR = TextColor.fromHexString("#ffa8a8");

    private final ProxyServer server;
    private final ProxyConfig config;
    private final VanishManager vanishManager;
    private final Logger logger;

    public StaffChatCommand(ProxyServer server, ProxyConfig config, VanishManager vanishManager, Logger logger) {
        this.server = server;
        this.config = config;
        this.vanishManager = vanishManager;
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource s = invocation.source();
        String[] args = invocation.arguments();

        if (!s.hasPermission(PERMISSION)) {
            s.sendMessage(TextUtil.parse("&cVocê não pode executar esse comando."));
            return;
        }

        if (args.length < 1) {
            s.sendMessage(TextUtil.parse("&cVocê deve indicar uma mensagem ao comando."));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        String name = (s instanceof Player player) ? player.getUsername() : "CONSOLE";
        sendStaffMessage(s, name, message);
    }

    private Component getPlayerComponent(Player player) {
        String prefix = LuckPermsHook.getPrefix(player.getUniqueId());

        return TextUtil.parse(prefix + player.getUsername())
                .hoverEvent(HoverEvent.showText(Component.text("Clique para teleportar-se.", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.suggestCommand("/tp " + player.getUsername()))
                .font(Style.DEFAULT_FONT);
    }

    private Component formatMessage(String msg, CommandSource sender) {
        return Emojis.formatToComponent(msg, "<#ffa8a8>", sender);
    }

    public void sendStaffMessage(CommandSource s, String origin, String msg) {
        final Component playerComponent;
        final String serverName;
        final boolean isVanished;

        if (s instanceof Player p) {
            playerComponent = getPlayerComponent(p);
            serverName = p.getCurrentServer()
                    .map(conn -> conn.getServerInfo().getName())
                    .orElse("Desconhecido");
            isVanished = vanishManager.isVanished(p.getUniqueId());
        } else {
            playerComponent = Component.text("CONSOLE", Style.style(NamedTextColor.DARK_RED, TextDecoration.BOLD));
            serverName = "Proxy";
            isVanished = false;
        }

        Component hoverInfo = Component.text()
                .append(Component.text("Servidor: ", NamedTextColor.GRAY))
                .append(Component.text(serverName, NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("Vanish: ", NamedTextColor.GRAY))
                .append(isVanished
                        ? Component.text("sim", NamedTextColor.GREEN)
                        : Component.text("não", NamedTextColor.RED))
                .build();

        Component packet = Component.text("[s] ", NamedTextColor.DARK_RED)
                .append(playerComponent)
                .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                .append(formatMessage(msg, s))
                .hoverEvent(HoverEvent.showText(hoverInfo));

        server.getConsoleCommandSource().sendMessage(packet);

        server.getAllPlayers().stream()
                .filter(bs -> bs.hasPermission(PERMISSION))
                .forEach(bs -> bs.sendMessage(packet));

        String webhookUrl = config.getStaffChatWebhook();
        if (webhookUrl != null && !webhookUrl.isBlank()) {
            new DiscordWebhook(webhookUrl)
                    .setUsername("[/s] " + origin + " (" + serverName + ")")
                    .setAvatarUrl("https://mc-heads.net/avatar/" + origin + "/64")
                    .setContent(msg)
                    .sendAsync(logger);
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(PERMISSION);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length > 0) {
            String current = args[args.length - 1];
            if (current.startsWith(":")) {
                CommandSource source = invocation.source();
                return Emojis.getKeys().stream()
                        .filter(k -> Emojis.hasEmojiPermission(source, k))
                        .map(k -> ":" + k + ":")
                        .filter(k -> k.toLowerCase().startsWith(current.toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}