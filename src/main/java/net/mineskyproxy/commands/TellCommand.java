package net.mineskyproxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mineskyproxy.addons.Emojis;
import net.mineskyproxy.managers.TellManager;
import net.mineskyproxy.managers.VanishManager;
import net.mineskyproxy.utils.TextUtil;

import java.util.*;
import java.util.stream.Collectors;

public class TellCommand implements SimpleCommand {

    private static final TextColor ROSA_CLARO = TextColor.fromHexString("#ff7396");
    private static final TextColor ROSA_ESCURO = TextColor.fromHexString("#ffd4de");

    public static final String PERMISSION_SEND_TO_VANISHED = "mineskyproxy.tell.sendtovanished";
    public static final String PERMISSION_SOCIAL_SPY = "minesky.spy";

    private static final Sound BELL_SOUND = Sound.sound(
            Key.key("minecraft:block.note_block.bell"),
            Sound.Source.MASTER,
            0.5f,
            1.2f
    );

    private final ProxyServer server;
    private final TellManager tellManager;
    private final VanishManager vanishManager;

    public TellCommand(ProxyServer server, TellManager tellManager, VanishManager vanishManager) {
        this.server = server;
        this.tellManager = tellManager;
        this.vanishManager = vanishManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource s = invocation.source();
        String alias = invocation.alias().toLowerCase();
        String[] args = invocation.arguments();

        if (!(s instanceof Player sender)) {
            s.sendMessage(TextUtil.parse("&cNão pode executar esse comando."));
            return;
        }

        if (alias.equals("socialspy") || alias.equals("spy")) {
            if (!sender.hasPermission(PERMISSION_SOCIAL_SPY)) {
                sender.sendMessage(TextUtil.parse("&cVocê não tem permissão para usar esse comando."));
                return;
            }
            boolean enabled = tellManager.toggleSpy(sender.getUniqueId());
            if (enabled) {
                sender.sendMessage(TextUtil.parse("&aSocialSpy ativado!"));
            } else {
                sender.sendMessage(TextUtil.parse("&cSocialSpy desativado!"));
            }
            return;
        }

        if (alias.equals("r") || alias.equals("reply")) {
            if (args.length < 1) {
                sender.sendMessage(Component.text("Uso correto: /r <mensagem>", NamedTextColor.RED));
                return;
            }

            UUID targetUUID = tellManager.getLastTell(sender.getUniqueId());
            Player target = targetUUID != null ? server.getPlayer(targetUUID).orElse(null) : null;

            if (target == null || (vanishManager.isVanished(target.getUniqueId()) && !sender.hasPermission(PERMISSION_SEND_TO_VANISHED))) {
                sender.sendMessage(TextUtil.parse("&cVocê não tem ninguém para responder ou o jogador deslogou."));
                return;
            }

            String message = String.join(" ", args).replace('&', '§');
            sendTell(sender, target, message);
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso correto: /tell <jogador> <mensagem>", NamedTextColor.RED));
            return;
        }

        String targetName = args[0];
        Player target = server.getPlayer(targetName).orElse(null);

        if (target == null || (vanishManager.isVanished(target.getUniqueId()) && !sender.hasPermission(PERMISSION_SEND_TO_VANISHED))) {
            sender.sendMessage(TextUtil.parse("&cEsse jogador não existe ou não está online."));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).replace('&', '§');
        sendTell(sender, target, message);
    }

    private void sendTell(Player source, Player who, String msg) {
        if (who == null || (vanishManager.isVanished(who.getUniqueId()) && !source.hasPermission(PERMISSION_SEND_TO_VANISHED))) {
            source.sendMessage(Component.text("Esse jogador não existe ou não está online.", NamedTextColor.RED));
            return;
        }

        if (source.equals(who)) {
            source.sendMessage(Component.text("Você não pode enviar mensagens para você mesmo.", NamedTextColor.RED));
            return;
        }

        String serverName = who.getCurrentServer()
                .map(conn -> " (" + conn.getServerInfo().getName() + ")")
                .orElse("");

        Component emojiMessage = Emojis.formatToComponent(msg, "<#ff7396><i>", source);

        Component paraQuemTaRecebendo = Component.text("[", ROSA_CLARO)
                .append(Component.text("t", ROSA_CLARO))
                .append(Component.text("] ", ROSA_CLARO))
                .append(Component.text(source.getUsername(), Style.style(ROSA_CLARO, TextDecoration.ITALIC, TextDecoration.BOLD)))
                .append(Component.text(" -> ", NamedTextColor.DARK_GRAY))
                .append(Component.text("VOCÊ", Style.style(ROSA_ESCURO, TextDecoration.ITALIC)))
                .append(Component.text(":", NamedTextColor.DARK_GRAY))
                .append(Component.space())
                .append(emojiMessage)
                .hoverEvent(HoverEvent.showText(Component.text("Clique para responder rapidamente." + serverName, NamedTextColor.YELLOW)))
                .clickEvent(ClickEvent.suggestCommand("/tell " + source.getUsername() + " "));

        Component paraQuemEnviou = Component.text("[", ROSA_CLARO)
                .append(Component.text("t", ROSA_CLARO))
                .append(Component.text("] ", ROSA_CLARO))
                .append(Component.text("VOCÊ", Style.style(ROSA_ESCURO, TextDecoration.ITALIC)))
                .append(Component.text(" -> ", NamedTextColor.DARK_GRAY))
                .append(Component.text(who.getUsername(), Style.style(ROSA_CLARO, TextDecoration.ITALIC, TextDecoration.BOLD)))
                .append(Component.text(":", NamedTextColor.DARK_GRAY))
                .append(Component.space())
                .append(emojiMessage);

        Component spyMessage = Component.text("(Spy)", Style.style(NamedTextColor.DARK_GRAY, TextDecoration.ITALIC))
                .append(Component.text(" ", NamedTextColor.GRAY))
                .append(Component.text("[", ROSA_CLARO))
                .append(Component.text("t", ROSA_CLARO))
                .append(Component.text("] ", ROSA_CLARO))
                .append(Component.text(source.getUsername(), Style.style(ROSA_ESCURO, TextDecoration.ITALIC, TextDecoration.BOLD)))
                .append(Component.text(" -> ", NamedTextColor.GRAY))
                .append(Component.text(who.getUsername(), Style.style(ROSA_CLARO, TextDecoration.ITALIC, TextDecoration.BOLD)))
                .append(Component.text(":", NamedTextColor.DARK_GRAY))
                .append(Component.space())
                .append(emojiMessage)
                .append(Component.text(" ", NamedTextColor.GRAY));

        try {
            who.playSound(BELL_SOUND, Sound.Emitter.self());
        } catch (Exception ignored) {}

        source.sendMessage(paraQuemEnviou);
        who.sendMessage(paraQuemTaRecebendo);

        tellManager.setLastTell(source.getUniqueId(), who.getUniqueId());
        tellManager.setLastTell(who.getUniqueId(), source.getUniqueId());

        server.getAllPlayers().stream()
                .filter(bs -> bs.hasPermission(PERMISSION_SOCIAL_SPY))
                .filter(bs -> tellManager.hasSpy(bs.getUniqueId()))
                .filter(bs -> !bs.equals(source) && !bs.equals(who))
                .forEach(bs -> bs.sendMessage(spyMessage));

        server.getConsoleCommandSource().sendMessage(spyMessage);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String alias = invocation.alias().toLowerCase();
        String[] args = invocation.arguments();

        if (alias.equals("socialspy") || alias.equals("spy")) {
            return Collections.emptyList();
        }

        if (alias.equals("r") || alias.equals("reply")) {
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

        if (args.length <= 1) {
            String current = args.length == 1 ? args[0].toLowerCase() : "";
            boolean canSeeVanished = invocation.source().hasPermission(PERMISSION_SEND_TO_VANISHED);

            return server.getAllPlayers().stream()
                    .filter(p -> canSeeVanished || !vanishManager.isVanished(p.getUniqueId()))
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .sorted()
                    .collect(Collectors.toList());
        }

        String current = args[args.length - 1];
        if (current.startsWith(":")) {
            CommandSource source = invocation.source();
            return Emojis.getKeys().stream()
                    .filter(k -> Emojis.hasEmojiPermission(source, k))
                    .map(k -> ":" + k + ":")
                    .filter(k -> k.toLowerCase().startsWith(current.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}