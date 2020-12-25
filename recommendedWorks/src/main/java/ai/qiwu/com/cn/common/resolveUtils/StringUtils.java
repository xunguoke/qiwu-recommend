package ai.qiwu.com.cn.common.resolveUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串操作工具类
 * @author hjd
 */
public class StringUtils {
    /**
     * 判断字符串是否包含数字
     * @param content 字符串
     * @return
     */
    public static boolean HasDigit(String content) {
        boolean flagq = false;
        String ut=".*\\d+.*";
        Matcher matcher =  Pattern.compile(ut).matcher(content);
        if(matcher.matches()){
            flagq=true;
        }
        return flagq;
    }

    /**
     * 获取字符串中的所有数字
     * @param content 字符串
     * @return
     */
    public static String getNumbers(String content) {
        String num = "";
        String aa="\\d+";
        Pattern compile = Pattern.compile(aa);
        Matcher matcher = compile.matcher(content);
        while (matcher.find()){
            num+=matcher.group();
        }
        return num;
    }
}
