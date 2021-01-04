package ai.qiwu.com.cn.pojo;

import java.util.Date;

/**
 * 已购买作品表
 */
public class PayControl {
    private String workname;
    private String uid;
    private String channelid;
    private String controlid;
    private Integer status;
    private Date gmtcreate;
    private Date gmtmodified;

    public String getWorkname() {
        return workname;
    }

    public void setWorkname(String workname) {
        this.workname = workname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public String getControlid() {
        return controlid;
    }

    public void setControlid(String controlid) {
        this.controlid = controlid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
