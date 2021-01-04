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

    public String getAppchannelid() {
        return appchannelid;
    }

    public void setAppchannelid(String appchannelid) {
        this.appchannelid = appchannelid;
    }

    public String getWorkname() {
        return workname;
    }

    public void setWorkname(String workname) {
        this.workname = workname;
    }

    public BigInteger getWatch() {
        return watch;
    }

    public void setWatch(BigInteger watch) {
        this.watch = watch;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Byte getOnline() {
        return online;
    }

    public void setOnline(Byte online) {
        this.online = online;
    }

    public Date getGmtcreate() {
        return gmtcreate;
    }

    public void setGmtcreate(Date gmtcreate) {
        this.gmtcreate = gmtcreate;
    }

    public Date getGmtmodified() {
        return gmtmodified;
    }

    public void setGmtmodified(Date gmtmodified) {
        this.gmtmodified = gmtmodified;
    }
}

