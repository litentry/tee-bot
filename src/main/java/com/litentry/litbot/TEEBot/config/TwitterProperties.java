package com.litentry.litbot.TEEBot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "twitter")
@Data
public class TwitterProperties {
    private String key;
    private String secret;
    private String bearToken;
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
}
