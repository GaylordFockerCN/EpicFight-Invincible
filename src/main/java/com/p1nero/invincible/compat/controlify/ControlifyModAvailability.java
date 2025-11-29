package com.p1nero.invincible.compat.controlify;

public final class ControlifyModAvailability {
    private ControlifyModAvailability() {
    }

    private static boolean isModInstalled;

    public static boolean isModInstalled() {
        return isModInstalled;
    }

    public static void setIsModInstalled(final boolean isModInstalled) {
        ControlifyModAvailability.isModInstalled = isModInstalled;
    }
}
