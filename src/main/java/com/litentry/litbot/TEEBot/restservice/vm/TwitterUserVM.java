package com.litentry.litbot.TEEBot.restservice.vm;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TwitterUserVM {
    @NotNull
    String id;

    @NotNull
    String name;

    @NotNull
    String handler;
}
