package com.litentry.litbot.TEEBot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.litentry.litbot.TEEBot.config.Constants;
import com.litentry.litbot.TEEBot.domain.DiscordVerifyMsg;
import com.litentry.litbot.TEEBot.repository.DiscordVerifyMsgRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class DiscordVerifyMsgService {
    private static final Logger log = LoggerFactory.getLogger(DiscordVerifyMsgService.class);

    private JDA jda = null;
    private DiscordVerifyMsgRepository verifyMsgRepo;
    private static int MAX_MSG_COUNT = 10;

    public DiscordVerifyMsgService(DiscordVerifyMsgRepository verifyMsgRepo) {
        this.verifyMsgRepo = verifyMsgRepo;
    }

    public void setJDA(JDA jda) {
        if (jda != null) {
            this.jda = jda;
        }
    }

    public void addMsg(Long guildId, Long userId, Long channelId, Long msgId, String userName, String msg,
            String jump) {
        List<DiscordVerifyMsg> msgList = verifyMsgRepo.findAllByGuildIdAndDiscordUserOrderByCreatedAtDesc(guildId,
                userName);
        int count = msgList.size();
        while (count >= MAX_MSG_COUNT) {
            DiscordVerifyMsg tMsg = msgList.get(msgList.size() - 1);
            verifyMsgRepo.delete(tMsg);
            count--;
        }

        DiscordVerifyMsg vMsg = new DiscordVerifyMsg();
        vMsg.setGuildId(guildId);
        vMsg.setUserId(userId);
        vMsg.setChannelId(channelId);
        vMsg.setMsgId(msgId);
        vMsg.setDiscordUser(userName);
        vMsg.setMsg(msg);
        vMsg.setJump(jump);
        verifyMsgRepo.save(vMsg);
    }

    public DiscordVerifyMsg getLatest(Long guildId, String userName) {
        List<DiscordVerifyMsg> msgList = verifyMsgRepo.findAllByGuildIdAndDiscordUserOrderByCreatedAtDesc(guildId,
                userName);
        log.info("msgList.size {} {} {}", guildId, userName, msgList.size());

        if (!msgList.isEmpty()) {
            return msgList.get(0);
        }
        return null;
    }

    public Boolean checkHasJoined(@NotNull Long guildId, @NotNull Long userId) {
        boolean[] founds = new boolean[1];
        founds[0] = false;
        try {
            List<Guild> guilds = jda.getGuilds();
            for (Guild guild : guilds) {
                if (guild.getIdLong() == guildId) {
                    jda.retrieveUserById(userId)
                            .queue(user -> {
                                guild.retrieveMember(user).queue(
                                        member -> {
                                            log.info("uid {} has joined guild {}", userId, guildId);
                                            founds[0] = true;
                                        },
                                        error -> {
                                        });
                            });
                }
            }
        } catch (Exception e) {
            log.error("{}", e);
        }

        return founds[0];
    }

    // user `handler` means `Name#Discriminator`
    public Boolean checkHasJoined(@NotNull Long guildId, @NotNull String handler) {
        if (handler.contains(Constants.DISCRIMINATOR)) {
            // username, discriminator
            String[] tmpArray = handler.split(Constants.DISCRIMINATOR);
            try {
                List<Guild> guilds = jda.getGuilds();
                for (Guild guild : guilds) {
                    if (guild.getIdLong() == guildId) {
                        Member m = guild.getMemberByTag(handler);
                        if (m != null) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("{}", e);
            }
        }

        return false;
    }
}
