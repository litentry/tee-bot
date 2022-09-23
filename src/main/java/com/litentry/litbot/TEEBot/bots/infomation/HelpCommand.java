package com.litentry.litbot.TEEBot.bots.infomation;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.litentry.litbot.TEEBot.command.CommandCategory;
import com.litentry.litbot.TEEBot.command.CommandContext;
import com.litentry.litbot.TEEBot.command.ICommand;
import com.litentry.litbot.TEEBot.config.BotProperties;
import com.litentry.litbot.TEEBot.config.Constants;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HelpCommand extends ICommand {

    private final EventWaiter waiter;
    private static BotProperties botProperties;

    public HelpCommand(EventWaiter waiter, BotProperties botProperties) {
        this.name = "help";
        this.help = "Shows the list with commands in the bot.";
        this.usage = "<command>";
        this.aliases = Arrays.asList("commands", "cmds", "commandlist");
        this.category = CommandCategory.INFORMATION;
        this.botPermissions = new Permission[] { Permission.MESSAGE_EMBED_LINKS };
        this.waiter = waiter;
        this.botProperties = botProperties;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        if (ctx.getAuthor().isBot()) {
            return;
        }

        if (ctx.getArgs().isEmpty()) {
            if (
                ctx
                    .getSelfMember()
                    .hasPermission(ctx.getChannel(), Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)
            ) {
                this.sendReactionHelpMenu(ctx);
            } else {
                this.sendCategoryHelpMenu(ctx);
                //log.warn("Not enough Permissions in HelpCommand");
            }
            return;
        }

        final String invoke = ctx.getArgs().get(0);
        CommandCategory category = CommandCategory.fromSearch(invoke);

        if (invoke.equalsIgnoreCase("info")) {
            category = CommandCategory.INFORMATION;
        }

        if (category != null) {
            ctx.reply(this.getCategoryHelpEmbed(ctx, category).build());
            return;
        }

        final ICommand cmd = ctx.getCmdHandler().getCommand(invoke);

        if (cmd == null) {
            ctx.reply(
                "Oops! Did you provide a valid command category or command?\n" +
                "Type **" +
                ctx.getPrefix() +
                "help** to see all available commands."
            );
            return;
        }
        cmd.sendUsageEmbed(ctx.getChannel(), ctx.getPrefix(), invoke, "Help");
    }

    public static MessageEmbed getReactionHelpMenu(CommandContext ctx) {
        EmbedBuilder embed = EmbedUtils
            .getDefaultEmbed()
            .setAuthor("Help Menu")
            .setDescription(
                "Hello I am " +
                ctx.getSelfMember().getUser().getName() +
                "! A cool discord bot which can serve your crypto-currency related needs.\n\n"
            );

        for (CommandCategory value : CommandCategory.values()) {
            if (value == CommandCategory.UNLISTED) {
                continue;
            }
            embed.addField(
                value.getName(),
                "React with " + value.getEmote() + " to view `" + value.getName().toLowerCase() + "` commands",
                true
            );
        }

        embed.addField(
            "\n_",
            "[**Support Server**](" +
            botProperties.getDiscordInvite() +
            //") **|** [**Bot Invite Link**](" +
            //botProperties.getBotInvite() +
            ")",
            false
        );
        return embed.build();
    }

    private void sendReactionHelpMenu(CommandContext ctx) {
        ctx
            .getChannel()
            .sendMessageEmbeds(getReactionHelpMenu(ctx))
            .queue(sentMessage -> {
                for (CommandCategory value : CommandCategory.values()) {
                    if (value.getEmote() == null) {
                        continue;
                    }
                    sentMessage.addReaction(value.getEmote()).queue();
                }
                waitForReactions(ctx, sentMessage);
            });
    }

    public static EmbedBuilder getCategoryHelpEmbed(CommandContext ctx, CommandCategory category) {
        String collector = ctx
            .getCmdHandler()
            .getCommands()
            .stream()
            .filter(cmd -> cmd.getCategory() == category)
            .map(cmd -> Constants.ARROW + " `" + ctx.getPrefix() + cmd.getName() + "` - " + cmd.getHelp())
            .collect(Collectors.joining("\n"));

        return EmbedUtils
            .getDefaultEmbed()
            .setAuthor(category.getName() + " Commands")
            .setThumbnail(category.getIconUrl())
            .setDescription(collector);
    }

    public static List<ICommand> getCategoryHelpCmds(CommandContext ctx, CommandCategory category) {
        List<ICommand> cmds = new ArrayList<>();
        for (ICommand cmd : ctx.getCmdHandler().getCommands()) {
            if (cmd.getCategory() == category) {
                if (cmd.getEmote() != null && !cmd.getEmote().isEmpty()) {
                    cmds.add(cmd);
                }
            }
        }
        return cmds;
    }

    private void waitForReactions(CommandContext ctx, Message sentMessage) {
        final CommandCategory[] values = CommandCategory.values();

        this.wait(
                ctx,
                sentMessage,
                e -> {
                    final String emoji = e.getReaction().getReactionEmote().getEmoji();
                    //final String emoji = e.getReaction().getReactionEmote().getEmoji();
                    for (CommandCategory value : values) {
                        if (value.getEmote() != null && value.getEmote().equals(emoji)) {
                            List<ICommand> cmds = this.getCategoryHelpCmds(ctx, value);
                            sentMessage
                                .editMessageEmbeds(this.getCategoryHelpEmbed(ctx, value).build())
                                .queue(msg -> {
                                    sentMessage
                                        .clearReactions()
                                        .queue(__ -> {
                                            for (ICommand cmd : cmds) {
                                                sentMessage.addReaction(cmd.getEmote()).queue();
                                            }
                                            sentMessage
                                                .addReaction(Constants.EMOTE_RETURN_UP_LEVEL)
                                                .queue(s -> waitForSubmenuReactions(ctx, sentMessage, cmds) //wait for next level reactions
                                                );
                                        });
                                });
                            break;
                        }
                    }
                }
            );
    }

    private void waitForSubmenuReactions(CommandContext ctx, Message sentMessage, List<ICommand> cmds) {
        this.wait(
                ctx,
                sentMessage,
                e -> {
                    final String emoji = e.getReaction().getReactionEmote().getEmoji();
                    if (emoji.equals(Constants.EMOTE_RETURN_UP_LEVEL)) {
                        MessageEmbed messageEmbed = getReactionHelpMenu(ctx);
                        sentMessage
                            .editMessageEmbeds(messageEmbed)
                            .queue(msg -> {
                                sentMessage.clearReactions().queue();
                                for (CommandCategory value : CommandCategory.values()) {
                                    if (value.getEmote() == null) {
                                        continue;
                                    }
                                    msg.addReaction(value.getEmote()).queue();
                                }
                                waitForReactions(ctx, msg);
                            });
                    } else {
                        //msg -> msg.removeReaction(emoji, e.getUser()).queue(__ -> this.waitForReactions(ctx, sentMessage))
                        for (ICommand cmd : cmds) {
                            if (emoji.equals(cmd.getEmote())) {
                                MessageEmbed messageEmbed = cmd.getUsageEmbed(ctx.getPrefix(), cmd.getName(), "Help");
                                sentMessage.clearReactions().queue();
                                sentMessage
                                    .addReaction(Constants.EMOTE_RETURN_UP_LEVEL)
                                    .queue(__ ->
                                        sentMessage
                                            .editMessageEmbeds(messageEmbed)
                                            .queue(s -> this.waitForSubmenuReactions(ctx, sentMessage, cmds))
                                    );
                            }
                        }
                    }
                }
            );
    }

    public static void sendCategoryHelpMenu(CommandContext ctx) {
        final User selfUser = ctx.getSelfMember().getUser();

        if (ctx.getArgs().isEmpty()) {
            String str = "**About Me:**\n";
            str += "Hello I am " + selfUser.getName() + "!\n\n";
            str += "A cool discord bot which can serve your crypto-currency related needs\n";
            str += "**Quick Links:**" + "\n";
            str += "Support Server: [Join here](" + botProperties.getDiscordInvite() + ")" + "\n";
            str += "**Command modules:**" + "\n";
            str += Constants.ARROW + " polkadot" + "\n\n";
            str += Constants.ARROW + " information" + "\n";

            EmbedBuilder embed = EmbedUtils
                .getDefaultEmbed()
                .setDescription(str)
                .setThumbnail(selfUser.getEffectiveAvatarUrl())
                .setColor(Constants.TRANSPARENT_EMBED)
                .setFooter(
                    "Type **" + ctx.getPrefix() + "help <module>** to see all related command",
                    ctx.getAuthor().getEffectiveAvatarUrl()
                );

            ctx.reply(embed.build());
        }
    }

    private void wait(CommandContext ctx, Message sentMsg, Consumer<MessageReactionAddEvent> action) {
        waiter.waitForEvent(
            MessageReactionAddEvent.class,
            e -> e.getUser().getId().equals(ctx.getAuthor().getId()) && e.getChannel().equals(ctx.getChannel()),
            action,
            Constants.REACTION_TIMEOUT_SECONDS,
            TimeUnit.SECONDS,
            new Timeout(sentMsg)
        );
    }

    private static class Timeout implements Runnable {

        private final Message msg;
        private boolean ran = false;

        private Timeout(Message msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            if (ran) return;
            ran = true;
            msg.clearReactions().queue();
        }
    }
}
