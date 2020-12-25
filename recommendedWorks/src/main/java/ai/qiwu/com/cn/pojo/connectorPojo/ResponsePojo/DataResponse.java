package ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hjd
 * 封装作品数据
 */
@Setter
public class DataResponse {
    private List<String> labels;
    private List<WorksPojo> works;

    public List<String> getLabels() {
        return labels;
    }

    public List<WorksPojo> getWorks() {
        return works;
    }
}
