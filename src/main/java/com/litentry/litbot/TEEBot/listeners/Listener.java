package com.litentry.litbot.TEEBot.listeners;

import com.litentry.litbot.TEEBot.bots.Bot;
import com.litentry.litbot.TEEBot.common.PresenceType;
import com.litentry.litbot.TEEBot.config.BotProperties;
import com.litentry.litbot.TEEBot.domain.DiscordGuild;
import com.litentry.litbot.TEEBot.repository.DiscordGuildRepository;
import com.litentry.litbot.TEEBot.utils.BotUtils;
import com.litentry.litbot.TEEBot.utils.WebhookUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class Listener implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    private Bot bot;
    private BotProperties botProperties;
    private final WebhookUtil webhookUtil;
    private DiscordGuildRepository discordGuildRepository;

    public Listener(Bot bot, BotProperties botProperties, DiscordGuildRepository discordGuildRepository) {
        this.bot = bot;
        this.botProperties = botProperties;
        this.discordGuildRepository = discordGuildRepository;
        this.webhookUtil = new WebhookUtil(botProperties.getWebHook());
    }

    public void onReady(@Nonnull ReadyEvent event) {
        final JDA jda = event.getJDA();

        for (Guild guild : jda.getGuilds()) {
            updateSlashCmds(guild);
        }

        // Update Presence
        bot.getThreadpool().scheduleWithFixedDelay(() -> BotUtils.updatePresence(jda, PresenceType.MEMBERS), 0, 30,
                TimeUnit.MINUTES);
        bot.getThreadpool().scheduleWithFixedDelay(() -> bot.getCmdHandler().cleanCooldowns(), 1, 1, TimeUnit.DAYS);

        log.info("{} is ready, Watching {} guilds", jda.getSelfUser().getAsTag(), jda.getGuilds().size());
    }

    private void onGuildJoin(@Nonnull GuildJoinEvent event) {
        final Guild guild = event.getGuild();
        guild
                .retrieveOwner()
                .queue(owner -> {
                    log.info("Guild Joined - GuildID: {} | OwnerId: {} | Members: {}", guild.getId(), owner.getId(),
                            guild.getMemberCount());
                    webhookUtil.sendWebhook(owner, guild, WebhookUtil.Action.JOIN);

                    updateSlashCmds(guild);

                    // register guild
                    Optional<DiscordGuild> opDiscordGuild = discordGuildRepository.findByGuildId(guild.getIdLong());
                    DiscordGuild discordGuild = new DiscordGuild();
                    if (opDiscordGuild.isPresent()) {
                        discordGuild = opDiscordGuild.get();
                    } else {
                        discordGuild.setGuildId(guild.getIdLong());
                    }
                    discordGuild.setGuildName(guild.getName());
                    discordGuild.setGuildRegion(guild.getDescription());
                    discordGuild.setOwnerId(owner.getIdLong());
                    discordGuild.setOwnerName(owner.getUser().getAsTag());
                    discordGuild.setBotJoinAt(Instant.now());
                    discordGuildRepository.save(discordGuild);
                });
    }

    private void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        final Guild guild = event.getGuild();
        guild
                .retrieveOwner()
                .queue(owner -> {
                    log.info("Guild Left - GuildID: {} | OwnerId: {} | Members: {}", guild.getId(), owner.getId(),
                            guild.getMemberCount());
                    webhookUtil.sendWebhook(owner, guild, WebhookUtil.Action.LEAVE);
                });
    }

    private void updateSlashCmds(Guild guild) {
        guild
                .updateCommands()
                .addCommands(Commands.slash("start", "start captcha challenge, to verify that you are not a bot."))
                .addCommands(Commands.slash("connect", "connect ID-Hub account and finish the verification."))
                .addCommands(Commands.slash("verify", "automatically query your predetermined roles from ID-Hub."))
                // .addCommands(Commands.slash("modmail", "mod mail tests"))
                .queue();
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            this.onReady((ReadyEvent) event);
        } else if (event instanceof GuildJoinEvent) {
            this.onGuildJoin((GuildJoinEvent) event);
        } else if (event instanceof GuildLeaveEvent) {
            this.onGuildLeave((GuildLeaveEvent) event);
        }
    }
}
