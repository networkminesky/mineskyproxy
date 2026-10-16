package net.mineskyproxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.mineskyproxy.hooks.BedrockHook;
import net.mineskyproxy.utils.TextUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PingCommand implements SimpleCommand {

    public static final String PERMISSION_OTHERS = "mineskyproxy.ping.others";

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
                long ping = getPing(p);
                s.sendMessage(TextUtil.parse("&aO seu ping é de " + ping + "ms " + getPlatformSuffix(p)));
            } else {
                if (!p.hasPermission(PERMISSION_OTHERS)) {
                    long ping = getPing(p);
                    s.sendMessage(TextUtil.parse("&aO seu ping é de " + ping + "ms " + getPlatformSuffix(p)));
                    return;
                }

                Player who = server.getPlayer(args[0]).orElse(null);
                if (who != null && who.isActive()) {
                    long ping = getPing(who);
                    p.sendMessage(TextUtil.parse("&aO ping deste jogador é de " + ping + "ms " + getPlatformSuffix(who)));
                } else {
                    p.sendMessage(TextUtil.parse("&cJogador não encontrado."));
                }
            }
        } else {
            if (args.length >= 1) {
                Player who = server.getPlayer(args[0]).orElse(null);
                if (who != null && who.isActive()) {
                    long ping = getPing(who);
                    s.sendMessage(TextUtil.parse("&aO ping de " + who.getUsername() + " é de " + ping + "ms " + getPlatformSuffix(who)));
                } else {
                    s.sendMessage(TextUtil.parse("&cJogador não encontrado."));
                }
            } else {
                s.sendMessage(TextUtil.parse("&cUtilize /ping <jogador>"));
            }
        }
    }

    private static long getPing(Player p) {
        Long bedrockPing = BedrockHook.getBedrockPing(p.getUniqueId());
        if (bedrockPing != null && bedrockPing >= 0) {
            return bedrockPing;
        }

        long ping = p.getPing();
        return ping >= 0 ? ping : 0;
    }

    private static String getPlatformSuffix(Player p) {
        if (BedrockHook.isBedrockPlayer(p.getUniqueId())) {
            return "&7(Bedrock)";
        }
        return "";
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