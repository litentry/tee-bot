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
@Table(name = "discord_guild")
@Data
public class DiscordGuild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 20, nullable = false)
    private Long id;

    @Column(name = "guild_id", nullable = false, length = 40)
    private Long guildId;

    @Size(max = 255)
    @Column(name = "guild_name", nullable = false, length = 255)
    private String guildName;

    @Size(max = 255)
    @Column(name = "guild_region", length = 255)
    private String guildRegion;

    @Column(name = "owner_id", nullable = false, length = 40)
    private Long ownerId;

    @Size(max = 255)
    @Column(name = "owner_name", nullable = false, length = 255)
    private String ownerName;

    @CreatedDate
    @Column(name = "bot_join_at")
    @JsonIgnore
    private Instant botJoinAt = Instant.now();

    @CreatedDate
    @Column(name = "created_at")
    @JsonIgnore
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonIgnore
    private Instant updatedAt = Instant.now();
}