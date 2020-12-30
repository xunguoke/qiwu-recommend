package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;

import java.util.List;

/**
 * 该类主要用于筛选作品
 * @author hjd
 */
public class FilterWorksUtils {
    /**
     * 筛选不包含禁用标签且包含意图的作品
     * @param dataResponse 封装的作品信息
     * @param semantics 用户意图
     * @param strings 禁用标签
     * @return
     */
    public static DataResponse nonProhibitedWorks(DataResponse dataResponse, String semantics, List<String> strings) {
        if(strings!=null&&strings.contains(semantics)){
            return null;
        }else {
            return dataResponse;
        }
    }
}
