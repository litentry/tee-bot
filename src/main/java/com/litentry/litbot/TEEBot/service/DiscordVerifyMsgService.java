package com.litentry.litbot.TEEBot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.litentry.litbot.TEEBot.domain.DiscordVerifyMsg;
import com.litentry.litbot.TEEBot.repository.DiscordVerifyMsgRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class DiscordVerifyMsgService {
    private static final Logger log = LoggerFactory.getLogger(DiscordVerifyMsgService.class);

    private DiscordVerifyMsgRepository verifyMsgRepo;
    private static int MAX_MSG_COUNT = 10;

    public DiscordVerifyMsgService(DiscordVerifyMsgRepository verifyMsgRepo) {
        this.verifyMsgRepo = verifyMsgRepo;
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
}
