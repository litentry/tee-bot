package com.litentry.litbot.TEEBot.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.litentry.litbot.TEEBot.domain.DiscordGuild;


@Transactional
@Repository
public interface DiscordGuildRepository extends JpaRepository<DiscordGuild, Long> {
    Optional<DiscordGuild> findByGuildId(Long guildId);
}

