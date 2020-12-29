package ai.qiwu.com.cn.pojo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Date;

/**
 * 数据库作品信息
 * @author hjd
 */


@Setter
@Getter
public class Watch {
    private String appchannelid;
    private String workname;
    private BigInteger watch;
    private Float score;
    private Byte online;
    private Date gmtcreate;
    private Date gmtmodified;

}

