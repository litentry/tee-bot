package com.litentry.litbot.TEEBot.bots;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.litentry.litbot.TEEBot.config.BotProperties;
import com.litentry.litbot.TEEBot.config.Constants;
import com.litentry.litbot.TEEBot.handlers.CommandHandler;
import com.litentry.litbot.TEEBot.handlers.PrivateMsgHandler;
import com.litentry.litbot.TEEBot.handlers.ReactionHandler;
import com.litentry.litbot.TEEBot.listeners.Listener;
import com.litentry.litbot.TEEBot.repository.DiscordGuildRepository;
import com.litentry.litbot.TEEBot.service.DiscordVerifyMsgService;
import com.litentry.litbot.TEEBot.service.PolkadotVerifyService;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class Bot {
    private final ScheduledExecutorService threadpool;
    private final EventWaiter waiter;
    private final CommandHandler cmdHandler;
    private final ReactionHandler reactionHandler;
    private PrivateMsgHandler privateMsgHandler;
    private BotProperties botProperties;
    private PolkadotVerifyService polkadotVerifyService;
    private DiscordGuildRepository discordGuildRepository;
    private DiscordVerifyMsgService discordVerifyMsgService;

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    public Bot(BotProperties botProperties, PolkadotVerifyService polkadotVerifyService,
            DiscordGuildRepository discordGuildRepository, DiscordVerifyMsgService discordVerifyMsgService)
            throws LoginException {
        this.botProperties = botProperties;
        this.polkadotVerifyService = polkadotVerifyService;
        this.discordGuildRepository = discordGuildRepository;
        this.discordVerifyMsgService = discordVerifyMsgService;

        // db = DBMaker.fileDB("discord_bot.db").checksumHeaderBypass()
        // .allocateStartSize(10 * 1024 * 1024).allocateIncrement(1024 * 1024).make();

        int threadpoolSize = 10;
        threadpool = Executors.newScheduledThreadPool(threadpoolSize);
        waiter = new EventWaiter();
        reactionHandler = new ReactionHandler(this);
        privateMsgHandler = new PrivateMsgHandler(this.botProperties, this.polkadotVerifyService,
                this.discordVerifyMsgService);
        privateMsgHandler.setWaiter(waiter);
        cmdHandler = new CommandHandler(this, this.botProperties, this.polkadotVerifyService);

        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder().setColor(Constants.BOT_EMBED).setFooter(botProperties.getFooter()));

        JDA jda = JDABuilder
                .createDefault(botProperties.getToken())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES)
                .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                .enableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.MEMBER_OVERRIDES)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMaxBufferSize(40960)
                .addEventListeners(new Listener(this, this.botProperties, discordGuildRepository), waiter, cmdHandler,
                        reactionHandler,
                        privateMsgHandler)
                .setStatus(OnlineStatus.ONLINE)
                .disableIntents(GatewayIntent.GUILD_MESSAGE_TYPING)
                .setActivity(Activity.playing("Booting..."))
                .build();

        // wait for loading all resources
        // jda.awaitReady();

        log.info("started LITBot: {}", this);

        polkadotVerifyService.setJDA(jda);
    }

    public ScheduledExecutorService getThreadpool() {
        return threadpool;
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public CommandHandler getCmdHandler() {
        return cmdHandler;
    }

    public ReactionHandler getReactionHandler() {
        return reactionHandler;
    }
}
