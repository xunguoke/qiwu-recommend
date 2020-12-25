package ai.qiwu.com.cn.common.resolveUtils;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间工具类
 * @author hjd
 */
public class DateUtil {
    /**
     * 获取当前时间
     * @return
     */
    public static String currentTimes(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String format = df.format(new Date());// new Date()为获取当前系统时间
        return format;
    }
    
    /**
     * 获取今天的时间范围
     * @return 返回长度为2的字符串集合，如：[2017-11-03 00:00:00, 2017-11-03 24:00:00]
     */
    public static List<String> getToday() {
        List<String> dataList = new ArrayList<>(2);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DATE, 0);
        String today = dateFormat.format(calendar.getTime());
        dataList.add(today + " 00:00:00");
        dataList.add(today + " 24:00:00");
        return dataList;
    }

    /**
     * 获取昨天的时间范围
     * @return 返回长度为2的字符串集合，如：[2017-11-02 00:00:00, 2017-11-02 24:00:00]
     */
    public static List<String> getYesterday() {
        List<String> dataList = new ArrayList<>(2);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //获取指定时间
        Calendar calendar = Calendar.getInstance();
        //获取当天时间的起点时间
        calendar.setTimeInMillis(System.currentTimeMillis());
        //获取当前日期前一天
        calendar.add(Calendar.DATE, -1);
        //转换成指定格式
        String yesterday = dateFormat.format(calendar.getTime());
        dataList.add(yesterday + " 00:00:00");
        dataList.add(yesterday + " 24:00:00");
        return dataList;
    }

    /**
     * 获取前天的时间范围
     * @return 返回长度为2的字符串集合，如：[2017-11-02 00:00:00, 2017-11-02 24:00:00]
     */
    public static List<String> getDayBeforeYesterday() {
        List<String> dataList = new ArrayList<>(2);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //获取指定时间
        Calendar calendar = Calendar.getInstance();
        //获取当天时间的起点时间
        calendar.setTimeInMillis(System.currentTimeMillis());
        //获取当前日期前一天
        calendar.add(Calendar.DATE, -1);
        //转换成指定格式
        String yesterday = dateFormat.format(calendar.getTime());
        dataList.add(yesterday + " 00:00:00");
        dataList.add(yesterday + " 24:00:00");
        return dataList;
    }

    /**
     * 获取本周的时间范围
     * @return 返回长度为2的字符串集合，如：[2017-10-30 00:00:00, 2017-11-05 24:00:00]
     */
    public static List<String> getCurrentWeek() {
        List<String> dataList = new ArrayList<>(2);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);//设置周一为一周之内的第一天
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String monday = dateFormat.format(calendar.getTime()) + " 00:00:00";
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String sunday = dateFormat.format(calendar.getTime()) + " 24:00:00";
        dataList.add(monday);
        dataList.add(sunday);
        return dataList;
    }

    /**
     * 获取上周的时间范围
     * @return 返回长度为2的字符串集合，如：[2017-10-30 00:00:00, 2017-11-05 24:00:00]
     */
    public static List<String> getLastWeek() {
        List<String> dataList = new ArrayList<>(2);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);//设置周一为一周之内的第一天
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY-7);
        String monday = dateFormat.format(calendar.getTime()) + " 00:00:00";
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY-7);
        String sunday = dateFormat.format(calendar.getTime()) + " 24:00:00";
        dataList.add(monday);
        dataList.add(sunday);
        return dataList;
    }

    /**
     * 获取本月的时间范围
     * @return 返回长度为2的字符串集合，如：[2017-11-01 00:00:00, 2017-11-30 24:00:00]
     */
    public static List<String> getCurrentMonth() {
        List<String> dataList = new ArrayList<>(2);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MONTH, 0);//0表示本月
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String firstDayOfMonth = dateFormat.format(calendar.getTime()) + " 00:00:00";
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        String lastDayOfMonth = dateFormat.format(calendar.getTime()) + " 24:00:00";
        dataList.add(firstDayOfMonth);
        dataList.add(lastDayOfMonth);
        return dataList;
    }

    /**
     * 获取上个月的时间范围
     * @return 返回长度为2的字符串集合，如：[2017-11-01 00:00:00, 2017-11-30 24:00:00]
     */
    public static List<String> getLastMonth() {
        List<String> dataList = new ArrayList<>(2);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MONTH, -1);//0表示
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String firstDayOfMonth = dateFormat.format(calendar.getTime()) + " 00:00:00";
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        String lastDayOfMonth = dateFormat.format(calendar.getTime()) + " 24:00:00";
        dataList.add(firstDayOfMonth);
        dataList.add(lastDayOfMonth);
        return dataList;
    }

    /**
     * 获取今年某月份的开始结束日期
     *
     * @param offset 0本月，1下个月，-1上个月，依次类推
     * @return
     */
    public static List<String> someMonth(int offset) {
        List<String> dataList = new ArrayList<>(2);
        Calendar calendar = Calendar.getInstance();
        //获取当前年份
        int i = calendar.get(Calendar.YEAR);
        calendar.set(Calendar.YEAR, i);
        calendar.set(Calendar.MONTH, offset - 1);
        calendar.set(Calendar.DATE, 1);
        String str = getCalendarToTimestapStr(setStarthhmmss(calendar));
        dataList.add(str);
        calendar.set(Calendar.YEAR, i);
        calendar.set(Calendar.MONTH, offset);
        calendar.set(Calendar.DATE, 1);
        String strs = getCalendarToTimestapStr(setStarthhmmss(calendar));
        dataList.add(strs);
        return dataList;
    }

    /**
     * 获取某天的开始日期
     *
     * @param offset 0今天，1明天，-1昨天，依次类推
     * @return
     */
    public static List<String> dayStart(int offset) {
        List<String> dataList = new ArrayList<>(2);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDate.now().plusDays(offset).atStartOfDay();
        dataList.add(localDateTime.format(dtf));
        return dataList;

    }

    /**
     * 获取某周的开始日期
     *
     * @param offset 0本周，1下周，-1上周，依次类推
     * @return
     */
    public static List<String> weekStart(int offset) {
        List<String> dataList = new ArrayList<>(2);
        LocalDate localDate = LocalDate.now().plusWeeks(offset);
        LocalDate date = localDate.with(DayOfWeek.MONDAY);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = date.format(fmt)+" 00:00:00";
        dataList.add(dateStr);
        return dataList;
    }

    /**
     * 获取某月份的开始日期
     *
     * @param offset 0本月，1下个月，-1上个月，依次类推
     * @return
     */
    public static List<String> monthStart(int offset) {
        List<String> dataList = new ArrayList<>(2);
        DateTimeFormatter dateFormat =DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.now().plusMonths(offset).with(TemporalAdjusters.firstDayOfMonth());
        String firstDayOfMonth = date.format(dateFormat) + " 00:00:00";
        dataList.add(firstDayOfMonth);
        return dataList;
    }

    /**
     * 获取某年的开始日期
     *
     * @param offset 0今年，1明年，-1去年，依次类推
     * @return
     */
    public static List<String> yearStart(int offset) {
        List<String> dataList = new ArrayList<>(2);
        DateTimeFormatter dateFormat =DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.now().plusYears(offset).with(TemporalAdjusters.firstDayOfYear());
        String firstDayOfMonth = localDate.format(dateFormat) + " 00:00:00";
        dataList.add(firstDayOfMonth);
        return dataList;
    }

    /**
     * 获取 日历 的 时间戳字符串 （月 需要 +1）
     */
    private static String getCalendarToTimestapStr(Calendar cal){

        String yyyymmdd = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
        String hhmmss = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
        return yyyymmdd + " "+ hhmmss;
    }

    /**
     * 设置 开始时间的 时分秒
     */
    private static Calendar setStarthhmmss(Calendar cal){
        cal.set(Calendar.HOUR_OF_DAY, 00);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        return cal;
    }



}
