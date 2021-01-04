package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.RequestPojo.Data;
import ai.qiwu.com.cn.pojo.connectorPojo.RequestPojo.Radical;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 该类用于封装返回结果
 * @author hjd
 */
@Slf4j
public class ResultUtils {
    /**
     * 封装返回结果
     *
     * @param workInformation 返回推荐的作品信息
     * @param listOfWorks 需要返回的作品列表
     * @return
     */
    public static String packageResult(String workInformation, String listOfWorks) {

        //封装返回结果
        Radical radical=new Radical();
        radical.setCode(1);
        radical.setMsg("成功");
        Data data=new Data();
        HashMap<String, Object> vars = new HashMap<>();
        vars.put("推荐作品列表",listOfWorks);
        vars.put("手表推荐返回信息",workInformation);
        data.setVars(vars);
        List<Object> list = new ArrayList<>();
        data.setGroupVars(list);
        radical.setData(data);

        //将对象转换成json
        String s = JSON.toJSONString(radical);
        return s;
    }
}
