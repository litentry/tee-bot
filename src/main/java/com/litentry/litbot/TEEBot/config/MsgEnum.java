package com.litentry.litbot.TEEBot.config;

// import javax.servlet.http.HttpServletRequest;
// import org.springframework.web.context.request.RequestContextHolder;
// import org.springframework.web.context.request.ServletRequestAttributes;

public enum MsgEnum {
    // polkadot:10
    POLKADOT_VERIFY_SIG_SUCCESS(100001, "Polkadot verify signature successful", "Polkadot签名验证成功"),
    POLKADOT_VERIFY_SIG_FAIL(100002, "Polkadot verify signature fail", "Polkadot签名验证失败"),

    // discord: 101
    DISCORD_VERIFY_MSG_FOUND(100101, "Discord verify message found", ""),
    DISCORD_VERIFY_MSG_NOTFOUND(100102, "Discord verify message not found", ""),

    // system common:99
    SYSTEM_COMMON_SUCCESS(100000, "Success", "操作成功"),
    SYSTEM_COMMON_FAIL(99000, "Fail", "操作失败"),
    SYSTEM_COMMON_BAD_REQUEST(99103, "Request parameter incorrect", "参数不正确"),
    SYSTEM_COMMON_DATA_NOT_FOUND(99104, "Not Found", "未找到"),
    SYSTEM_COMMON_UNAUTHORIZED(99105, "Unauthorized", "权限不够"),
    SYSTEM_COMMON_MAN_VERIFY_FAIL(99111, "Validation Failure", "验证失败"),
    SYSTEM_COMMON_EXCEPTION(99201, "System abnormal", "系统异常"),
    SYSTEM_COMMON_ERROR(99202, "System error", "系统错误"),
    SYSTEM_COMMON_REQUEST_FREQUENCY_LIMITED(99206, "Request frequency limited", "请求频繁，请稍后重试");

    private int code;
    private String en;
    private String cn;

    MsgEnum(int code, String en, String cn) {
        this.code = code;
        this.en = en;
        this.cn = cn;
    }

    public int getCode() {
        return code;
    }

    public String getEn() {
        return getMsg();
    }

    public String getCn() {
        return getMsg();
    }

    public String getMsg() {
        // if (((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
        // != null) {
        // HttpServletRequest request = ((ServletRequestAttributes)
        // RequestContextHolder.getRequestAttributes())
        // .getRequest();
        // if (request != null) {
        // String acceptLanguage = request.getHeader("accept-language");
        // return acceptLanguage != null &&
        // acceptLanguage.toLowerCase().startsWith("zh") ? cn : en; // cn
        // }
        // }
        return en; // return en by default
    }

    @Override
    public String toString() {
        return en;
    }
}