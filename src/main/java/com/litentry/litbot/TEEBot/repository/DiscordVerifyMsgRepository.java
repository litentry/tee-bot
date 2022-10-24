package com.litentry.litbot.TEEBot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.litentry.litbot.TEEBot.domain.DiscordVerifyMsg;

public interface DiscordVerifyMsgRepository extends JpaRepository<DiscordVerifyMsg, Long> {
    long countByGuildIdAndDiscordUser(Long guildId, String discordUser);

    List<DiscordVerifyMsg> findAllByGuildIdAndDiscordUserOrderByCreatedAtDesc(Long guildId, String discordUser);

    List<DiscordVerifyMsg> findAllByGuildIdAndDiscordUserAndChannelIdOrderByCreatedAtDesc(Long guildId,
            String discordUser, Long channelId);

    void deleteById(Long id);
}
