package net.mineskyproxy.hooks;

import java.lang.reflect.Method;
import java.util.UUID;

public final class BedrockHook {

    private static Boolean floodgatePresent = null;
    private static Boolean geyserPresent = null;

    private static Object floodgateInstance = null;
    private static Method isFloodgatePlayerMethod = null;

    private static Object geyserApiInstance = null;
    private static Method geyserConnectionByUuidMethod = null;

    private BedrockHook() {}

    public static boolean isBedrockPlayer(UUID uuid) {
        if (uuid == null) return false;

        if (checkFloodgate()) {
            try {
                Object result = isFloodgatePlayerMethod.invoke(floodgateInstance, uuid);
                if (result instanceof Boolean b && b) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        if (checkGeyser()) {
            try {
                Object conn = geyserConnectionByUuidMethod.invoke(geyserApiInstance, uuid);
                if (conn != null) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        return false;
    }

    public static Long getBedrockPing(UUID uuid) {
        if (uuid == null) return null;

        if (checkGeyser()) {
            try {
                Object conn = geyserConnectionByUuidMethod.invoke(geyserApiInstance, uuid);
                if (conn != null) {
                    for (String methodName : new String[]{"ping", "getPing", "latency", "getLatency"}) {
                        try {
                            Method pingMethod = conn.getClass().getMethod(methodName);
                            Object pingResult = pingMethod.invoke(conn);
                            if (pingResult instanceof Number n) {
                                long val = n.longValue();
                                if (val > 0) {
                                    return val;
                                }
                            }
                        } catch (NoSuchMethodException ignored) {}
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

                geyserApiInstance = apiMethod.invoke(null);

                if (geyserApiInstance != null) {
                    geyserConnectionByUuidMethod = geyserApiInstance.getClass().getMethod("connectionByUuid", UUID.class);
                    geyserPresent = true;
                } else {
                    geyserPresent = false;
                }
            } catch (Throwable t) {
                geyserPresent = false;
            }
        }
        return geyserPresent != null && geyserPresent;
    }
}