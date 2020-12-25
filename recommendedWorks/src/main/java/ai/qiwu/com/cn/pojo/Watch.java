package ai.qiwu.com.cn.pojo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Date;

/**
 * 数据库作品信息
 * @author hjd
 */

@Getter
@Setter
public class Watch {
    private String app_channel_id;
    private String work_name;
    private BigInteger watch;
    private Float score;
    private Byte online;
    private Date gmt_create;
    private Date gmt_modified;
}

