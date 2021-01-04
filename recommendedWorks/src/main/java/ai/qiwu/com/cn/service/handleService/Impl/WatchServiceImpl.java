package ai.qiwu.com.cn.service.handleService.Impl;

import ai.qiwu.com.cn.dao.WatchMapper;
import ai.qiwu.com.cn.pojo.PayControl;
import ai.qiwu.com.cn.pojo.SeriesPay;
import ai.qiwu.com.cn.pojo.UserHistory;
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
     * @param online
     * @return
     */
    @Override
    public List<Watch> findByChannelId(String channelId, Integer online) {
        return watchMapper.findByChannelId(channelId,online);
    }

    /**
     * 根据用户id查询作品
     * @param uid 用户id
     * @return
     */
    @Override
    public List<UserHistory> findByUid(String uid) {
        return watchMapper.findByUid(uid);
    }

    /**
     * 根据用户id以及时间段查询作品
     * @param uid 用户id
     * @param startingTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @Override
    public List<UserHistory> findByUidOfDate(String uid, String startingTime, String endTime) {
        return watchMapper.findByUidOfDate(uid,startingTime,endTime);
    }

    /**
     * 根据用户ID渠道id查询购买作品表中的数据
     * @param uid 用户id
     * @param channelId 渠道id
     * @return
     */
    @Override
    public List<PayControl> findByUidAndChannelId(String uid, String channelId) {
        return watchMapper.findByUidAndChannelId(uid,channelId);
    }

    /**
     * 查询有购买系列作品表中的数据
     * @param uid 用户id
     * @param channelId 渠道id
     */
    @Override
    public List<SeriesPay> seriesByUidAndChannelId(String uid, String channelId) {
        return watchMapper.seriesByUidAndChannelId(uid,channelId);
    }

    /**
     * 根据用户ID渠道id查询和时间段购买作品表中的数据
     * @param uid 用户id
     * @param channelId 渠道id
     * @param startingTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @Override
    public List<PayControl> findByUidOfTimeOfChannelId(String uid, String channelId, String startingTime, String endTime) {
        return watchMapper.findByUidOfTimeOfChannelId(uid,channelId,startingTime,endTime);
    }

    /**
     * 根据用户ID渠道id查询和时间段购买系列作品表中的数据
     * @param uid 用户id
     * @param channelId 渠道id
     * @param startingTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @Override
    public List<SeriesPay> seriesPayByUidOfTimeOfChannelId(String uid, String channelId, String startingTime, String endTime) {
        return watchMapper.seriesPayByUidOfTimeOfChannelId(uid,channelId,startingTime,endTime);
    }
}
