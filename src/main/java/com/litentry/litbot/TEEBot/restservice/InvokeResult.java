package com.litentry.litbot.TEEBot.restservice;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litentry.litbot.TEEBot.config.MsgEnum;

import java.io.Serializable;
import java.text.MessageFormat;
import org.springframework.http.HttpStatus;

public class InvokeResult<T> implements Serializable {

    private T data;
    private String message;
    private boolean hasErrors;
    private Integer msgCode;

    @JsonIgnore
    @JSONField(serialize = false)
    private MsgEnum msgEnum;

    @JsonIgnore
    @JSONField(serialize = false)
    private String[] replace;

    public InvokeResult() {
    }

    public InvokeResult(T dto) {
        this.data = dto;
    }

    public InvokeResult(String message, Integer msgCode) {
        this.message = message;
        this.msgCode = msgCode;
        this.hasErrors = true;
    }

    public void setData(T dto) {
        this.data = dto;
    }

    public InvokeResult<T> success(Integer msgCode) {
        this.hasErrors = false;
        this.msgCode = msgCode;
        return this;
    }

    public InvokeResult<T> failure(MsgEnum status) {
        this.hasErrors = true;
        this.message = status.getMsg();
        this.msgCode = status.getCode();
        msgEnum = status;
        return this;
    }

    public InvokeResult<T> failure(MsgEnum status, String... replace) {
        this.hasErrors = true;
        this.message = status.getMsg();
        this.msgCode = status.getCode();
        msgEnum = status;
        this.replace = replace;
        return this;
    }

    public InvokeResult<T> failure(String message, Integer msgCode) {
        this.hasErrors = true;
        this.message = message;
        this.msgCode = msgCode;
        return this;
    }

    public InvokeResult<T> success(MsgEnum status) {
        this.hasErrors = false;
        this.msgCode = status.getCode();
        this.message = status.getEn();
        msgEnum = status;
        return this;
    }

    public InvokeResult<T> failure(HttpStatus httpStatus) {
        this.hasErrors = true;
        this.msgCode = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
        return this;
    }

    public T getData() {
        return this.data;
    }

    public boolean isHasErrors() {
        return this.hasErrors;
    }

    public boolean isSuccess() {
        return !this.hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Integer getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(Integer msgCode) {
        this.msgCode = msgCode;
    }

    public String getMessage() {
        return getReplaceMsg(message);
    }

    private String getReplaceMsg(String msg) {
        if (replace == null || replace.length == 0) {
            return msg;
        } else {
            return MessageFormat.format(msg, replace);
        }
    }

    @JsonIgnore
    public MsgEnum getMsgEnum() {
        if (hasErrors && msgEnum == null) {
            return MsgEnum.SYSTEM_COMMON_EXCEPTION;
        }
        return msgEnum;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReplace(String... replace) {
        this.replace = replace;
    }

    @Override
    public String toString() {
        return ("InvokeResult{" + "data=" + data + ", message='" + message + '\'' + ", hasErrors=" + hasErrors
                + ", msgCode=" + msgCode + '}');
    }
}
