package ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 封装数据
 * @author hjd
 */

public class BotConfig {
    private int id;

    private int type;

    private String appChannelName;

    private String appChannelId;

    private String centerControlBotAccount;

    private String centerControlBotTitle;

    private String recommendBotAccount;

    private String recommendBotTitle;

    private String cancelFeather;

    private int includeExitPlatform;

    private int auth;

    private String ccHint;

    private String labelBlacklist;

    private String gmtCreate;

    private String gmtModified;

    private List<String> cancelFeathers;

    private String notFoundBot;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAppChannelName() {
        return appChannelName;
    }

    public void setAppChannelName(String appChannelName) {
        this.appChannelName = appChannelName;
    }

    public String getAppChannelId() {
        return appChannelId;
    }

    public void setAppChannelId(String appChannelId) {
        this.appChannelId = appChannelId;
    }

    public String getCenterControlBotAccount() {
        return centerControlBotAccount;
    }

    public void setCenterControlBotAccount(String centerControlBotAccount) {
        this.centerControlBotAccount = centerControlBotAccount;
    }

    public String getCenterControlBotTitle() {
        return centerControlBotTitle;
    }

    public void setCenterControlBotTitle(String centerControlBotTitle) {
        this.centerControlBotTitle = centerControlBotTitle;
    }

    public String getRecommendBotAccount() {
        return recommendBotAccount;
    }

    public void setRecommendBotAccount(String recommendBotAccount) {
        this.recommendBotAccount = recommendBotAccount;
    }

    public String getRecommendBotTitle() {
        return recommendBotTitle;
    }

    public void setRecommendBotTitle(String recommendBotTitle) {
        this.recommendBotTitle = recommendBotTitle;
    }

    public String getCancelFeather() {
        return cancelFeather;
    }

    public void setCancelFeather(String cancelFeather) {
        this.cancelFeather = cancelFeather;
    }

    public int getIncludeExitPlatform() {
        return includeExitPlatform;
    }

    public void setIncludeExitPlatform(int includeExitPlatform) {
        this.includeExitPlatform = includeExitPlatform;
    }

    public int getAuth() {
        return auth;
    }

    public void setAuth(int auth) {
        this.auth = auth;
    }

    public String getCcHint() {
        return ccHint;
    }

    public void setCcHint(String ccHint) {
        this.ccHint = ccHint;
    }

    public String getLabelBlacklist() {
        return labelBlacklist;
    }

    public void setLabelBlacklist(String labelBlacklist) {
        this.labelBlacklist = labelBlacklist;
    }

    public String getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(String gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(String gmtModified) {
        this.gmtModified = gmtModified;
    }

    public List<String> getCancelFeathers() {
        return cancelFeathers;
    }

    public void setCancelFeathers(List<String> cancelFeathers) {
        this.cancelFeathers = cancelFeathers;
    }

    public String getNotFoundBot() {
        return notFoundBot;
    }

    public void setNotFoundBot(String notFoundBot) {
        this.notFoundBot = notFoundBot;
    }
}
