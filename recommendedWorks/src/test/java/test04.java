import ai.qiwu.com.cn.common.resolveUtils.DateUtil;
import ai.qiwu.com.cn.common.resolveUtils.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test04 {
    public static void main(String[] args) {
        String aa="诗词+过于+你好";
        String[] split = aa.split("[+]");
        List<String> r = Arrays.asList(split);
        for (String s : r) {
            System.out.println(s);
        }

    }

}
