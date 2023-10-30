package com.litentry.litbot.TEEBot.restservice;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.litentry.litbot.TEEBot.config.BotProperties;
import com.litentry.litbot.TEEBot.config.MsgEnum;
import com.litentry.litbot.TEEBot.domain.DiscordVerifyMsg;
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

    // check whether the user {handler} has joined {guildid} or not
    @GetMapping("/joined")
    public ResponseEntity<InvokeResult<Boolean>> HasJoined(String handler, String guildid) {
        if (guildid == null || guildid.isEmpty()) {
            return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_GUILD_ID_INVALID));
        }
        if (handler == null || handler.isEmpty()) {
            return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_USER_HANDLER_INVALID));
        }

        Boolean check = false;
        try {
            long gid = Long.parseLong(guildid);
            check = verifyMsgService.checkHasJoined(gid, handler);
            if (check) {
                return ResponseEntity.ok(new InvokeResult<>(check).success(MsgEnum.SYSTEM_COMMON_SUCCESS));
            }
        } catch (Exception e) {
            log.error("{}", e);
        }

        return ResponseEntity.ok(new InvokeResult<>(check).failure(MsgEnum.SYSTEM_COMMON_DATA_NOT_FOUND));
    }

    // Assign the 'ID-Hubber' Role to the user {handler} who has joined {guildid}.
    @GetMapping("/assgin/idhubber")
    public ResponseEntity<InvokeResult<Boolean>> AssignRole(String handler, String guildid) {
        if (guildid == null || guildid.isEmpty()) {
            return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_GUILD_ID_INVALID));
        }
        if (handler == null || handler.isEmpty()) {
            return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_USER_HANDLER_INVALID));
        }

        try {
            long gid = Long.parseLong(guildid);
            Long roleId = botProperties.getIdHubberRoleId();

            if (!verifyMsgService.checkHasJoined(gid, handler)) {
                return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_USER_NOTIN_GUILD));
            }

            if (roleId <= 0) {
                log.error("invalid roleid {}", roleId);
                return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.SYSTEM_COMMON_DATA_NOT_FOUND));
            }

            if (verifyMsgService.assginRoleToUser(gid, handler, roleId)) {
                return ResponseEntity.ok(new InvokeResult<>(true).success(MsgEnum.SYSTEM_COMMON_SUCCESS));
            }

        } catch (Exception e) {
            log.error("{}", e);
        }
        return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.SYSTEM_COMMON_DATA_NOT_FOUND));
    }

    @GetMapping("/commented/idhubber")
    public ResponseEntity<InvokeResult<Boolean>> HasCommneted(String handler, String guildid, String channelid,
            String roleid) {
        if (guildid == null || guildid.isEmpty()) {
            return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_GUILD_ID_INVALID));
        }
        if (handler == null || handler.isEmpty()) {
            return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_USER_HANDLER_INVALID));
        }

        try {
            long gid = Long.parseLong(guildid);
            Long channelId = Long.parseLong(channelid);
            Long roleId = Long.parseLong(roleid);

            if (!verifyMsgService.checkHasJoined(gid, handler)) {
                return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_USER_NOTIN_GUILD));
            }

            if (channelId <= 0 || roleId <= 0) {
                log.error("invalid id {} {}", channelId, roleId);
                return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.SYSTEM_COMMON_DATA_NOT_FOUND));
            }

            if (verifyMsgService.hasCommentedInChannelWithRole(gid, handler, channelId, roleId)) {
                return ResponseEntity.ok(new InvokeResult<>(true).success(MsgEnum.SYSTEM_COMMON_SUCCESS));
            }
        } catch (Exception e) {
            log.error("{}", e);
        }

        return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.SYSTEM_COMMON_DATA_NOT_FOUND));
    }

    @GetMapping("/user/has/role")
    public ResponseEntity<InvokeResult<Boolean>> hasRole(final String handler,
                                                         final String guildid,
                                                         final String roleid) {
        try {
            if (StringUtils.isEmpty(guildid)) {
                return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_GUILD_ID_INVALID));
            }
            if (StringUtils.isEmpty(handler)) {
                return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_USER_HANDLER_INVALID));
            }
            if (StringUtils.isEmpty(roleid)) {
                return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_ROLE_ID_INVALID));
            }

            final long gid = Long.parseLong(guildid);
            final long roleId = Long.parseLong(roleid);

            if (roleId <= 0) {
                log.error("Invalid roleId id {}", roleId);
                return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.DISCORD_ROLE_ID_INVALID));
            }

            if (verifyMsgService.hasRole(gid, handler, roleId)) {
                return ResponseEntity.ok(new InvokeResult<>(true).success(MsgEnum.SYSTEM_COMMON_SUCCESS));
            }
        } catch (Exception e) {
            log.error("Fail to call API: discord/user/has/role", e);
        }

        return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.SYSTEM_COMMON_DATA_NOT_FOUND));
    }
}
