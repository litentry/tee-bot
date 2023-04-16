package com.litentry.litbot.TEEBot.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.litentry.litbot.TEEBot.config.TwitterProperties;
import com.litentry.litbot.TEEBot.restservice.vm.TwitterUserVM;
import com.litentry.litbot.TEEBot.restservice.vm.TwitterVM;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.Get2TweetsIdResponse;
import com.twitter.clientlib.model.Get2UsersByUsernameUsernameResponse;
import com.twitter.clientlib.model.Get2UsersIdFollowersResponse;
import com.twitter.clientlib.model.ResourceUnauthorizedProblem;
import com.twitter.clientlib.model.User;

import twitter4j.Twitter;
import twitter4j.TwitterException;

@Service
public class TwitterService {
    private static final Logger log = LoggerFactory.getLogger(TwitterService.class);

    private final TwitterProperties twitterProperties;
    private final TwitterApi apiInstance;
    private static int RETRIES = 5;

    private final Twitter twitter4j;

    public TwitterService(TwitterProperties twitterProperties) {
        this.twitterProperties = twitterProperties;
        apiInstance = new TwitterApi(new TwitterCredentialsBearer(this.twitterProperties.getBearToken()));
        twitter4j = Twitter.newBuilder()
                .oAuthConsumer(this.twitterProperties.getConsumerKey(), this.twitterProperties.getConsumerSecret())
                .oAuthAccessToken(this.twitterProperties.getAccessToken(),
                        this.twitterProperties.getAccessTokenSecret())
                .build();
    }

    public TwitterVM FindTweetById(@NotNull String tid) {
        Set<String> tweetFields = new HashSet<>();
        tweetFields.add("author_id");
        tweetFields.add("id");
        tweetFields.add("created_at");
        tweetFields.add("text");

        try {
            Get2TweetsIdResponse result = apiInstance.tweets().findTweetById(tid)
                    .tweetFields(tweetFields)
                    .execute(RETRIES);
            if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                result.getErrors().forEach(e -> {
                    log.error("{}", e);
                    if (e instanceof ResourceUnauthorizedProblem) {
                        log.error("{} {}", ((ResourceUnauthorizedProblem) e).getTitle(),
                                ((ResourceUnauthorizedProblem) e).getDetail());
                    }
                });
            } else {
                TwitterVM tvm = new TwitterVM();
                tvm.setId(result.getData().getId());
                tvm.setAuthorId(result.getData().getAuthorId());
                tvm.setCreatedAt(result.getData().getCreatedAt());
                tvm.setText(result.getData().getText());
                return tvm;
            }
        } catch (ApiException e) {
            log.error("Status code: {}", e.getCode());
            log.error("Reason: {}", e.getResponseBody());
            log.error("Response headers: {}", e.getResponseHeaders());
            log.error("{}", e);
        }
        return null;
    }

    public TwitterUserVM FindUidByHandler(@NotNull String handler) {
        Set<String> userFields = new HashSet<>(Arrays.asList());
        try {
            Get2UsersByUsernameUsernameResponse result = apiInstance.users().findUserByUsername(handler)
                    .userFields(userFields)
                    .execute(RETRIES);
            if (result.getData() != null) {
                TwitterUserVM user = new TwitterUserVM();
                user.setId(result.getData().getId());
                user.setName(result.getData().getName());
                user.setHandler(result.getData().getUsername());
                return user;
            }
        } catch (ApiException e) {
            log.error("Exception when calling UsersApi#findUserByUsername. Status code: {}, Reason:{}", e.getCode(),
                    e.getResponseBody());
            log.error("Response headers: {}", e.getResponseHeaders());
            log.error("{}", e);
        }
        return null;
    }

    public boolean CheckFriendship(String uid, String beCheckedUid) {
        log.info("CheckFriendship {} {}", uid, beCheckedUid);
        try {
            twitter4j.v1().tweets().updateStatus("Hello Twitter API!");
            twitter4j.v1().friendsFollowers().showFriendship(uid, beCheckedUid);

        } catch (TwitterException e) {
            log.error("got TwitterException {}", e);
        }

        return false;
    }

    // Check whether the {beCheckedUid} is following the {uid} or not
    // Due to the twitter api limitations, only the first 1000 followers can be
    // fetched.
    // Will improve this to multiple "pages" of results in the future version.
    public boolean CheckFollowers(String uid, String beCheckedUid) {
        log.info("CheckFollowers {} {}", uid, beCheckedUid);

        Set<String> userFields = new HashSet<>(Arrays.asList());
        try {
            Get2UsersIdFollowersResponse result = apiInstance.users().usersIdFollowers(uid)
                    .maxResults(1000)
                    .userFields(userFields)
                    .execute(RETRIES);

            if (result.getData() != null) {
                List<User> users = result.getData();

                log.info("follower size {}", users.size());

                // List<TwitterUserVM> userList = new ArrayList<>();
                for (User user : users) {
                    // TwitterUserVM tuser = new TwitterUserVM();
                    // tuser.setId(user.getId());
                    // tuser.setName(user.getName());
                    // tuser.setHandler(user.getUsername());
                    // userList.add(tuser);

                    log.info("follower: uid {} handler {}", user.getId(), user.getUsername());
                    if (user.getId().equalsIgnoreCase(beCheckedUid)) {
                        return true;
                    }
                }
            }
            // log.info("{}", result);
        } catch (ApiException e) {
            log.error("Exception when calling UsersApi#usersIdFollowers. Status code{}", e.getCode());
            log.error("Reason: {}", e.getResponseBody());
            log.error("Response headers: {}", e.getResponseHeaders());
            log.error("{}", e);
        }

        return false;
    }
}
