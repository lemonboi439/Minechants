package me.lemonboi439.minechants.util;

public final class ServerConfigState {
    private static volatile boolean disableExplosiveOnServer;

    private ServerConfigState() {
    }

    public static boolean isExplosiveDisabledOnServer() {
        return disableExplosiveOnServer;
    }

    public static void setDisableExplosiveOnServer(boolean disableExplosiveOnServer) {
        ServerConfigState.disableExplosiveOnServer = disableExplosiveOnServer;
    }

    public static void reset() {
        disableExplosiveOnServer = false;
    }
}








