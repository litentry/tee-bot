package com.litentry.litbot.TEEBot.common;

import org.jetbrains.annotations.Nullable;

public enum ModActionType {
    BAN("Ban"),
    KICK("Kick"),
    WARN("Warn");

    private final String search;

    ModActionType(String text) {
        this.search = text;
    }

    public String getText() {
        return this.search;
    }

    @Nullable
    public static ModActionType fromSearch(String text) {
        for (ModActionType b : ModActionType.values()) {
            if (b.search.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
