package ai.qiwu.com.cn.service.handleService.Impl;

import ai.qiwu.com.cn.dao.WatchMapper;
import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.service.handleService.WatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hjd
 */
@Service
public class WatchServiceImpl implements WatchService {
    @Autowired(required = false)
    private WatchMapper watchMapper;

    /**
     * 根据渠道id查询数据
     * @param channelId 渠道id
     * @return
     */
    @Override
    public List<Watch> findByChannelId(String channelId) {
        return watchMapper.findByChannelId(channelId);
    }
}
