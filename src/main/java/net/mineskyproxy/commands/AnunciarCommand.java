package net.mineskyproxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.mineskyproxy.utils.TextUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnunciarCommand implements SimpleCommand {

    public static final String PERMISSION = "mineskycore.anunciar";

    private static final Sound LEVELUP_SOUND = Sound.sound(
            Key.key("minecraft:entity.player.levelup"),
            Sound.Source.MASTER,
            1f,
            0f
    );

    private final ProxyServer server;

    public AnunciarCommand(ProxyServer server) {
        this.server = server;
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

        String rawMessage = "&b" + String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        Component messageComponent = TextUtil.parse(rawMessage);

        Component announcement = Component.text(" \n§f§lMine§b§lSky §8§l: §b")
                .append(messageComponent)
                .append(Component.text("\n "))
                .hoverEvent(HoverEvent.showText(Component.text("§a✔ Mensagem oficial do servidor!")));

        for (Player p : server.getAllPlayers()) {
            p.sendMessage(announcement);
            try {
                p.playSound(LEVELUP_SOUND);
            } catch (Exception ignored) {}
        }

        server.getConsoleCommandSource().sendMessage(announcement);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(PERMISSION);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return Collections.emptyList();
    }
}