package net.mineskyproxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.mineskyproxy.utils.TextUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PingCommand implements SimpleCommand {

    public static final String PERMISSION_OTHERS = "mineskycore.ping.others";

    private final ProxyServer server;

    public PingCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource s = invocation.source();
        String[] args = invocation.arguments();

        if (s instanceof Player p) {
            if (args.length < 1) {
                s.sendMessage(TextUtil.parse("&aO seu ping é de " + getPing(p) + "ms "));
            } else {
                if (!p.hasPermission(PERMISSION_OTHERS)) {
                    s.sendMessage(TextUtil.parse("&aO seu ping é de " + getPing(p) + "ms "));
                    return;
                }

                Player who = server.getPlayer(args[0]).orElse(null);
                if (who != null && who.isActive()) {
                    p.sendMessage(TextUtil.parse("&aO ping deste jogador é de " + getPing(who) + "ms "));
                } else {
                    p.sendMessage(TextUtil.parse("&cJogador não encontrado."));
                }
            }
        } else {
            if (args.length >= 1) {
                Player who = server.getPlayer(args[0]).orElse(null);
                if (who != null && who.isActive()) {
                    s.sendMessage(TextUtil.parse("&aO ping de " + who.getUsername() + " é de " + getPing(who) + "ms "));
                } else {
                    s.sendMessage(TextUtil.parse("&cJogador não encontrado."));
                }
            } else {
                s.sendMessage(TextUtil.parse("&cUtilize /ping <jogador>"));
            }
        }
    }

    private static long getPing(Player p) {
        return p.getPing();
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1 && invocation.source().hasPermission(PERMISSION_OTHERS)) {
            String current = args.length == 1 ? args[0].toLowerCase() : "";
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}