package com.litentry.litbot.TEEBot.restservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.litentry.litbot.TEEBot.config.MsgEnum;
import com.litentry.litbot.TEEBot.restservice.vm.TwitterUserVM;
import com.litentry.litbot.TEEBot.restservice.vm.TwitterVM;
import com.litentry.litbot.TEEBot.service.TwitterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/twitter")
public class TwitterResource {
    private static final Logger log = LoggerFactory.getLogger(TwitterResource.class);

    private final TwitterService twitterService;

    public TwitterResource(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    @GetMapping("/uid")
    public ResponseEntity<InvokeResult<TwitterUserVM>> FindUidByHandler(String handler) {
        TwitterUserVM user = new TwitterUserVM();

        log.debug("Get handler {}", handler);
        if (handler == null || handler.isEmpty()) {
            return ResponseEntity.ok(new InvokeResult<>(user).failure(MsgEnum.TWITTER_HANDLER_NOTFOUND));
        }

        user = twitterService.FindUidByHandler(handler);
        if (user != null) {
            return ResponseEntity.ok(new InvokeResult<>(user).success(MsgEnum.SYSTEM_COMMON_SUCCESS));
        }

        return ResponseEntity.ok(new InvokeResult<>(user).failure(MsgEnum.TWITTER_HANDLER_NOTFOUND));
    }

    @GetMapping("/tweet")
    public ResponseEntity<InvokeResult<TwitterVM>> FindTweetById(String tid) {
        TwitterVM tvm = new TwitterVM();
        if (tid == null || tid.isEmpty()) {
            return ResponseEntity.ok(new InvokeResult<>(tvm).failure(MsgEnum.SYSTEM_COMMON_FAIL));
        }

        tvm = twitterService.FindTweetById(tid);
        if (tvm != null) {
            log.info("find tweet by id {}, content {}", tid, tvm.toString());
            return ResponseEntity.ok(new InvokeResult<>(tvm).success(MsgEnum.SYSTEM_COMMON_SUCCESS));
        }

        return ResponseEntity.ok(new InvokeResult<>(tvm).failure(MsgEnum.SYSTEM_COMMON_DATA_NOT_FOUND));
    }

    // check whether the {handler2} is following {handler1} or not
    @GetMapping("/followers/verification")
    public ResponseEntity<InvokeResult<Boolean>> FollowersVerification(String handler1, String handler2) {
        if (handler1 == null || handler1.isEmpty() || handler2 == null || handler2.isEmpty()) {
            return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.TWITTER_HANDLER_NOTFOUND));
        }

        TwitterUserVM user1 = twitterService.FindUidByHandler(handler1);
        if (user1 == null) {
            return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.TWITTER_HANDLER_NOTFOUND));
        }
        log.info("got uid1 {} by handler1 {}", user1.getId(), handler1);

        TwitterUserVM user2 = twitterService.FindUidByHandler(handler2);
        if (user2 == null) {
            return ResponseEntity.ok(new InvokeResult<>(false).failure(MsgEnum.TWITTER_HANDLER_NOTFOUND));
        }
        log.info("got uid2 {} by handler2 {}", user2.getId(), handler2);

        Boolean check = twitterService.CheckFollowers(user1.getId(), user2.getId());
        if (check) {
            return ResponseEntity.ok(new InvokeResult<>(check).success(MsgEnum.SYSTEM_COMMON_SUCCESS));
        }
        return ResponseEntity.ok(new InvokeResult<>(check).failure(MsgEnum.SYSTEM_COMMON_DATA_NOT_FOUND));
    }
}
