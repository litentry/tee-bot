package com.litentry.litbot.TEEBot.restservice;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.litentry.litbot.TEEBot.config.BotProperties;
import com.litentry.litbot.TEEBot.config.MsgEnum;
import com.litentry.litbot.TEEBot.domain.DiscordVerifyMsg;
import com.litentry.litbot.TEEBot.handlers.PrivateMsgHandler;
import com.litentry.litbot.TEEBot.restservice.vm.DiscordVerify;
import com.litentry.litbot.TEEBot.restservice.vm.DiscordVerifyVM;
import com.litentry.litbot.TEEBot.service.DiscordVerifyMsgService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/discord")
public class VerifyMsgResource {

    private final BotProperties botProperties;
    private DiscordVerifyMsgService verifyMsgService;

    private static final Logger log = LoggerFactory.getLogger(VerifyMsgResource.class);

    public VerifyMsgResource(DiscordVerifyMsgService verifyMsgService, BotProperties botProperties) {
        this.verifyMsgService = verifyMsgService;
        this.botProperties = botProperties;
    }

    @PostMapping("/verify")
    public ResponseEntity<InvokeResult<DiscordVerifyVM>> verifySig(
            @Valid @RequestBody DiscordVerify verifyMsg) {

        DiscordVerifyVM result = new DiscordVerifyVM();
        result.setGuildId(botProperties.getMainGuildId());
        result.setHandler(verifyMsg.getHandler());

        log.info("got handler {} {}", verifyMsg.getHandler(), botProperties.getMainGuildId());

        if (!verifyMsg.getHandler().isEmpty() && verifyMsg.getHandler().length() < 64) {
            DiscordVerifyMsg msg = verifyMsgService.getLatest(botProperties.getMainGuildId(), verifyMsg.getHandler());
            if (msg != null) {
                String url = "https://discordapp.com/api/channels/" + msg.getChannelId() + "/messages/"
                        + msg.getMsgId();

                result.setMsg(msg.getMsg());
                result.setMsgCreatedAt(msg.getCreatedAt());
                result.setUrl(url);
                result.setChannleId(msg.getChannelId());
                result.setMsgId(msg.getMsgId());
                result.setUserId(msg.getUserId());
                result.setAuthorization("Bot " + botProperties.getToken());
                return ResponseEntity.ok(new InvokeResult<>(result).success(MsgEnum.DISCORD_VERIFY_MSG_FOUND));
            }
        }

        return ResponseEntity.ok(new InvokeResult<>(result).failure(MsgEnum.DISCORD_VERIFY_MSG_NOTFOUND));
    }
}
