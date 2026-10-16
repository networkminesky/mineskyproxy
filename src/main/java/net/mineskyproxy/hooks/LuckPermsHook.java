package net.mineskyproxy.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public final class LuckPermsHook {

    private static Boolean available = null;

    private LuckPermsHook() {}

    public static boolean isAvailable() {
        if (available == null) {
            try {
                Class.forName("net.luckperms.api.LuckPermsProvider");
                available = (LuckPermsProvider.get() != null);
            } catch (Throwable t) {
                available = false;
            }
        }
        return available;
    }

    public static String getPrefix(UUID uuid) {
        if (uuid == null) return "";
        try {
            if (isAvailable()) {
                LuckPerms lp = LuckPermsProvider.get();
                User user = lp.getUserManager().getUser(uuid);
                if (user != null) {
                    String prefix = user.getCachedData().getMetaData().getPrefix();
                    return prefix != null ? prefix : "";
                }
            }
        } catch (Throwable ignored) {
        }
        return "";
    }
}