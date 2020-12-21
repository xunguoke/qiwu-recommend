package ai.qiwu.com.cn.dao;

import ai.qiwu.com.cn.pojo.UserHistory;
import ai.qiwu.com.cn.pojo.Watch;

import java.util.List;

/**
 * @author hjd
 */
public interface WatchMapper {

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
}
