package ai.qiwu.com.cn.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户已玩作品
 * @author hjd
 */
@Getter
@Setter
public class UserHistory {
    private Integer id;
    private String type;
    private String workname;
    private String userid;
    private String channelid;
    private Date gmtcreate;
    private Date gmtmodified;

}
