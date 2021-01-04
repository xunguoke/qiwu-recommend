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

    public List<WorkInformation> getWorkInformations() {
        return workInformations;
    }

    public void setWorkInformations(List<WorkInformation> workInformations) {
        this.workInformations = workInformations;
    }

    public List<WorksPojo> getWorksPojos() {
        return worksPojos;
    }

    public void setWorksPojos(List<WorksPojo> worksPojos) {
        this.worksPojos = worksPojos;
    }
}
