package com.litentry.litbot.TEEBot.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.litentry.litbot.TEEBot.exception.BotException;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    public static void requestGetJsonObject(String url, Callback callback) throws IOException, BotException {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder().url(url).addHeader("Accept", "application/json").build();

        httpClient.newCall(request).enqueue(callback);
    }

    public static JSONObject requestGetJsonObject(String url) {
        OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        Request request = new Request.Builder().url(url).addHeader("Accept", "application/json").build();
        Response response;
        try {
            response = httpClient.newCall(request).execute();
            if (response.body() == null) {
                throw new BotException("get request content is null");
            }
            return JSON.parseObject(response.body().string());
        } catch (IOException e) {
            //log.error("Error HTTP get:", e);
        }
        return null;
    }

    public static JSONObject requestPostJsonObject(String url, String data) {
        OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder().url(url).post(requestBody).build();

        try {
            Response response = httpClient.newCall(request).execute();
            ResponseBody body = response.body();
            if (body == null) {
                throw new BotException("post request content is null");
            }
            return JSON.parseObject(response.body().string());
        } catch (Exception e) {
            //log.error("Error HTTP post {}", e);
        }
        return null;
    }

    public static void main(String[] args) {
        String url = "http://localhost:8091/verify-polka-sig";
        JSONObject data = new JSONObject();
        data.put("message", "This is a text message");
        data.put(
            "signature",
            "0x2aeaa98e26062cf65161c68c5cb7aa31ca050cb5bdd07abc80a475d2a2eebc7b7a9c9546fbdff971b29419ddd9982bf4148c81a49df550154e1674a6b58bac84"
        );
        data.put("address", "5FHneW46xGXgs5mUiveU4sbTyGBzmstUspZC92UhjJM694ty");

        JSONObject result = HttpUtils.requestPostJsonObject(url, data.toString());
        System.out.println("result " + result + ", " + result.getBoolean("isValid"));
    }
}
