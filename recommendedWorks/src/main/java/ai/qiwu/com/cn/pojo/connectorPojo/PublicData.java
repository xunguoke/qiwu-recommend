package ai.qiwu.com.cn.pojo.connectorPojo;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 中转使用
 * @author hjd
 */
@Getter
@Setter
public class PublicData {
    private List<String> labels;
    private List<PublicWorks> works;
}
