package net.mineskyproxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.minesky.api.messaging.Messaging;
import net.mineskyproxy.addons.Emojis;
import net.mineskyproxy.channel.ProxyChannel;
import net.mineskyproxy.channel.ProxyNetworkListener;
import net.mineskyproxy.commands.*;
import net.mineskyproxy.config.ProxyConfig;
import net.mineskyproxy.events.PlayerEvents;
import net.mineskyproxy.managers.EventProxyManager;
import net.mineskyproxy.managers.RewardManager;
import net.mineskyproxy.managers.TellManager;
import net.mineskyproxy.managers.VanishManager;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "mineskyproxy",
        name = "MineSkyProxy",
        version = "1.0.0-RELEASE",
        description = "Plugin para funções gerais de Proxy e integração com MineSkyEvents",
        authors = {"Bruno C.", "Drawn"}
)
public class MineSkyProxy {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;

    private ProxyConfig config;
    private RewardManager rewardManager;
    private EventProxyManager eventProxyManager;
    private VanishManager vanishManager;
    private TellManager tellManager;
    private ProxyNetworkListener networkListener;

    private static MineSkyProxy instance;

    @Inject
    public MineSkyProxy(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        logger.info("================================================");
        logger.info("   MineSkyProxy - Inicializando Plugin Proxy    ");
        logger.info("================================================");

        this.config = new ProxyConfig(dataDirectory, logger);
        this.config.load();

        Emojis.load(dataDirectory, logger);

        this.rewardManager = new RewardManager(proxy, config, logger);
        this.eventProxyManager = new EventProxyManager(proxy, config, logger, rewardManager);
        this.vanishManager = new VanishManager();
        this.tellManager = new TellManager();

        if (Messaging.hasObserver()) {
            this.networkListener = new ProxyNetworkListener(proxy, eventProxyManager, rewardManager, vanishManager, logger);
            Messaging.registerListener("MineSkyProxy", this.networkListener);
            logger.info("Mainframe Messaging registrado com sucesso no canal '{}'.", ProxyChannel.MAIN_CHANNEL);
        } else {
            logger.warn("Mainframe Messaging desabilitado ou sem conexao ativa com o MongoDB!");
        }

        proxy.getEventManager().register(this, new PlayerEvents());
        logger.info("Listeners do Velocity registrados com sucesso.");

        CommandManager commandManager = proxy.getCommandManager();

        CommandMeta eventoMeta = commandManager.metaBuilder("evento")
                .aliases("eventos", "event")
                .plugin(this)
                .build();
        commandManager.register(eventoMeta, new EventoCommand(proxy, config, eventProxyManager));
        logger.info("Comando /evento (/eventos, /event) registrado.");

        CommandMeta staffChatMeta = commandManager.metaBuilder("staffchat")
                .aliases("s")
                .plugin(this)
                .build();
        commandManager.register(staffChatMeta, new StaffChatCommand(proxy, config, vanishManager, logger));
        logger.info("Comando /staffchat (/s) registrado.");

        TellCommand tellCommand = new TellCommand(proxy, tellManager, vanishManager);
        CommandMeta tellMeta = commandManager.metaBuilder("tell")
                .aliases("t", "w", "msg", "whisper", "pm", "dm", "m")
                .plugin(this)
                .build();
        commandManager.register(tellMeta, tellCommand);

        CommandMeta replyMeta = commandManager.metaBuilder("r")
                .aliases("reply")
                .plugin(this)
                .build();
        commandManager.register(replyMeta, tellCommand);

        CommandMeta spyMeta = commandManager.metaBuilder("socialspy")
                .aliases("spy")
                .plugin(this)
                .build();
        commandManager.register(spyMeta, tellCommand);
        logger.info("Comandos de Tell (/tell, /r, /socialspy) registrados.");

        CommandMeta pingMeta = commandManager.metaBuilder("ping")
                .aliases("latency")
                .plugin(this)
                .build();
        commandManager.register(pingMeta, new PingCommand(proxy));
        logger.info("Comando /ping registrado.");

        CommandMeta anunciarMeta = commandManager.metaBuilder("anunciar")
                .aliases("aviso")
                .plugin(this)
                .build();
        commandManager.register(anunciarMeta, new AnunciarCommand(proxy));
        logger.info("Comando /anunciar registrado.");

        final BrigadierCommand gList = GlobalList.createBrigadierCommand(proxy);
        commandManager.register(commandManager.metaBuilder(gList)
                .aliases("globallist")
                .build(), gList);
        logger.info("Comando /globallist registrado.");

        logger.info("MineSkyProxy inicializado e pronto para operacao!");
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (vanishManager != null) {
            vanishManager.remove(event.getPlayer().getUniqueId());
        }
        if (tellManager != null) {
            tellManager.removePlayer(event.getPlayer().getUniqueId());
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("MineSkyProxy sendo desativado...");

        if (networkListener != null) {
            Messaging.unregisterListener(networkListener);
        }

        if (vanishManager != null) {
            vanishManager.clear();
        }
        logger.info("MineSkyProxy desativado.");
    }

    public ProxyServer getServer() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ProxyConfig getConfig() {
        return config;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public EventProxyManager getEventProxyManager() {
        return eventProxyManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public TellManager getTellManager() {
        return tellManager;
    }

    public static MineSkyProxy getInstance() {
        return instance;
    }
}