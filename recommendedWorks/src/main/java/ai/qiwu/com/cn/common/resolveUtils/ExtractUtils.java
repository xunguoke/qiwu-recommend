package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.UserHistory;
import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.PublicData;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.service.handleService.WatchService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 获取数据库和接口中交集作品
 * @author hjd
 */
@Service
@Slf4j
public class ExtractUtils {
    /**
     * 获取交集作品
     * @param watches
     * @param map
     * @return
     */
    public static DataResponse intersectionWorks(List<Watch> watches, Map map) {
        //定义两个List用于存储渠道作品名和接口作品名
        List<String> channels = null;
        List<String> interfaceWorks=null;

        List<WorksPojo> worksList = null;
        PublicData publicData = new PublicData();



        //判断渠道中是否有作品
        if (watches.size() > 0) {
            //获取渠道中所有作品名
            for (Watch watch : watches) {
                channels.add(watch.getWork_name());
            }
            //获取接口中所有作品
            DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
            List<WorksPojo> works = dataResponse.getWorks();
            publicData.setLabels(dataResponse.getLabels());
            //循环遍历接口中的作品名
            for (WorksPojo work : works) {
                interfaceWorks.add(work.getName());
            }

            //获取交集获取作品名
            channels.retainAll(interfaceWorks);

            //循环遍历接口中的所有作品
            for (WorksPojo work : works) {
                String name = work.getName();
                for (String channel : channels) {
                    if(name.equals(channel)){
                        worksList.add(work);
                    }
                }
            }
            dataResponse.setWorks(worksList);

            //根据不同意图返回作品
            return dataResponse;
        }else{

            //获取接口中所有作品
            DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
            //从接口中筛选数据返回
            return dataResponse;
        }

    }


    /**
     * 数据库与作品接口的交集
     * @param watches
     * @param map
     * @return
     */
    public static DataResponse channelWorks(List<Watch> watches, Map map) {
        //定义两个List用于存储渠道作品名和接口作品名
        List<String> channels = null;
        List<String> interfaceWorks=null;

        List<WorksPojo> worksList = null;
        PublicData publicData = new PublicData();

        //获取渠道中所有作品名
        for (Watch watch : watches) {
            channels.add(watch.getWork_name());
        }
        //获取接口中所有作品
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();
        publicData.setLabels(dataResponse.getLabels());
        //循环遍历接口中的作品名
        for (WorksPojo work : works) {
            interfaceWorks.add(work.getName());
        }

        //获取交集获取作品名
        channels.retainAll(interfaceWorks);

        //循环遍历接口中的所有作品
        for (WorksPojo work : works) {
            String name = work.getName();
            for (String channel : channels) {
                if(name.equals(channel)){
                    worksList.add(work);
                }
            }
        }
        dataResponse.setWorks(worksList);
        return dataResponse;
    }


    /**
     * 获取已玩作品和渠道作品以及数据库作品的交集
     * @param watches
     * @param map
     * @return
     */
    public static DataResponse playedWorks(List<Watch> watches, Map map, String uid, WatchService watchService) {
        List<UserHistory> byUid = TypeRecommendation.findByUid(uid, watchService);
        //定义两个List用于存储渠道作品名和接口作品名
        List<String> channels = null;
        List<String> interfaceWorks=null;

        List<WorksPojo> worksList = null;
        PublicData publicData = new PublicData();

        //获取渠道中所有作品名
        for (UserHistory watch : byUid) {
            channels.add(watch.getWork_name());
        }
        //获取接口中所有作品
        DataResponse dataResponse = ExtractUtils.channelWorks(watches, map);
        List<WorksPojo> works = dataResponse.getWorks();
        publicData.setLabels(dataResponse.getLabels());
        //循环遍历接口中的作品名
        for (WorksPojo work : works) {
            interfaceWorks.add(work.getName());
        }

        //获取交集获取作品名
        channels.retainAll(interfaceWorks);

        //循环遍历接口中的所有作品
        for (WorksPojo work : works) {
            String name = work.getName();
            for (String channel : channels) {
                if(name.equals(channel)){
                    worksList.add(work);
                }
            }
        }
        dataResponse.setWorks(worksList);
        return dataResponse;
    }
}
