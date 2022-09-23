package com.litentry.litbot.TEEBot.handlers;

import com.litentry.litbot.TEEBot.bots.Bot;
import com.litentry.litbot.TEEBot.bots.antifraud.CaptchaSlashBot;
import com.litentry.litbot.TEEBot.bots.infomation.BotInfoCommand;
import com.litentry.litbot.TEEBot.bots.infomation.HelpCommand;
import com.litentry.litbot.TEEBot.bots.polkadot.ConnectSlashBot;
import com.litentry.litbot.TEEBot.bots.polkadot.VerifySlashBot;
import com.litentry.litbot.TEEBot.command.CommandContext;
import com.litentry.litbot.TEEBot.command.ICommand;
import com.litentry.litbot.TEEBot.config.BotProperties;
import com.litentry.litbot.TEEBot.service.PolkadotVerifyService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CommandHandler extends ListenerAdapter {
    private final ArrayList<ICommand> commands = new ArrayList<>();
    private final HashMap<String, Integer> commandIndex = new HashMap<>();
    private final HashMap<String, OffsetDateTime> cooldowns = new HashMap<>();
    private final HashMap<String, Integer> uses = new HashMap<>();

    private BotProperties botProperties;
    private PolkadotVerifyService polkadotVerifyService;
    private CaptchaSlashBot captchaSlashBot;
    private ConnectSlashBot connectSlashBot;
    private VerifySlashBot verifySlashBot;

    public CommandHandler(Bot bot, BotProperties botProperties, PolkadotVerifyService polkadotVerifyService) {
        this.botProperties = botProperties;
        this.polkadotVerifyService = polkadotVerifyService;

        captchaSlashBot = new CaptchaSlashBot(this.botProperties);
        connectSlashBot = new ConnectSlashBot(this.botProperties, this.polkadotVerifyService);
        verifySlashBot = new VerifySlashBot(botProperties);

        // INFORMATION COMMANDS
        addCommand(new BotInfoCommand(this.botProperties));
        addCommand(new HelpCommand(bot.getWaiter(), this.botProperties));
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        String raw = event.getMessage().getContentRaw();
        if (raw.startsWith(botProperties.getPrefix())) {
            this.handle(event, botProperties.getPrefix());
        }
    }

    private void addCommand(ICommand cmd) {
        int index = this.commands.size();

        if (this.commandIndex.containsKey(cmd.getName())) {
            throw new IllegalArgumentException(String.format("Command name \"%s\" is already in use", cmd.getName()));
        }

        for (String alias : cmd.getAliases()) {
            if (this.commandIndex.containsKey(alias)) {
                throw new IllegalArgumentException(String.format("Alias: %s in Command: \"%s\" is already used!", alias, cmd.getName()));
            }
            this.commandIndex.put(alias, index);
        }

        this.commandIndex.put(cmd.getName(), index);
        this.commands.add(index, cmd);
    }

    public ArrayList<ICommand> getCommands() {
        return commands;
    }

    @Nullable
    public ICommand getCommand(String search) {
        int i = this.commandIndex.getOrDefault(search.toLowerCase(), -1);
        return i != -1 ? this.commands.get(i) : null;
    }

    private void handle(MessageReceivedEvent event, String prefix) {
        String[] split = event.getMessage().getContentRaw().replaceFirst("(?i)" + Pattern.quote(prefix), "").split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            List<String> args = Arrays.asList(split).subList(1, split.length);
            CommandContext ctx = new CommandContext(event, args, invoke, prefix, this);
            uses.put(cmd.getName(), uses.getOrDefault(cmd.getName(), 0) + 1);
            cmd.run(ctx);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("start")) {
            captchaSlashBot.handleSlash(event);
        } else if (event.getName().equals("connect")) {
            connectSlashBot.handleSlash(event);
        } else if (event.getName().equals("verify")) {
            verifySlashBot.handleSlash(event);
        } else {
            //log.error("can not handle SlashCommandInteractionEvent: " + event.getName());
        }
        //        if (event.getName().equals("modmail")) {
        //            TextInput subject = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
        //                .setPlaceholder("Subject of this ticket")
        //                .setMinLength(10)
        //                .setMaxLength(100) // or setRequiredRange(10, 100)
        //                .build();
        //
        //            TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
        //                .setPlaceholder("Your concerns go here")
        //                .setMinLength(30)
        //                .setMaxLength(1000)
        //                .build();
        //
        //            Modal modal = Modal.create("modmail", "Modmail")
        //                .addActionRows(ActionRow.of(subject), ActionRow.of(body))
        //                .build();
        //
        //            event.replyModal(modal).queue();
        //        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (
            event.getComponentId().equals("1") ||
            event.getComponentId().equals("2") ||
            event.getComponentId().equals("3") ||
            event.getComponentId().equals("4") ||
            event.getComponentId().equals("5")
        ) {
            captchaSlashBot.handleButton(event);
        }
    }

    public int getRemainingCooldown(String name) {
        if (cooldowns.containsKey(name)) {
            int time = (int) Math.ceil(OffsetDateTime.now().until(cooldowns.get(name), ChronoUnit.MILLIS) / 1000D);
            if (time <= 0) {
                cooldowns.remove(name);
                return 0;
            }
            return time;
        }
        return 0;
    }

    public void applyCooldown(String name, int seconds) {
        cooldowns.put(name, OffsetDateTime.now().plusSeconds(seconds));
    }

    public void cleanCooldowns() {
        OffsetDateTime now = OffsetDateTime.now();
        final int size = cooldowns.size();
        cooldowns
            .keySet()
            .stream()
            .filter(str -> (cooldowns.get(str).isBefore(now)))
            .collect(Collectors.toList())
            .forEach(cooldowns::remove);
    }
}
