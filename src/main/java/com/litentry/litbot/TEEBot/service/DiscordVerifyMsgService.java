package com.litentry.litbot.TEEBot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.litentry.litbot.TEEBot.config.Constants;
import com.litentry.litbot.TEEBot.domain.DiscordVerifyMsg;
import com.litentry.litbot.TEEBot.repository.DiscordVerifyMsgRepository;

import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

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
    private static int MAX_MSG_COUNT = 50;

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
            // String[] tmpArray = handler.split(Constants.DISCRIMINATOR);
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

    // user `handler` means `Name#Discriminator`
    public Boolean assginRoleToUser(@NotNull Long guildId, @NotNull String handler, @NotNull Long roleId) {
        if (checkHasJoined(guildId, handler) && roleId > 0) {
            try {
                List<Guild> guilds = jda.getGuilds();
                for (Guild guild : guilds) {
                    if (guild.getIdLong() == guildId) {
                        Member m = guild.getMemberByTag(handler);
                        boolean assigned = assignRole(guild, m.getUser(), roleId);
                        if (!assigned) {
                            log.error("Error assign Role {} {} {}", guildId, m.getId(), roleId);
                            return false;
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                log.error("{}", e);
            }
        }

        return false;
    }

    public Boolean hasCommentedInChannelWithRole(@NotNull Long guildId, @NotNull String handler,
            @NotNull Long channelId, @NotNull Long roleId) {
        try {
            List<Guild> guilds = jda.getGuilds();
            for (Guild guild : guilds) {
                if (guild.getIdLong() == guildId) {
                    Member m = guild.getMemberByTag(handler);
                    if (hasRole(guild, m, roleId)) {
                        List<DiscordVerifyMsg> msgList = verifyMsgRepo
                                .findAllByGuildIdAndDiscordUserAndChannelIdOrderByCreatedAtDesc(
                                        guildId, handler, channelId);

                        if (!msgList.isEmpty()) {
                            DiscordVerifyMsg msg = msgList.get(0);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("{}", e);
        }

        return false;
    }

    public Boolean hasRole(@NotNull final Long guildId, @NotNull final String handler, @NotNull final Long roleId) {
        try {
            final List<Guild> guilds = jda.getGuilds();
            for (final Guild guild : guilds) {
                if (guild.getIdLong() == guildId) {
                    final Member m = guild.getMemberByTag(convertHandler2Tag(handler));
                    return hasRole(guild, m, roleId);
                }
            }
        } catch (Exception e) {
            log.error("Fail to call hasRole, guildId={}, handler={}, roleId={}", guildId, handler, roleId, e);
        }

        return false;
    }

    private boolean assignRole(Guild guild, User user, long roleId) {
        if (guild == null || user == null) {
            return false;
        }

        Role role = guild.getRoleById(roleId);
        if (role != null) {
            log.info("adding ID-Hubber Role {} {} {}", guild.getId(), user.getId(), roleId);
            guild
                    .addRoleToMember(user, role)
                    .queue(__ -> {
                        EmbedBuilder embed = EmbedUtils
                                .getDefaultEmbed()
                                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                                .setDescription("**Guild**: " + guild.getName() + "\n**Role**: " + role.getName())
                                .setAuthor("Role Added to " + user.getName())
                                .setColor(role.getColor());

                        // BotUtils.sendDM(user, embed.build());
                    });
            return true;
        }
        return false;
    }

    public Boolean hasRole(Guild guild, Member member, long roleId) {
        if (guild == null || member == null) {
            return false;
        }

        List<Role> roles = member.getRoles();
        for (Role r : roles) {
            if (r.getIdLong() == roleId) {
                return true;
            }
        }

        return false;
    }

    private String convertHandler2Tag(String handler) {
        return handler.contains(Constants.DISCRIMINATOR) ? handler
                : String.format("%s%s%s", handler, Constants.DISCRIMINATOR, "0000");
    }
}
