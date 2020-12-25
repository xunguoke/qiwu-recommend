package ai.qiwu.com.cn.service.handleService;

import ai.qiwu.com.cn.pojo.UserHistory;
import ai.qiwu.com.cn.pojo.Watch;

import java.util.List;

/**
 * 手表推荐
 * @author hjd
 */
public interface WatchService {
    /**
     * 根据渠道id查询数据
     * @param channelId 渠道id
     * @return
     */
    List<Watch> findByChannelId(String channelId);

    /**
     * 根据用户id查询作品
     * @param uid 用户id
     * @return
     */
    List<UserHistory> findByUid(String uid);

    /**
     * 根据用户id以及时间段查询作品
     * @param uid 用户id
     * @param startingTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    List<UserHistory> findByUidOfDate(String uid, String startingTime, String endTime);
}
