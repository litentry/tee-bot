package com.litentry.litbot.TEEBot.handlers;

import com.litentry.litbot.TEEBot.bots.Bot;
import com.litentry.litbot.TEEBot.config.Constants;
import com.litentry.litbot.TEEBot.utils.BotUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class ReactionHandler extends ListenerAdapter {
    @Autowired
    private final Bot bot;

    public ReactionHandler(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {}

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;

        if (event.getReactionEmote().isEmoji()) {
            //bot.getReactionHandler().handleFlagReaction(event);

            // Handle Tickets in async
            //bot.getThreadpool().execute(() -> bot.getReactionHandler().handleTicket(event));
        }
        //bot.getReactionHandler().handleReactionRole(event, true);
    }

    @Override
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
        if (event.getUser() != null && event.getUser().isBot()) return;
        //bot.getReactionHandler().handleReactionRole(event, false);

    }

    //    public void handleReactionRole(@NotNull GenericGuildMessageReactionEvent event, boolean isAdded) {
    //        final MessageReaction.ReactionEmote reactionEmote = event.getReaction().getReactionEmote();
    //        final Guild guild = event.getGuild();
    //        final TextChannel channel = event.getChannel();

    //        String emoji;
    //        if (reactionEmote.isEmote())
    //            emoji = reactionEmote.getEmote().getName() + ":" + reactionEmote.getEmote().getId();
    //        else
    //            emoji = reactionEmote.getEmoji();

    //        String roleId = DataSource.INS.getReactionRoleId(guild.getId(), channel.getId(), event.getMessageId(), emoji);
    //
    //        if (roleId == null)
    //            return;
    //
    //        final Role role = guild.getRoleById(roleId);
    //
    //        if (role == null) {
    //            // TODO: If role is removed, remove data from DB
    //            return;
    //        }
    //
    //        try {
    //            if (isAdded)
    //                guild.addRoleToMember(event.getUserId(), role).queue(
    //                    (__) -> sendRoleInfo(event.getUser(), guild, role, true),
    //                    e -> LOGGER.error("ReactionRole - Reaction Add failed : " + e.getMessage()));
    //            else
    //                guild.removeRoleFromMember(event.getUserId(), role).queue(
    //                    (__) -> sendRoleInfo(event.getUser(), guild, role, false),
    //                    e -> LOGGER.error("ReactionRole - Reaction Remove failed : " + e.getMessage()));
    //
    //        } catch (PermissionException ex) { /* Ignore */ } catch (Exception e) {
    //            LOGGER.error("ReactionRole failed : " + e.getMessage());
    //        }

    //    }

    private void sendRoleInfo(User user, Guild guild, Role role, boolean isAdded) {
        String SERVER_LINK = "https://discord.com/channels/";
        EmbedBuilder embed = EmbedUtils
            .getDefaultEmbed()
            .setDescription(
                "**Guild Name**: " +
                BotUtils.getEmbedHyperLink(guild.getName(), SERVER_LINK + guild.getId()) +
                "\n**Role Name**: " +
                role.getName()
            );

        if (isAdded) {
            embed.setAuthor("Role Added").setColor(role.getColor());
        } else {
            embed.setAuthor("Role Removed").setColor(Constants.ERROR_EMBED);
        }

        BotUtils.sendDM(user, embed.build());
    }
}
