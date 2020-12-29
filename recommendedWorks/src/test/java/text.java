import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Collections;

import ai.qiwu.com.cn.common.resolveUtils.DateUtil;
import ai.qiwu.com.cn.common.resolveUtils.ExtractUtils;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;


public class text {
    public static void main(String[] args) {
        String aa="你好+我好";
        aa.substring(0, aa.indexOf("+"));
        System.out.println(aa);
    }
}