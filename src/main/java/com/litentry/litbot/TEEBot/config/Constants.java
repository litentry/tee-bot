package com.litentry.litbot.TEEBot.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";
    public static final String SYSTEM = "system";
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String SYMBOL_LIT = "LIT";

    // transaction type
    public static final long TRANSACTION_TYPE_IN = 0L;
    public static final long TRANSACTION_TYPE_OUT = 1L;

    // blockchain type
    public static final long BLOCKCHAIN_NULL = 0L;
    public static final long BLOCKCHAIN_POLKADOT = 1L;
    public static final long BLOCKCHAIN_ETHEREUM = 2L;
    public static final long BLOCKCHAIN_BSC = 3L;

    // blockchain decimal
    public static final int CHAIN_DECIMAL_ETHEREUM = 18;
    public static final int CHAIN_DECIMAL_POLKADOT = 10;
    public static final int CHAIN_DECIMAL_KUSAMA = 12;
    public static final int CHAIN_LITENTRY_STAGING = 12;

    // parachain chain type
    public static final long PARACHAIN_WESTEND = 0L;
    public static final long PARACHAIN_KUSAMA = 1L;
    public static final long PARACHAIN_DOT = 2L;
    public static final long PARACHAIN_LITENTRY_STAGING = 3L;

    // ---------------------for discord bots-----------------------------
    public static final int USER_TYPE_ORIGIN = 0;
    public static final int USER_TYPE_DISCORD = 1; // for discord users

    // Invites Reward
    public static final long MAX_REWARD_INVITES = 1000L;
    public static final int MINIMUM_INVITES = 5;
    public static final String REWARDS_SYMBOL_LIT = "LIT";

    // Emoji's
    public static final String CUBE_BULLET = "\u2752";
    public static final String ARROW_BULLET = "\u00BB";
    public static final String ARROW = "\u276F";
    public static final String TICK = "\u2713";
    public static final String X_MARK = "\u2715";
    public static final String CURRENCY = "LIT";
    public static final int REACTION_TIMEOUT_SECONDS = 15;
    public static final String EMOTE_RETURN_UP_LEVEL = "⬅";

    // Embed Colors
    public static final int BOT_EMBED = 0x068ADD;
    public static final int TRANSPARENT_EMBED = 0x36393F;
    public static final int SUCCESS_EMBED = 0x00A56A;
    public static final int ERROR_EMBED = 0xD61A3C;

    // Error Message
    public static final String API_ERROR = "Unexpected Backend Error! Try again later.";

    // Captcha
    public static final long MAX_RETRY_TIMES = 3L;
    public static final int CAPTCHA_LENGTH = 6;
    public static final String CAPTCHA_VERIFIED_ROLE = "CaptchaVerified";

    // Discord
    public static final String DISCRIMINATOR = "#";

    private Constants() {
    }
}
