package ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 封装数据
 * @author hjd
 */
@Setter
@Getter
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

}
