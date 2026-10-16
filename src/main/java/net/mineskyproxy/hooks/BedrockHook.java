package net.mineskyproxy.hooks;

import java.lang.reflect.Method;
import java.util.UUID;

public final class BedrockHook {

    private static Boolean floodgatePresent = null;
    private static Boolean geyserPresent = null;

    private static Object floodgateInstance = null;
    private static Method isFloodgatePlayerMethod = null;
    private static Method getFloodgatePlayerMethod = null;

    private static Method geyserConnectionByUuidMethod = null;

    private BedrockHook() {}

    public static boolean isBedrockPlayer(UUID uuid) {
        if (uuid == null) return false;

        // 1. Tentar via Floodgate API
        if (checkFloodgate()) {
            try {
                Object result = isFloodgatePlayerMethod.invoke(floodgateInstance, uuid);
                if (result instanceof Boolean b && b) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        // 2. Tentar via Geyser API
        if (checkGeyser()) {
            try {
                Object conn = geyserConnectionByUuidMethod.invoke(null, uuid);
                if (conn != null) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        return false;
    }

    public static Long getBedrockPing(UUID uuid) {
        if (uuid == null) return null;

        // Tentar via Geyser API (GeyserApi.api().connectionByUuid(uuid).ping())
        if (checkGeyser()) {
            try {
                Object conn = geyserConnectionByUuidMethod.invoke(null, uuid);
                if (conn != null) {
                    Method pingMethod = conn.getClass().getMethod("ping");
                    Object pingResult = pingMethod.invoke(conn);
                    if (pingResult instanceof Number n) {
                        return n.longValue();
                    }
                }
            } catch (Throwable ignored) {}
        }

        return null;
    }

    private static boolean checkFloodgate() {
        if (floodgatePresent == null) {
            try {
                Class<?> floodgateClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
                Method getInstanceMethod = floodgateClass.getMethod("getInstance");
                floodgateInstance = getInstanceMethod.invoke(null);
                isFloodgatePlayerMethod = floodgateClass.getMethod("isFloodgatePlayer", UUID.class);
                getFloodgatePlayerMethod = floodgateClass.getMethod("getPlayer", UUID.class);
                floodgatePresent = (floodgateInstance != null);
            } catch (Throwable t) {
                floodgatePresent = false;
            }
        }
        return floodgatePresent != null && floodgatePresent;
    }

    private static boolean checkGeyser() {
        if (geyserPresent == null) {
            try {
                Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
                Method apiMethod = geyserApiClass.getMethod("api");
                Object geyserApiInstance = apiMethod.invoke(null);
                geyserConnectionByUuidMethod = geyserApiInstance.getClass().getMethod("connectionByUuid", UUID.class);
                geyserPresent = (geyserApiInstance != null);
            } catch (Throwable t) {
                geyserPresent = false;
            }
        }
        return geyserPresent != null && geyserPresent;
    }
}