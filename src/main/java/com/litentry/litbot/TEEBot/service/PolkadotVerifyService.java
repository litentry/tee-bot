package com.litentry.litbot.TEEBot.service;

import com.alibaba.fastjson.JSONObject;
import com.litentry.litbot.TEEBot.config.BotProperties;
import com.litentry.litbot.TEEBot.polkadot.SS58.SS58Type;
import com.litentry.litbot.TEEBot.polkadot.common.Address;
import com.litentry.litbot.TEEBot.utils.AES256;
import com.litentry.litbot.TEEBot.utils.BotUtils;
import com.litentry.litbot.TEEBot.utils.HttpUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PolkadotVerifyService {
    private static final Logger log = LoggerFactory.getLogger(PolkadotVerifyService.class);

    private final BotProperties botProperties;

    private JDA jda = null;
    private static String secretKey = "polkadot!!!!";
    private static String salt = "verify!!!!";
    private static byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    private final String verifySignatureApi = "http://127.0.0.1:8091/verify-polka-sig";

    public PolkadotVerifyService(BotProperties botProperties) {
        this.botProperties = botProperties;
    }

    public void setJDA(JDA jda) {
        if (jda != null) {
            this.jda = jda;
        }
    }

    //    public String getValidatedAddress(Long guildId, Long discordUid) {
    //        Optional<DiscordVerify> opVerify = discordVerifyRepository.findFirstByGuildIdAndDiscordUserId(guildId, discordUid);
    //        if (opVerify.isPresent()) {
    //            return opVerify.get().getPolkadotAddress();
    //        }
    //        return null;
    //    }

    //    public boolean isCrowdloaner(Long guildId, Long discordUid) {
    //        Optional<DiscordVerify> opVerify = discordVerifyRepository.findFirstByGuildIdAndDiscordUserId(guildId, discordUid);
    //        if (opVerify.isPresent()) {
    //            return opVerify.get().getIsCrowdloaner();
    //        }
    //        return false;
    //    }

    public String getDiscordVerifyMsg(Long guildId, Long discordUserId) {
        if (botProperties.getShouldPolkadotWallet()) {
            //Long discordUserId = ctx.getAuthor().getIdLong();
            //            Optional<DiscordUser> opDiscordUser = discordUserRepository.findById(discordUserId);
            //            DiscordUser discordUser = new DiscordUser();
            //            if (!opDiscordUser.isPresent()) {
            //                //add new user
            //                discordUser.setId(discordUserId);
            //                discordUser.setDiscordName(ctx.getAuthor().getAsTag());
            //                discordUser.setRegistrationAt(ctx.getAuthor().getTimeCreated().toInstant());
            //                discordUserRepository.save(discordUser);
            //            }

            //            Optional<DiscordVerify> opVerify = discordVerifyRepository.findFirstByGuildIdAndDiscordUserId(guildId, discordUserId);
            //            if (opVerify.isPresent() && opVerify.get().getVerificationCode() != null) {
            //                return opVerify.get().getVerificationCode();
            //            }

            String plain = "type=discord&uid=" + discordUserId + "&ts=" + Instant.now().getEpochSecond() + "&guild=" + guildId;
            String verifyCode = AES256.encrypt(secretKey, salt, iv, plain);
            //            DiscordVerify verify = new DiscordVerify();
            //            verify.setGuildId(guildId);
            //            verify.setDiscordUserId(discordUserId);
            //            verify.setVerificationCode(verifyCode);
            //            verify.setChainId(Constants.BLOCKCHAIN_POLKADOT);
            //            discordVerifyRepository.save(verify);
            return verifyCode;
        }
        return null;
    }

    //address format: SUBSTRATE
    public boolean verifySignature(String message, String signature, String address) {
        //1. check if the message is original
        String messageDecoded = AES256.decrypt(secretKey, salt, iv, message);
        if (messageDecoded == null) {
            return false;
        }

        String[] splitStr = messageDecoded.split("&");
        if (splitStr.length == 4) {
            if (!splitStr[0].equalsIgnoreCase("type=discord")) {
                return false;
            }
            if (!splitStr[1].startsWith("uid=")) {
                return false;
            }
            if (!splitStr[2].startsWith("ts=")) {
                return false;
            }
            if (!splitStr[3].startsWith("guild=")) {
                return false;
            }
        }

        //2. check user id is valid
        String[] splitUidStr = splitStr[1].split("=");
        if (splitUidStr.length != 2) {
            log.warn("Parsed Discord Uid not valid", splitStr[1]);
            return false;
        }
        Long discordUid = Long.parseLong(splitUidStr[1]);
        String[] splitGuildStr = splitStr[3].split("=");
        if (splitGuildStr.length != 2) {
            log.warn("Parsed Discord Guild Id not valid", splitStr[1]);
            return false;
        }
        Long guildId = Long.parseLong(splitGuildStr[1]);

        //3. check if the signature is valid
        try {
            JSONObject data = new JSONObject();
            data.put("message", message);
            data.put("signature", signature);
            data.put("address", address);

            //make suer the address is SS58 Substrate
            if (!isSubstrateAddr(address)) {
                log.error("Not valid substrate address {}", address);
                return false;
            }

            JSONObject result = HttpUtils.requestPostJsonObject(verifySignatureApi, data.toString());
            if (result != null) {
                boolean isValid = result.getBoolean("isValid");
                log.info("verifySignature: {} {} {} {}", message, signature, address, isValid);

                if (isValid) {
                    //make suer the <guild_id + polkadot_address> is uniq
                    //                    List<DiscordVerify> addressList = discordVerifyRepository.findAllByGuildIdAndPolkadotAddress(guildId, address);
                    //                    if (!addressList.isEmpty()) {
                    //                        sendDuplicatedAddressDM(discordUid, address);
                    //                        log.info("Got duplicated address {} {} {}", guildId, discordUid, address);
                    //                        return false;
                    //                    }

                    //                    Optional<DiscordVerify> opVerify = discordVerifyRepository.findFirstByGuildIdAndDiscordUserId(guildId, discordUid);
                    //                    if (!opVerify.isPresent()) {
                    //                        log.error("Not found in DiscordVerify {} {}", guildId, discordUid);
                    //                        return false;
                    //                    }

                    //                    DiscordVerify verify = opVerify.get();
                    //                    verify.setVerificationSucc(true);
                    //                    verify.setVerificationAt(Instant.now());
                    //                    verify.setPolkadotAddress(address);
                    //                    discordVerifyRepository.save(verify);

                    //Optional<DiscordGuildSettings> opSettings = guildSettingsRepository.findByGuildId(guildId);
                    //if (opSettings.isPresent()) {
                    //4. send invites reward
                    //handleInvitesReward(guildId, discordUid, opSettings.get());

                    //5. access to validated role and channel
                    Long roleId = botProperties.getPolkaVerifiedRoleId();
                    Long channelId = botProperties.getPolkaVerifiedChanId();
                    if (roleId.longValue() > 0L && channelId.longValue() > 0L) {
                        //log.info("start addRoleAndChannel {} {} {} {}", guildId, discordUid, roleId, channelId);
                        addRoleAndChannel(guildId, discordUid, roleId, channelId);
                    }
                    //}

                    //6. send wallet connect reward
                    //handleWalletConnectReward(guildId, discordUid, opSettings.get());

                    //7. send confirm DM to user
                    sendSuccValidatedDM(discordUid, address);
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            log.error("verify signature: {} {} {} {}", message, signature, address, e);
        }
        return false;
    }

    //    public boolean isCaptchaVerified(Long guildId, Long discordUid) {
    //        Optional<CaptchaLog> captchaOP = captchaLogRepository.findByGuildIdAndDiscordUserId(guildId, discordUid);
    //        if (captchaOP.isPresent() && captchaOP.get().isVerified()) {
    //            return true;
    //        }
    //        return false;
    //    }

    public boolean isSubstrateAddr(String address) {
        Address substrateAddr = Address.from(address);
        if (substrateAddr.getNetwork() == SS58Type.Network.SUBSTRATE) {
            return true;
        }
        return false;
    }

    //address format: SUBSTRATE
    //    public boolean queryCrowdloanContributor(String address, Long guildId, Long discordUid, DiscordGuildSettings settings)
    //        throws Exception {
    //        log.info("queryCrowdloanContributor substrate address {} {} {}", address, guildId, discordUid);
    //
    //        Address substrateAddr = Address.from(address);
    //        if (substrateAddr.getNetwork() != SS58Type.Network.SUBSTRATE) {
    //            log.error("Invalid SUBSTRATE address: {}", address);
    //            return false;
    //        }
    //
    //        Optional<DiscordVerify> opVerify = discordVerifyRepository.findFirstByGuildIdAndDiscordUserId(guildId, discordUid);
    //        if (opVerify.isPresent() && opVerify.get().getIsCrowdloaner()) {
    //            return true;
    //        }
    //
    //        Address polkaAddr = new Address(SS58Type.Network.POLKADOT, substrateAddr.getPubkey());
    //        String addressPolkadot = polkaAddr.toString();
    //
    //        JSONObject data = new JSONObject();
    //        data.put("account", polkaAddr.toString());
    //        JSONObject result = HttpUtils.requestPostJsonObject(crowdloanContributorApi, data.toString());
    //        log.info("got query result {} data {} API {}", result.toJSONString(), data.toJSONString(), crowdloanContributorApi);
    //
    //        if (result != null) {
    //            int overallContributionAmount = result.getInteger("overallContributionAmount");
    //            log.info("query crowdloanContributor: {} {}", addressPolkadot, overallContributionAmount);
    //            if (overallContributionAmount > 0) {
    //                if (opVerify.isPresent()) {
    //                    DiscordVerify verify = opVerify.get();
    //                    verify.setCrowdloanVerifyAt(Instant.now());
    //                    verify.setIsCrowdloaner(true);
    //                    discordVerifyRepository.save(verify);
    //
    //                    //handleCrowdloanReward(guildId, discordUid, settings);
    //                    return true;
    //                } else {
    //                    log.error("query crowdloanContributor should have address first {} {}", guildId, discordUid);
    //                }
    //            }
    //        }
    //        return false;
    //    }

    private void addRoleAndChannel(Long guildId, Long discordUid, Long roleId, Long channelId) {
        try {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                //log.error("guild is null! {}", guildId);
                return;
            }

            Role role = guild.getRoleById(roleId);
            if (role == null) {
                //log.error("role is null! {} {}", guildId, roleId);
                return;
            }

            //log.info("adding Polkadot Wallet Role to user {} {} {} {}", guild.getId(), role.getId(), discordUid, channelId);
            guild
                .getJDA()
                .retrieveUserById(discordUid)
                .queue(user -> {
                    guild
                        .addRoleToMember(user, role)
                        .queue(__ -> {
                            EmbedBuilder embed = EmbedUtils
                                .getDefaultEmbed()
                                .setDescription("**Guild**: " + guild.getName() + "\n**Role**: " + role.getName())
                                .setAuthor("Polkadot Wallet Verified Role Added to " + user.getName())
                                .setColor(role.getColor());

                            BotUtils.sendDM(user, embed.build());
                        });
                });
        } catch (Exception e) {
            //log.error("Error addRoleToMember {} {} {} {} {}", guildId, discordUid, roleId, channelId, e);
        }
    }

    private void sendSuccValidatedDM(Long discordUid, String address) {
        this.jda.retrieveUserById(discordUid)
            .queue(user -> {
                String desc = "Well done! Your Polkadot address: **" + address + "** is validated!\n";
                final EmbedBuilder embed = EmbedUtils
                    .getDefaultEmbed()
                    .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                    .setDescription(desc);

                BotUtils.sendDM(user, embed.build());
            });
    }
    //    private void sendDuplicatedAddressDM(Long discordUid, String address) {
    //        String desc = "Oops! There is a same Polkadot address " + address + " validated before. \nPlease provide a different one!\n";
    //        final EmbedBuilder embed = EmbedUtils.getDefaultEmbed().setAuthor("Info").setDescription(desc);
    //        this.jda.retrieveUserById(discordUid).queue(user -> BotUtils.sendDM(user, embed.build()));
    //    }
    //
    //    private void handleWalletConnectReward(Long guildID, Long inviteeID, DiscordGuildSettings settings) {
    //        long rewardAmount = settings.getRewardPolkadotWalletLit();
    //        if (rewardAmount > 0) {
    //            // reward invitee
    //            walletService.deposit(guildID, inviteeID, Constants.REWARDS_SYMBOL_LIT, rewardAmount);
    //            log.info("handle Wallet Connect Reward {} {} {}", guildID, inviteeID, rewardAmount);
    //        }
    //    }
    //
    //    public void disconnectWallet(Long guildId, Long userId) {
    //        discordVerifyRepository.deleteByGidAndDiscordUid(guildId, userId);
    //    }
}
