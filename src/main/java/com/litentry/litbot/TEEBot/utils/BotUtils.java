package com.litentry.litbot.TEEBot.utils;

import com.litentry.litbot.TEEBot.common.PresenceType;
import com.litentry.litbot.TEEBot.config.Constants;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BotUtils {

    public static void sendMsg(TextChannel channel, String message) {
        if (channel != null) {
            if (channel.canTalk()) {
                channel
                    .sendMessage(message)
                    .queue(
                        null,
                        error -> {
                            /* Ignore */
                        }
                    );
            }
        }
    }

    public static void sendMsg(TextChannel channel, String message, int time) {
        if (channel != null) {
            if (channel.canTalk()) {
                channel
                    .sendMessage(message)
                    .queue(
                        m ->
                            m
                                .delete()
                                .queueAfter(
                                    time,
                                    TimeUnit.SECONDS,
                                    null,
                                    error -> {
                                        /* Ignore */
                                    }
                                ),
                        null
                    );
            }
        }
    }

    public static void sendEmbed(TextChannel channel, EmbedBuilder embed) {
        sendEmbed(channel, embed.build());
    }

    public static void sendErrorEmbed(TextChannel channel, String message) {
        EmbedBuilder embed = EmbedUtils.getDefaultEmbed().setDescription(message).setColor(Constants.ERROR_EMBED);
        sendEmbed(channel, embed);
    }

    public static void sendEmbed(TextChannel channel, MessageEmbed embed) {
        if (channel != null) {
            if (channel.canTalk()) {
                Guild guild = channel.getGuild();
                if (guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                    channel
                        .sendMessageEmbeds(embed)
                        .queue(
                            null,
                            error -> {
                                /* Ignore */
                            }
                        );
                } else if (embed.getFields().isEmpty()) {
                    sendMsg(channel, embed.getDescription());
                }
            }
        }
    }

    public static void sendEmbed(TextChannel channel, MessageEmbed embed, int time) {
        if (channel != null) {
            if (channel.canTalk()) {
                Guild guild = channel.getGuild();
                if (guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                    channel
                        .sendMessageEmbeds(embed)
                        .queue(
                            m ->
                                m
                                    .delete()
                                    .queueAfter(
                                        time,
                                        TimeUnit.SECONDS,
                                        null,
                                        error -> {
                                            /* Ignore */
                                        }
                                    ),
                            null
                        );
                } else if (embed.getFields().isEmpty()) {
                    sendMsg(channel, embed.getDescription(), time);
                }
            }
        }
    }

    public static void sendDM(User user, EmbedBuilder embed, Consumer<? super Object> success, Consumer<? super Throwable> error) {
        if (user == null) return;
        user.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(embed.build())).queue(success, error);
    }

    public static void sendDM(User user, String message) {
        if (user == null) return;
        user
            .openPrivateChannel()
            .flatMap(channel -> channel.sendMessage(message))
            .queue(
                null,
                error -> {
                    /* Ignore */
                }
            );
    }

    public static void sendDM(User user, MessageEmbed embed) {
        if (user == null) return;
        user
            .openPrivateChannel()
            .flatMap(channel -> channel.sendMessageEmbeds(embed))
            .queue(
                null,
                error -> {
                    /* Ignore */
                }
            );
    }

    public static void sendSuccess(Message message) {
        MessageUtils.sendSuccess(message);
    }

    public static void sendSuccessWithMessage(Message message, String content) {
        MessageUtils.sendSuccessWithMessage(message, content);
    }

    public static void sendErrorWithMessage(Message message, String content) {
        MessageUtils.sendErrorWithMessage(message, content);
    }

    public static String getEmbedHyperLink(String text, String link) {
        return "[" + text + "](" + link + ")";
    }

    public static void updatePresence(JDA jda, PresenceType presence) {
        if (presence == PresenceType.GUILDS) {
            jda.getPresence().setActivity(Activity.listening("!help | " + jda.getGuildCache().size() + " guilds"));
        }

        if (presence == PresenceType.CHANNELS) {
            jda.getPresence().setActivity(Activity.listening("!help | " + jda.getTextChannelCache().size() + " channels"));
        }

        if (presence == PresenceType.ROLES) {
            jda.getPresence().setActivity(Activity.playing("!help | " + jda.getRoleCache().size() + " roles"));
        }

        if (presence == PresenceType.MEMBERS) {
            long count = 0;
            for (Guild g : jda.getGuilds()) count += g.getMemberCount();
            jda.getPresence().setActivity(Activity.watching("!help | " + count + " members"));
        }
    }
}
