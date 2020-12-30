package ai.qiwu.com.cn.pojo.connectorPojo;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 存储临时作品信息和作品
 * @author hjd
 */
@Setter
@Getter
public class TemporaryWorks {
    private List<WorkInformation> workInformations;
    private List<WorksPojo> worksPojos;
}
