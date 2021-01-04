package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.PayControl;
import ai.qiwu.com.cn.pojo.SeriesPay;
import ai.qiwu.com.cn.pojo.UserHistory;
import ai.qiwu.com.cn.service.handleService.WatchService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 该类主要用于查询数据库的作品
 * @author ME
 */
@Service
public class DatabaseUtils {
    /**
     * 查询数据库中指定用户已经玩过的作品
     * @return
     */
    public static List<UserHistory> findByUid(String uid, WatchService watchService) {
        //数据库中查询
        List<UserHistory> userHistory = watchService.findByUid(uid);
        return userHistory;
    }

    /**
     * 查询已购买表中的作品
     * @param watchService
     * @param uid 用户id
     * @param channelId 渠道id
     * @return
     */
    public static List<PayControl> purchaseWorks(WatchService watchService, String uid, String channelId) {
        List<PayControl> payControls= watchService.findByUidAndChannelId(uid,channelId);
        return payControls;
    }

    /**
     * 查询有购买系列作品表中的数据
     * @param watchService 类对象
     * @param uid 用户id
     * @param channelId 渠道id
     * @return
     */
    public static List<SeriesPay> purchasedSeries(WatchService watchService, String uid, String channelId) {
        List<SeriesPay> seriesPays= watchService.seriesByUidAndChannelId(uid,channelId);
        return seriesPays;
    }
}
