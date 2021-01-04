package ai.qiwu.com.cn.pojo.connectorPojo.RequestPojo;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * @author hjd
 */
public class Data {
    /**
     * 变量
     */
    private Object vars;
    /**
     * 组
     */
    private List<Object> groupVars;

    public Object getVars() {
        return vars;
    }

    public void setVars(Object vars) {
        this.vars = vars;
    }

    public List<Object> getGroupVars() {
        return groupVars;
    }

    public void setGroupVars(List<Object> groupVars) {
        this.groupVars = groupVars;
    }
}
