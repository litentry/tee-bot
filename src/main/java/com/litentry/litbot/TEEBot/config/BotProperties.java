package com.litentry.litbot.TEEBot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bots")
@Data
public class BotProperties {
    private String token;
    private String prefix;
    private String footer;
    private String botInvite;
    private String discordInvite;
    private String webHook;
    private String socialLink;
    private Boolean shouldPolkadotWallet;
    private Boolean shouldCaptcha;
    private Long polkaVerifiedRoleId;
    private Long polkaVerifiedChanId;
    private Long captchaVerifiedRoleId;
    private Long mainGuildId;
}
