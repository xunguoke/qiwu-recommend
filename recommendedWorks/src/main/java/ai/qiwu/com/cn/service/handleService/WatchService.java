package ai.qiwu.com.cn.service.handleService;

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
}
