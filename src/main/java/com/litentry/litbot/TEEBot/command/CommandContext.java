package com.litentry.litbot.TEEBot.command;

import com.litentry.litbot.TEEBot.handlers.CommandHandler;
import com.litentry.litbot.TEEBot.utils.BotUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandContext {

    private final MessageReceivedEvent event;
    private final CommandHandler cmdHandler;
    private final String prefix;
    private final String invoke;
    private final List<String> args;

    public CommandContext(MessageReceivedEvent event, List<String> args, String invoke, String prefix, CommandHandler cmdHandler) {
        this.event = event;
        this.cmdHandler = cmdHandler;
        this.prefix = prefix;
        this.invoke = invoke;
        this.args = args;
    }

    public MessageReceivedEvent getEvent() {
        return this.event;
    }

    public CommandHandler getCmdHandler() {
        return this.cmdHandler;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getInvoke() {
        return this.invoke;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public Guild getGuild() {
        return this.getEvent().getGuild();
    }

    public String getGuildId() {
        return this.getEvent().getGuild().getId();
    }

    public TextChannel getChannel() {
        return this.getEvent().getTextChannel();
    }

    public Message getMessage() {
        return this.getEvent().getMessage();
    }

    public Member getMember() {
        return this.getEvent().getMember();
    }

    public Member getSelfMember() {
        return this.getGuild().getSelfMember();
    }

    public JDA getJDA() {
        return this.getEvent().getJDA();
    }

    public User getAuthor() {
        return this.getEvent().getAuthor();
    }

    public String getArgsJoined() {
        return String.join(" ", this.getArgs());
    }

    public void reply(String message) {
        BotUtils.sendMsg(this.getChannel(), message);
    }

    public void reply(MessageEmbed embed) {
        BotUtils.sendEmbed(this.getChannel(), embed);
    }

    public void replyWithSuccess(String message) {
        BotUtils.sendSuccessWithMessage(this.getMessage(), message);
    }

    public void replyWithError(String message) {
        BotUtils.sendErrorWithMessage(this.getMessage(), message);
    }

    public void replyError(String message) {
        BotUtils.sendErrorEmbed(this.getChannel(), message);
    }
}
