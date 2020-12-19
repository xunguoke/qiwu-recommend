package ai.qiwu.com.cn.common.resolveUtils;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * 用于解析请求
 * @author hjd
 */
@Slf4j
@Service
public class CommonlyUtils {

    /**
     * 解析请求
     * @param request
     * @return
     */
    public static Map parsingRequest(HttpServletRequest request) {
        //定义一个map集合用于存储json数据
        Map map = new HashMap<>();

        //获取请求体
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            log.warn("请求体:{}",reader);
        } catch (IOException e) {
            log.error("获取请求体错误:{}",e.toString());
            e.printStackTrace();
        }
        //转换成String
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error("读取流失败:{}",e.toString());
            e.printStackTrace();
        }

        String body=sb.toString();
        log.warn("body:{}",body);

        //将String数据转换成map
        Gson gson=new Gson();
        map = gson.fromJson(body, map.getClass());
        //log.warn("json转换成map:{}",map);

        return map;
    }

    /**
     * 用于将yyyy-MM-dd'T'HH:mm:ss转换成yyyy-MM-dd HH:mm:ss格式的时间
     * @param oldDate 需要转换的时间
     * @return
     */
    public static String dealDateFormat(String oldDate) {
        Date date1 = null;
        DateFormat df2 = null;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = df.parse(oldDate);
            SimpleDateFormat df1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
            date1 = df1.parse(date.toString());
            df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {

            e.printStackTrace();
        }
        return df2.format(date1);
    }
}
