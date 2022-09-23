package com.litentry.litbot.TEEBot.restservice.vm;

import java.time.Instant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class DiscordVerifyVM {
    @NotNull
    @Size(min = 4, max = 256)
    private String handler;

    @NotNull
    private Long guildId;

    @NotNull
    private Long channleId;

    @NotNull
    private Long msgId;

    @NotNull
    private Long userId;

    @NotNull
    @Size(min = 4, max = 256)
    private String msg;

    @NotNull
    private Instant msgCreatedAt;

    @NotNull
    private String url;

    @NotNull
    private String authorization;
}
