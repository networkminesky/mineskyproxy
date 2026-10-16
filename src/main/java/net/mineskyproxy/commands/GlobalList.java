package net.mineskyproxy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

public class GlobalList {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> staffNode = BrigadierCommand.literalArgumentBuilder("glist")
                .requires(a -> a.hasPermission("mineskyproxy.glist"))
                .executes(context -> {
                    CommandSource source = context.getSource();

                    Component t = Component.text("§bNo total: " + proxy.getAllPlayers().size() + " jogadores online.");

                    for (RegisteredServer s : proxy.getAllServers()) {
                        int amount = s.getPlayersConnected().size();
                        t = t.appendNewline().append(
                                Component.text("§b" + s.getServerInfo().getName() + " §3(" + amount + ") ")
                        );
                    }

                    source.sendMessage(t);

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(staffNode);
    }
}