package net.mineskyproxy.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import net.mineskyproxy.MineSkyProxy;
import net.mineskyproxy.managers.TellManager;

public class PlayerEvents {

    @Subscribe
    public void onJoin(PostLoginEvent e) {
        Player p = e.getPlayer();

        if (p.hasPermission("minesky.spy")) {
            TellManager.getSpy(p.getUniqueId(), spyAtivado -> {
                if (spyAtivado && p.isActive()) {
                    TellManager.addPlayer(p.getUniqueId());
                }
            });
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent e) {
        if (e.getServer().getServerInfo().getName().equalsIgnoreCase(MineSkyProxy.getInstance().getConfig().getSurvivalServer())) {
            MineSkyProxy.getInstance().getRewardManager().flushPendingRewards();
        }
    }
}