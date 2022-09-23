package com.litentry.litbot.TEEBot.command;

import org.jetbrains.annotations.Nullable;

public enum CommandCategory {
    //ADMINISTRATION("Admin", "https://icons.iconarchive.com/icons/dtafalonso/android-lollipop/512/Settings-icon.png", "\u2699"),
    //WALLET("Wallet", "https://icons.iconarchive.com/icons/custom-icon-design/pretty-office-11/128/coins-icon.png", "\uD83E\uDE99"),
    //INVITE("Invite", "https://icons.iconarchive.com/icons/streamlineicons/streamline-ux-free/128/user-female-add-icon.png", "\uD83D\uDCEC"),
    POLKADOT("Polkadot", "https://icons.iconarchive.com/icons/cjdowner/cryptocurrency-flat/128/Verify-CRED-icon.png", "\uD83D\uDD25"),
    INFORMATION("Information", "https://icons.iconarchive.com/icons/graphicloads/100-flat/128/information-icon.png", "\uD83E\uDEA7"),
    //SOCIAL("Socail", "https://icons.iconarchive.com/icons/designbolts/seo/128/Social-Media-Marketing-icon.png", "✨"),
    UNLISTED(null);

    private final String name;
    private final String iconUrl;
    private final String emote;

    CommandCategory(String search) {
        this.name = search;
        this.iconUrl = null;
        this.emote = null;
    }

    CommandCategory(String search, String url, String emote) {
        this.name = search;
        this.iconUrl = url;
        this.emote = emote;
    }

    public static @Nullable CommandCategory fromSearch(String input) {
        for (final CommandCategory value : values()) {
            if (input.equalsIgnoreCase(value.name()) || input.equalsIgnoreCase(value.getName())) {
                return value;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    public String getEmote() {
        return emote;
    }
}
