package ai.qiwu.com.cn.pojo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Date;

/**
 * 数据库作品信息
 * @author hjd
 */



public class Watch {
    private String app_channel_id;
    private String work_name;
    private BigInteger watch;
    private Float score;
    private Byte online;
    private Date gmt_create;
    private Date gmt_modified;

    public void setApp_channel_id(String app_channel_id) {
        this.app_channel_id = app_channel_id;
    }

    public void setWork_name(String work_name) {
        this.work_name = work_name;
    }

    public void setWatch(BigInteger watch) {
        this.watch = watch;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public void setOnline(Byte online) {
        this.online = online;
    }

    public void setGmt_create(Date gmt_create) {
        this.gmt_create = gmt_create;
    }

    public void setGmt_modified(Date gmt_modified) {
        this.gmt_modified = gmt_modified;
    }

    public String getApp_channel_id() {
        return app_channel_id;
    }

    public String getWork_name() {
        return work_name;
    }

    public BigInteger getWatch() {
        return watch;
    }

    public Float getScore() {
        return score;
    }

    public Byte getOnline() {
        return online;
    }

    public Date getGmt_create() {
        return gmt_create;
    }

    public Date getGmt_modified() {
        return gmt_modified;
    }
}

