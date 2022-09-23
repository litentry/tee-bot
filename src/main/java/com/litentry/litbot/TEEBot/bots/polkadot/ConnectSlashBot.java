package com.litentry.litbot.TEEBot.bots.polkadot;

import com.litentry.litbot.TEEBot.config.BotProperties;
import com.litentry.litbot.TEEBot.service.PolkadotVerifyService;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ConnectSlashBot {
    private BotProperties botProperties;
    private PolkadotVerifyService polkadotVerifyService;

    public ConnectSlashBot(BotProperties botProperties, PolkadotVerifyService polkadotVerifyService) {
        this.botProperties = botProperties;
        this.polkadotVerifyService = polkadotVerifyService;
    }

    public void handleSlash(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        User user = event.getUser();

        String msg = polkadotVerifyService.getDiscordVerifyMsg(guild.getIdLong(), user.getIdLong());
        //        if (msg == null) {
        //            EmbedBuilder embed = EmbedUtils
        //                .getDefaultEmbed()
        //                .setAuthor("Sorry")
        //                .setDescription("Oops! There is something wrong when I generate ID-Hub verify link.");
        //
        //            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        //            log.error("getDiscordVerifyMsg fail {} {}", guild.getId(), user.getId());
        //            return;
        //        }

        try {
            msg = URLEncoder.encode(msg, StandardCharsets.UTF_8.toString());
            String url = "https://www.drop3.id/tasks/polkadot/wallet-connect?msg=" + msg;
            //String url = "https://mycryptoprofile.io/register/select";
            event
                .reply("To link your ID-Hub account, please click: \n\n")
                .addActionRow(Button.link(url, "Link"))
                .setEphemeral(true)
                .queue();
        } catch (Exception e) {
            EmbedBuilder embed = EmbedUtils
                .getDefaultEmbed()
                .setAuthor("Sorry")
                .setDescription("Oops! There is something wrong when I generate ID-Hub verify message.");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
        return;
    }
}
