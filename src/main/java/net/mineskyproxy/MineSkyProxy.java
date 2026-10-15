package net.mineskyproxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.mineskyproxy.channel.PluginMessageListener;
import net.mineskyproxy.channel.ProxyChannel;
import net.mineskyproxy.commands.EventoCommand;
import net.mineskyproxy.config.ProxyConfig;
import net.mineskyproxy.managers.EventProxyManager;
import net.mineskyproxy.managers.RewardManager;
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

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ProxyConfig config;
    private RewardManager rewardManager;
    private EventProxyManager eventProxyManager;

    @Inject
    public MineSkyProxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("================================================");
        logger.info("   MineSkyProxy - Inicializando Plugin Proxy    ");
        logger.info("================================================");

        // 1. Carrega configuracao
        this.config = new ProxyConfig(dataDirectory, logger);
        this.config.load();

        // 2. Registra o canal de comunicacao com o Paper
        server.getChannelRegistrar().register(ProxyChannel.IDENTIFIER);
        logger.info("Canal '{}' registrado com sucesso no Velocity.", ProxyChannel.CHANNEL_NAME);

        // 3. Inicializa os gerenciadores
        this.rewardManager = new RewardManager(server, config, logger);
        this.eventProxyManager = new EventProxyManager(server, config, logger, rewardManager);

        // 4. Registra o listener de Plugin Messages
        server.getEventManager().register(this, new PluginMessageListener(eventProxyManager, rewardManager, logger));
        logger.info("Listener de Plugin Messaging registrado.");

        // 5. Registra o comando /evento e apelidos
        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("evento")
                .aliases("eventos", "event")
                .plugin(this)
                .build();

        commandManager.register(meta, new EventoCommand(server, config, eventProxyManager));
        logger.info("Comando /evento (/eventos, /event) registrado.");

        logger.info("MineSkyProxy inicializado e pronto para operacao!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("MineSkyProxy sendo desativado...");
        server.getChannelRegistrar().unregister(ProxyChannel.IDENTIFIER);
        logger.info("MineSkyProxy desativado.");
    }

    public ProxyServer getServer() {
        return server;
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
}
