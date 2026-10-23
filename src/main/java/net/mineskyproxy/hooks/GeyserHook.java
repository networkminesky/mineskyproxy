package net.mineskyproxy.hooks;

import com.velocitypowered.api.proxy.Player;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.bedrock.camera.CameraFade;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.awt.*;
import java.util.UUID;

public class GeyserHook {

    public static void sendFade(UUID uuid, float fadeIn, float duration, float fadeOut) {
        GeyserConnection connection = GeyserApi.api().connectionByUuid(uuid);
        if(connection == null)
            return;

        connection.camera().sendCameraFade(CameraFade.builder()
                        .fadeInSeconds(fadeIn)
                        .fadeHoldSeconds(duration)
                        .fadeOutSeconds(fadeOut)
                        .color(Color.BLACK)
                .build());
    }

}
