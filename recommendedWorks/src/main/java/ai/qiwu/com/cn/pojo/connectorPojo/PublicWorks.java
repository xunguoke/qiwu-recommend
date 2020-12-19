package ai.qiwu.com.cn.pojo.connectorPojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 中转使用
 * @author hjd
 */
@Getter
@Setter
public class PublicWorks {
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
