package com.litentry.litbot.TEEBot.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "discord_verify_msg")
@Data
public class DiscordVerifyMsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false, length = 40)
    private Long guildId;

    @Column(name = "user_id", nullable = false, length = 40)
    private Long userId;

    @Column(name = "channel_id", nullable = false, length = 40)
    private Long channelId;

    @Column(name = "msg_id", nullable = false, length = 40)
    private Long msgId;

    @Size(max = 255)
    @Column(name = "discord_user", nullable = false, length = 255)
    private String discordUser;

    @Size(max = 255)
    @Column(name = "msg", nullable = false, length = 255)
    private String msg;

    @Size(max = 255)
    @Column(name = "jump", nullable = false, length = 255)
    private String jump;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    @JsonIgnore
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonIgnore
    private Instant updatedAt = Instant.now();
}
