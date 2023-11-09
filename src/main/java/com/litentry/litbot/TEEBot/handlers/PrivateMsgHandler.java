package com.litentry.litbot.TEEBot.handlers;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.litentry.litbot.TEEBot.config.BotProperties;
import com.litentry.litbot.TEEBot.config.Constants;
import com.litentry.litbot.TEEBot.service.DiscordVerifyMsgService;
import com.litentry.litbot.TEEBot.service.PolkadotVerifyService;

import com.litentry.litbot.TEEBot.utils.BotUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Service
public class PrivateMsgHandler extends ListenerAdapter {

    private BotProperties botProperties;
    private EventWaiter waiter;
    private PolkadotVerifyService polkadotVerifyService;
    private DiscordVerifyMsgService discordVerifyMsgService;

    private static final Logger log = LoggerFactory.getLogger(PrivateMsgHandler.class);
    public static int MAX_MSG_SIZE = 255;

    public PrivateMsgHandler(BotProperties botProperties, PolkadotVerifyService polkadotVerifyService,
                             DiscordVerifyMsgService discordVerifyMsgService) {
        this.botProperties = botProperties;
        this.polkadotVerifyService = polkadotVerifyService;
        this.discordVerifyMsgService = discordVerifyMsgService;
    }

    public void setWaiter(EventWaiter waiter) {
        this.waiter = waiter;
    }

    private TextChannel getTextChannel(MessageReceivedEvent event) {
        return event.getTextChannel();
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.isBot()) {
            return;
        }

        try {
            User user = event.getAuthor();

            if (event.isFromType(ChannelType.TEXT) && !user.isBot() && !user.isSystem()) {
                GuildMessageChannel channel = event.getGuildChannel();
                Long guildId = event.getGuild().getIdLong();
                String content = raw(event).trim().toLowerCase();
                String userName = user.getName() + "#" + user.getDiscriminator();
                boolean isIdHubberChannel =
                        Objects.equals(String.valueOf(botProperties.getIdHubberChannelId()), channel.getId());
                boolean isVerifyIdentityChannel =
                        Objects.equals(String.valueOf(botProperties.getVerifyIdentityChannelId()), channel.getId());
                if (isIdHubberChannel || isVerifyIdentityChannel) {
                    log.info("got text channel message: userId[{}] userName[{}] discriminator[{}] content[{}] jumpUrl[{}] guildId[{}] channelId[{}]",
                            user.getId(), user.getName(), user.getDiscriminator(), content, event.getJumpUrl(),
                            guildId, channel.getId());
                }

                if (isVerifyIdentityChannel) {
                    if (isProofMsg(content)) {
                        Message message = event.getMessage();
                        discordVerifyMsgService.addMsg(guildId, user.getIdLong(), channel.getIdLong(),
                                message.getIdLong(), userName, content, event.getJumpUrl());
                        if (event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND)) {
                            String desc = "Thank you! this is a bot message. Here is your discord verification link:\n\n" +
                                    String.format("<https://discord.com/channels/%s/%s/%s>\n\n",
                                            botProperties.getMainGuildId(), botProperties.getVerifyIdentityChannelId(), message.getId()) +
                                    "Next, please copy above link and continue the process on the Identity Hub and paste the link. " +
                                    "Afterward, click the \"Verify\" button. Your data's security and privacy are safeguarded, " +
                                    "stored within the secure TEE. This protection remains intact regardless of who views the challenge code. " +
                                    "If you require help, don't hesitate to ask for assistance.";
                            EmbedBuilder embed = EmbedUtils
                                    .getDefaultEmbed()
                                    .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                                    .setFooter(botProperties.getFooter())
                                    .setDescription(desc);

                            message.replyEmbeds(embed.build()).queue();
                        }
                    }
                } else if (isIdHubberChannel) {
                    // save message id-hubber channel message to db for that API /discord/commented/idhubber will use it
                    discordVerifyMsgService.addMsg(guildId, user.getIdLong(), channel.getIdLong(),
                            event.getMessage().getIdLong(), userName, content, event.getJumpUrl());
                }
            }
        } catch (Exception e) {
            log.error("parse message got error guildId[{}] authorId[{}]",
                    event.getGuild().getIdLong(), author.getId(), e);
        }

        // log.info("got Private Message, content:{}, author:{}", content,
        // author.getId());

        // if (isStartCommand(content)) {
        // CaptchaDTO captcha = ossService.queryCaptcha(drop3Guild, author.getIdLong());
        // if (captcha == null) {
        // BotUtils.sendDM(author, getReplyErrMsg().build());
        // return;
        // }
        // if (captcha.isVerified()) {
        // final EmbedBuilder embed = EmbedUtils
        // .getDefaultEmbed()
        // .setAuthor("Wow")
        // .setDescription("Thank you! You have been verified in this guild!\n");
        // BotUtils.sendDM(author, embed.build());
        // return;
        // }
        //
        // final EmbedBuilder embed = getVerifyMsg(captcha);
        // author
        // .openPrivateChannel()
        // .flatMap(channel -> channel.sendMessageEmbeds(embed.build()))
        // .queue(// message -> {
        // // message.addReaction(CaptchaBot.resendEmote).queue();
        // // waitForReaction(drop3Guild, author, message);
        // // },
        // e -> log.error("send DM got error {} {}", e, author.getId())
        // );
        // return;
        // }

        // if (isResendCommand(content)) {
        // //request a new Captcha
        // ossService.cleanCaptchaLog(drop3Guild, author.getIdLong());
        // CaptchaDTO captcha = ossService.queryCaptcha(drop3Guild, author.getIdLong());
        // if (captcha == null) {
        // BotUtils.sendDM(author, getReplyErrMsg().build());
        // return;
        // }
        // BotUtils.sendDM(author, getVerifyMsg(captcha).build());
        // return;
        // }

        // if (isCaptchaAnswer(content)) {
        // log.info("check answer {} from {}", content, author.getId());
        //
        // //check answer
        // Optional<CaptchaLog> captchaLogOp =
        // captchaLogRepository.findByGuildIdAndDiscordUserId(drop3Guild,
        // author.getIdLong());
        // if (captchaLogOp.isPresent()) {
        // CaptchaLog captcha = captchaLogOp.get();
        // long retryTimes = captcha.getRetryTimes() + 1L;
        //
        // if (retryTimes > Constants.MAX_RETRY_TIMES) {
        // final EmbedBuilder embed = EmbedUtils
        // .getDefaultEmbed()
        // .setAuthor("Sorry")
        // .setDescription(
        // "You have 0 attempt remaining to try again. Please restart Captcha
        // Verification by relpy **!start**.\nIf you are not a bot and there has been a
        // mistake, send a message to us in the support channel!\n"
        // );
        // BotUtils.sendDM(author, embed.build());
        //
        // captchaLogRepository.delete(captcha);
        // return;
        // }
        //
        // Guild guild = event.getJDA().getGuildById(captcha.getGuildId());
        // if (content.equalsIgnoreCase(captcha.getCaptchaAnswer())) {
        // captcha.setRetryTimes(retryTimes);
        // captcha.setVerified(true);
        // captcha.setVerifiedAt(Instant.now());
        //
        // final EmbedBuilder embed = EmbedUtils
        // .getDefaultEmbed()
        // .setAuthor("Thank you!")
        // .setDescription("You have been verified in Drop3 guild!");
        // BotUtils.sendDM(author, embed.build());
        //
        // //assign Verified Role to User
        // boolean assigned = assignRole(guild, author);
        // if (!assigned) {
        // log.error("Error assign Verified Role {}", author.getId());
        // }
        // } else {
        // captcha.setRetryTimes(retryTimes);
        //
        // String desc =
        // "Attempt Failed, please try again. You have " + (Constants.MAX_RETRY_TIMES -
        // retryTimes) + " attempts remaining.";
        // if (Constants.MAX_RETRY_TIMES == retryTimes) {
        // desc = "Attempt Failed. You can reply **!start** or **!resend** to
        // restart.\n";
        // }
        // final EmbedBuilder embed =
        // EmbedUtils.getDefaultEmbed().setAuthor("Sorry").setDescription(desc);
        // BotUtils.sendDM(author, embed.build());
        // }
        // captchaLogRepository.save(captcha);
        // } else {
        // final EmbedBuilder embed = EmbedUtils
        // .getDefaultEmbed()
        // .setAuthor("Sorry")
        // .setDescription("Can not find your pending Captcha Verifications.\nYou can
        // enter **!start** command to start.\n");
        // BotUtils.sendDM(author, embed.build());
        // }
        // return;
        // }

        // if (isPWDCommand(content)) {
        // String msg = polkadotVerifyService.getDiscordVerifyMsg(drop3Guild,
        // author.getIdLong());
        // if (msg == null) {
        // EmbedBuilder embed = EmbedUtils
        // .getDefaultEmbed()
        // .setAuthor("Sorry")
        // .setDescription("Oops! There is something wrong when I generate Polkadot
        // verify message.");
        // BotUtils.sendDM(author, embed.build());
        // log.error("getDiscordVerifyMsg fail {} {}", drop3Guild, author.getId());
        // return;
        // }
        //
        // try {
        // msg = URLEncoder.encode(msg, StandardCharsets.UTF_8.toString());
        // String url = "https://www.drop3.id/tasks/polkadot/wallet-connect?msg=" + msg;
        // String desc = "To verify your Polkadot address: [Click Here](" + url +
        // ")\n\n";
        //
        // final EmbedBuilder embed =
        // EmbedUtils.getDefaultEmbed().setDescription(desc).setAuthor("Connect Polkadot
        // Wallet");
        // BotUtils.sendDM(author, embed.build());
        // } catch (Exception e) {
        // EmbedBuilder embed = EmbedUtils
        // .getDefaultEmbed()
        // .setAuthor("Sorry")
        // .setDescription("Oops! There is something wrong when I generate Polkadot
        // verify message.");
        // BotUtils.sendDM(author, embed.build());
        // log.error("Send verify message {} {} {}", drop3Guild, author.getId(), e);
        // }
        // return;
        // }

        final EmbedBuilder embed = EmbedUtils
                .getDefaultEmbed()
                .setAuthor("Sorry")
                .setFooter(botProperties.getFooter())
                .setDescription(
                        "I don't understand. Please try again.\n\n" +
                                "**NOTE**: \n1. If you want to start Captcha Verification, reply **!start** to me." +
                                "\n2. If you want to connect your Polkadot Wallet,  reply **!connect** to me.\nThank you!\n\n");
        // BotUtils.sendDM(author, embed.build());
    }

    // private EmbedBuilder getVerifyMsg(CaptchaDTO captcha) {
    // String desc = "Please send the captcha below in this DM.\n\n";
    // desc +=
    // "**NOTE**: \n1. The captcha is is Not Case Sensitive and does Not Include
    // Spaces." +
    // " \n2. If it's hard to recognize, you can reply `!resend` command to request
    // a new one.\n\n";
    //
    // EmbedBuilder embed =
    // EmbedUtils.embedImage(captcha.getOssURL()).setAuthor("Verify yourself to gain
    // access").setDescription(desc);
    // return embed;
    // }

    private EmbedBuilder getReplyErrMsg() {
        String desc = "Oops! There is something wrong when I generate new captcha.";
        EmbedBuilder embed = EmbedUtils.getDefaultEmbed().setAuthor("Sorry").setDescription(desc);
        return embed;
    }

    // private void waitForReaction(long guildId, User author, Message sentMessage)
    // {
    // this.wait(
    // author,
    // sentMessage,
    // e -> {
    // final String emoji = e.getReaction().getReactionEmote().getEmoji();
    // if (emoji.equals(CaptchaBot.resendEmote)) {
    // sentMessage.removeReaction(CaptchaBot.resendEmote).queue();
    // sentMessage
    // .editMessage(getResendEmbed().build())
    // .queue(msg -> {
    // final long userId = author.getIdLong();
    // ossService.cleanCaptchaLog(guildId, userId);
    // CaptchaDTO captcha = ossService.queryCaptcha(guildId, userId);
    // if (captcha == null) {
    // BotUtils.sendDM(author, getReplyErrMsg().build());
    // return;
    // }
    // sentMessage.removeReaction(CaptchaBot.resendEmote).queue();
    // EmbedBuilder embedMsg = getVerifyMsg(captcha);
    // sentMessage.editMessage(embedMsg.build()).queue();
    // });
    // }
    // }
    // );
    // }

    // private boolean assignRole(Guild guild, User user) {
    // if (guild == null || user == null) {
    // return false;
    // }
    //
    // Optional<DiscordGuildSettings> settingsOP =
    // this.settingsRepository.findByGuildId(guild.getIdLong());
    // if (settingsOP.isPresent()) {
    // Long roleId = settingsOP.get().getCaptchaRoleId();
    // Role role = guild.getRoleById(roleId);
    // if (role != null) {
    // log.info("adding Captcha Verified Role {} {} {}", guild.getId(),
    // user.getId(), roleId);
    // guild
    // .addRoleToMember(user, role)
    // .queue(__ -> {
    // EmbedBuilder embed = EmbedUtils
    // .getDefaultEmbed()
    // .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
    // .setDescription("**Guild**: " + guild.getName() + "\n**Role**: " +
    // role.getName())
    // .setAuthor("Captcha Verified Role Added to " + user.getName())
    // .setColor(role.getColor());
    //
    // BotUtils.sendDM(user, embed.build());
    // });
    // return true;
    // }
    // }
    // return false;
    // }

    // private void wait(User author, Message sentMsg,
    // Consumer<MessageReactionAddEvent> action) {
    // waiter.waitForEvent(
    // MessageReactionAddEvent.class,
    // e -> e.getUser().getId().equals(author.getId()),
    // action,
    // Constants.REACTION_TIMEOUT_SECONDS * 4,
    // TimeUnit.SECONDS,
    // new Timeout(sentMsg)
    // );
    // }

    private EmbedBuilder getResendEmbed() {
        return EmbedUtils
                .getDefaultEmbed()
                .setAuthor("Resend Captcha")
                .setDescription("Hey! Your request is received. A new Captcha is on the way!\n");
    }

    private static class Timeout implements Runnable {
        private final Message msg;
        private boolean ran = false;

        private Timeout(Message msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            if (ran)
                return;
            ran = true;
        }
    }

    private boolean isStartCommand(String content) {
        return content != null && content.trim().equalsIgnoreCase("!start");
    }

    private boolean isProofMsg(String content) {
        if (content != null && content.length() < MAX_MSG_SIZE && content.startsWith("0x")) {
            return true;
        }
        return false;
    }

    private boolean isResendCommand(String content) {
        return content != null && content.trim().equalsIgnoreCase("!resend");
    }

    private boolean isCaptchaAnswer(String content) {
        return content.length() == Constants.CAPTCHA_LENGTH && content.matches("[0-9a-z]+");
    }

    private boolean isPWDCommand(String content) {
        return content != null && content.trim().equalsIgnoreCase("!connect");
    }

    private String raw(MessageReceivedEvent event) {
        return event.getMessage().getContentRaw();
    }
}
