package ai.qiwu.com.cn.pojo.connectorPojo;

import lombok.Getter;
import lombok.Setter;

/**
 * 响应数据封装
 * @author hjd
 */
@Getter
@Setter
public class RecommendResponse {
    /**
     * 推荐的作品
     */
    private String recommend;

    public String getRecommend() {
        return recommend;
    }

    public void setRecommend(String recommend) {
        this.recommend = recommend;
    }
}
