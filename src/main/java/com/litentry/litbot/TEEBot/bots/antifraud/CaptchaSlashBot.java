package com.litentry.litbot.TEEBot.bots.antifraud;

import com.litentry.litbot.TEEBot.config.BotProperties;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

public class CaptchaSlashBot {

    public static String[] verifyEmotes = {
        "\uD83D\uDE38",
        "\uD83E\uDD16",
        "\uD83D\uDC27",
        "\uD83D\uDE80",
        "☀",
        "\uD83D\uDD04",
        "\uD83C\uDFB5",
        "⭐",
        "\uD83D\uDD25",
        "\uD83C\uDF89",
    };
    private BotProperties botProperties;

    public CaptchaSlashBot(BotProperties botProperties) {
        this.botProperties = botProperties;
    }

    public void handleSlash(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        User user = event.getUser();
        //        CaptchaDTO captcha = ossService.queryCaptcha(guild.getIdLong(), user.getIdLong());
        //
        //        if (captcha == null) {
        //            String desc = "Oops! There is something wrong when I generate new captcha.";
        //            EmbedBuilder embed = EmbedUtils.getDefaultEmbed().setAuthor("Sorry").setDescription(desc);
        //            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        //            log.error("queryCaptcha fail {} {}", guild.getId(), user.getId());
        //            return;
        //        }

        //        if (captcha.isVerified()) {
        //            final EmbedBuilder embed = EmbedUtils
        //                .getDefaultEmbed()
        //                .setAuthor("Wow")
        //                .setDescription("Thank you! You have been verified in " + guild.getName() + " guild!\n");
        //            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        //            return;
        //        }

        final EmbedBuilder embed = getVerifyMsg();
        event
            .replyEmbeds(embed.build())
            .addActionRow(
                Button.primary("1", Emoji.fromUnicode(verifyEmotes[0])),
                Button.secondary("2", Emoji.fromUnicode(verifyEmotes[1])),
                Button.success("3", Emoji.fromUnicode(verifyEmotes[2])),
                Button.danger("4", Emoji.fromUnicode(verifyEmotes[3])),
                Button.primary("5", Emoji.fromUnicode(verifyEmotes[4]))
            )
            .setEphemeral(true)
            .queue();
        return;
    }

    public void handleButton(ButtonInteractionEvent event) {
        final EmbedBuilder embed = EmbedUtils.getDefaultEmbed();
        String buttonId = event.getComponentId();
        if (buttonId.equals("4")) {
            embed.setAuthor("Wow").setDescription("Thank you! You have been verified in " + event.getGuild().getName() + " guild!\n");
        } else {
            embed.setAuthor("Sorry").setDescription("Attempt Failed, please try again.");
        }
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private EmbedBuilder getVerifyMsg() {
        String desc = "To gain access, please click the 4th emoji to get the verified role.\n";

        EmbedBuilder embed = EmbedUtils.getDefaultEmbed().setDescription(desc);
        return embed;
    }
}
