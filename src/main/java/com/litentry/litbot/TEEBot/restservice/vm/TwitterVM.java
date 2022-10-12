package com.litentry.litbot.TEEBot.restservice.vm;

import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TwitterVM {
    @NotNull
    String id;

    @NotNull
    String authorId;

    @NotNull
    OffsetDateTime createdAt;

    @NotNull
    String text;
}
