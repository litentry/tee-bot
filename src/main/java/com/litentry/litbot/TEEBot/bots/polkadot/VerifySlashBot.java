package com.litentry.litbot.TEEBot.bots.polkadot;

import com.litentry.litbot.TEEBot.config.BotProperties;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class VerifySlashBot {
    private BotProperties botProperties;

    public VerifySlashBot(BotProperties botProperties) {
        this.botProperties = botProperties;
    }

    public void handleSlash(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        User user = event.getUser();

        try {
            event.reply("I'm querying your predetermined roles from ID-Hub. Please wait for a minute.").setEphemeral(true).queue();
        } catch (Exception e) {
            EmbedBuilder embed = EmbedUtils
                .getDefaultEmbed()
                .setAuthor("Sorry")
                .setDescription("Oops! There is something wrong when I start ID-Hub verification.");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }
}
