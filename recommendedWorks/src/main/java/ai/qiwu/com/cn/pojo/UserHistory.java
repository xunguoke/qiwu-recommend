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
    private String work_name;
    private String user_id;
    private String channel_id;
    private Date gmt_create;
    private Date gmt_modified;

}
