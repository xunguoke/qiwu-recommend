package ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 作品信息
 * @author hjd
 */
@Setter
@Getter
public class WorksPojo {
        private String name;
        private String ttsName;
        private String seriesName;
        private String nicknames;
        private String intro;
        private String authorName;
        private int watch;
        private String preface;
        private String difficulty;
        private List<String> suitCrowds;
        private int startAge;
        private int stopAge;
        private Double score;
        private boolean free;
        private int plotCount;
        private String audioType;
        private List<String> labels;
        private String gmtApply;
        private String botAccount;
}
