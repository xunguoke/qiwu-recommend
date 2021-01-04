package ai.qiwu.com.cn.common.resolveUtils;

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
}
