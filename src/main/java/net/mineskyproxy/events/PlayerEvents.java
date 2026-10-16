package net.mineskyproxy.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.mineskyproxy.managers.TellManager;

public class PlayerEvents {
    @Subscribe
    public void onJoin(PostLoginEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("minesky.spy")) {
            TellManager.getSpy(p.getUniqueId(), spyAtivado -> {
                if (spyAtivado) {
                    TellManager.addPlayer(p.getUniqueId());
                }
            });
        }
    }
}
