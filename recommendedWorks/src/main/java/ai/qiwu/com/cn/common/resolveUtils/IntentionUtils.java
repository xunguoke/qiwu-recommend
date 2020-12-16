package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.pojo.connectorPojo.WorkInformation;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

/**
 * 手机推荐返回数据
 * @author hjd
 */
@Slf4j
public class IntentionUtils {

    /**
     * 手表推荐之推荐
     * @param map 接口返回数据
     * @return
     */
    public static String recommend(Map map) {
        //获取返回信息
        String text = "";
        String title="";

        //创建一个集合用于存储排序后的游戏名和编号
        HashMap<String, String> game = new HashMap<>();
        //创建以个map集合用于存储免费游戏名和编号
        HashMap<String, String> freeGameNumber = new HashMap<>();
        //创建以个map集合用于存储收费游戏名和编号
        HashMap<String, String> paidGameNumber = new HashMap<>();
        //创建一个map集合用于免费游戏
        HashMap<String, Double> gameFree = new HashMap<>();
        //创建以个map集合用于收费游戏
        HashMap<String, Double> gameCharges = new HashMap<>();
        //获取works
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();


        for (WorksPojo work : works) {
            //获取是否收费信息
            List<String> synopsis = work.getLabels();
            for (String s : synopsis) {
                if (s.equals("New") || s.equals("免费")) {
                    //获取游戏名
                    String gameName =  work.getName();
                    //获取游戏分数
                    Double fraction = work.getScore();
                    //获取游戏编号
                    String botAccount = work.getBotAccount();
                    //存入免费游戏编号集合
                    freeGameNumber.put(gameName, botAccount);
                    //存入免费游戏集合
                    gameFree.put(gameName, fraction);
                } else if (s.equals("VIP") || s.equals("付费")) {
                    //获取游戏名
                    String gameName =  work.getName();
                    //获取游戏分数
                    Double fraction =  work.getScore();
                    //获取游戏编号
                    String botAccount =  work.getBotAccount();
                    //存入收费游戏编号集合
                    paidGameNumber.put(gameName, botAccount);
                    //存入收费游戏集合
                    gameCharges.put(gameName, fraction);
                }
            }

        }

        //对免费游戏集合按分数进行降序排序
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(gameFree.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //对收费游戏集合按分数进行降序排序
        List<Map.Entry<String, Double>> list2 = new ArrayList<Map.Entry<String, Double>>(gameCharges.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //判断集合长度
        if(list.size()>list2.size()){
            for(int i=0;i<list.size();i++){

                //判断是否最后
                if(i==list.size()-1){
                    //获取免费游戏名
                    String freeName = list.get(i).getKey();
                    //获取免费游戏名编号
                    String number = freeGameNumber.get(freeName);
                    text+=number+"+"+freeName;
                    title+="《"+freeName+"》,你可以说：打开某作品";
                    game.put(freeName,number);
                    String recommendText ="☛推荐"+text+"☚";
                    String recommendName="为您推荐以上作品："+title;
                    return TypeRecommendation.packageResult(recommendName,recommendText);
                }

                //获取免费游戏名
                String freeName = list.get(i).getKey();
                //获取免费游戏名编号
                String number = freeGameNumber.get(freeName);
                text+=number+"+"+freeName+",";
                title+="《"+freeName+"》,";
                game.put(freeName,number);
                if(list2.size()>i){
                    //获取收费游戏名
                    String freeGameName2 = list2.get(i).getKey();
                    //获取收费游戏名编号
                    String number2 = paidGameNumber.get(freeGameName2);
                    text+=number2+"+"+freeGameName2+",";
                    title+="《"+freeGameName2+"》,";
                    game.put(freeGameName2,number2);
                }
            }
        }else{
            for(int i=0;i<list2.size();i++){


                if(list.size()>=i){
                    //获取免费游戏名
                    String freeName = list.get(i).getKey();
                    //获取免费游戏名编号
                    String number = freeGameNumber.get(freeName);
                    text+=number+"+"+freeName+",";
                    title+="《"+freeName+"》,";
                    game.put(freeName,number);
                }
                if(i==list2.size()-1){
                    //获取收费游戏名
                    String freeGameName2 = list.get(i).getKey();
                    //获取收费游戏名编号
                    String number2 = paidGameNumber.get(freeGameName2);
                    text+=number2+"+"+freeGameName2;
                    title+="《"+freeGameName2+"》,你可以说：打开某作品";
                    game.put(freeGameName2,number2);
                    String recommendText ="☛推荐"+text+"☚";
                    String recommendName="为您推荐以上作品："+title;
                    return TypeRecommendation.packageResult(recommendName,recommendText);
                }
                //获取收费游戏名
                String freeGameName2 = list2.get(i).getKey();
                //获取收费游戏名编号
                String number2 = paidGameNumber.get(freeGameName2);
                text+=number2+"+"+freeGameName2+",";
                title+="《"+freeGameName2+"》,";
                game.put(freeGameName2,number2);
            }
        }

        return null;
    }


    /**
     * 手表推荐之类型推荐
     * @param map 接口返回数据
     * @return
     */
    public static String typeRecommendation(Map map,String semantics) {
        //定义一个String类型的变量用于存储筛选的游戏
        String text="";
        String title="";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, Double> gameRating = new HashMap<>();

        //获取works将map转对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取类型列表
            List<String> labels = work.getLabels();
            for (String label : labels) {
                if(label.equals(semantics)){
                    //如果该作品属于该类型
                    //获取游戏名
                    String gameName =  work.getName();
                    log.warn("gameName:{}",gameName);
                    //获取游戏分数
                    Double fraction = work.getScore();
                    log.warn("fraction:{}",fraction);
                    //获取游戏编号
                    String botAccount = work.getBotAccount();
                    log.warn("botAccount:{}",botAccount);
                    //存入游戏编号集合
                    gameNumber.put(gameName, botAccount);
                    //存入游戏评分集合
                    gameRating.put(gameName, fraction);
                }
            }
        }

        //将游戏按照评分降序排序
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(gameRating.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //循环遍历集合，提取游戏名游戏编号
        for (int i=0;i<list.size();i++){
            //判断是否是最后
            if(i==list.size()-1){
                //获取游戏名
                String gameName2 = list.get(i).getKey();
                //获取收费游戏名编号
                String number2 = gameNumber.get(gameName2);
                text+=number2+"+"+gameName2;
                title+="《"+gameName2+"》,你可以说：打开某作品";
                String recommendText ="☛推荐"+text+"☚";
                String recommendName="为您推荐以上作品："+title;
                return TypeRecommendation.packageResult(recommendName,recommendText);
            }

            //获取游戏名
            String gameName2 = list.get(i).getKey();
            //获取收费游戏名编号
            String number2 = gameNumber.get(gameName2);
            text+=number2+"+"+gameName2+",";
            title+="《"+gameName2+"》,";

        }
        return null;
    }


    /**
     * 手机推荐之最新推荐
     * @param map 接口返回数据
     * @return
     */
    public static String latestCreation(Map map) {
        //定义一个String类型的变量用于存储筛选的游戏
        String text="";
        String title="";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏上线时间
        HashMap<String, String> gameLaunchTime = new HashMap<>();

        //获取works将map转对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName =  work.getName();
            //获取游戏上线时间
            String gmtApply = work.getGmtApply();
            //将时间转换成指定格式
            String timeOnline = CommonlyUtils.dealDateFormat(gmtApply);
            //获取游戏编号
            String botAccount = work.getBotAccount();
            //存入游戏编号集合
            gameNumber.put(gameName, botAccount);
            //存入游戏上线时间集合
            gameLaunchTime.put(gameName, timeOnline);

        }

        //将游戏上线时间降序排序
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(gameLaunchTime.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //循环遍历集合，提取游戏名游戏编号
        for (int i=0;i<list.size();i++){
            //判断是否是最后
            if(i==list.size()-1){
                //获取游戏名
                String gameName2 = list.get(i).getKey();
                //获取收费游戏名编号
                String number2 = gameNumber.get(gameName2);
                text+=number2+"+"+gameName2;
                title+="《"+gameName2+"》,你可以说：打开某作品";
                String recommendText ="☛推荐"+text+"☚";
                String recommendName="为您推荐以上作品："+title;
                return TypeRecommendation.packageResult(recommendName,recommendText);
            }

            //获取游戏名
            String gameName2 = list.get(i).getKey();
            //获取游戏名编号
            String number2 = gameNumber.get(gameName2);
            text+=number2+"+"+gameName2+",";
            title+="《"+gameName2+"》,";

        }
        return null;
    }


    /**
     * 手表推荐之类似作品推荐
     * @param map 接口返回数据
     * @param semantics 语义
     * @return
     */
    public static String similarWorks(Map map, String semantics) {
        //用于存储临时数据
        List<WorkInformation> listWork = new ArrayList<WorkInformation>();
        //用于存储用户说的作品类型列表
        List<String> typeList = null;
        //定义一个String类型的变量用于存储筛选的游戏
        String text="";
        String title="";

        //获取works将map转对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();

        //获取作品类型列表
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName1 =  work.getName();
            //判断游戏名是否和语义相同
            if(gameName1.equals(semantics)){
                //获取类型列表
                typeList = work.getLabels();
            }
        }

        //获取相似作品
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName =  work.getName();
            log.warn("gameName2:{}",gameName);
            if(!gameName.equals(semantics)){
                //获取游戏分数
                Double fraction = work.getScore();
                log.warn("fraction:{}",fraction);
                //获取游戏编号
                String botAccount = work.getBotAccount();
                log.warn("botAccount:{}",botAccount);
                //获取类型列表
                List<String> labels = work.getLabels();
                labels.retainAll(typeList);
                //判断有相同标签才存入集合
                int size = labels.size();
                log.warn("size:{}",size);
                if (size>0){
                    //创建对象传入参数
                    WorkInformation information = new WorkInformation(gameName, botAccount, fraction, size);
                    listWork.add(information);
                }

            }
        }

        //循环集合按照Size倒序排序，size相同时按照评分倒序
        Collections.sort(listWork, new Comparator<WorkInformation>() {
            @Override
            public int compare(WorkInformation o1, WorkInformation o2) {
                Integer s1 = o1.getSize();
                Integer s2 = o2.getSize();

                int temp = s2.compareTo(s1);

                if(temp != 0){
                    return  temp;
                }

                double m1 = o1.getFraction();
                double m2 = o2.getFraction();

                BigDecimal data1 = new BigDecimal(m1);
                BigDecimal data2 = new BigDecimal(m2);

                return data2.compareTo(data1);
            }
        });

        //循环遍历集合，提取游戏名游戏编号
        for (int i=0;i<listWork.size();i++){
            //判断是否是最后
            if(i==listWork.size()-1){
                //获取游戏名
                String gameName2 = listWork.get(i).getGameName();
                //获取收费游戏名编号
                String number2 = listWork.get(i).getBotAccount();
                text+=number2+"+"+gameName2;
                title+="《"+gameName2+"》,你可以说：打开某作品";
                String recommendText ="☛推荐"+text+"☚";
                String recommendName="为您推荐以上作品："+title;
                return TypeRecommendation.packageResult(recommendName,recommendText);
            }

            //获取游戏名
            String gameName2 = listWork.get(i).getGameName();
            //获取收费游戏名编号
            String number2 = listWork.get(i).getBotAccount();
            text+=number2+"+"+gameName2+",";
            title+="《"+gameName2+"》,";
        }
        return null;
    }

    /**
     * 手表推荐之某作者的作品推荐
     * @param map 接口返回数据
     * @param semantics 语义
     * @return
     */
    public static String authorWorks(Map map, String semantics) {
        //定义一个String类型的变量用于存储筛选的游戏
        String text="";
        String title="";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, Double> gameRating = new HashMap<>();

        //获取works将map转对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取作者名字
            String authorName = work.getAuthorName();
            if (authorName.equals(semantics)){
                //获取游戏名
                String gameName =  work.getName();
                log.warn("gameName:{}",gameName);
                //获取游戏分数
                Double fraction = work.getScore();
                log.warn("fraction:{}",fraction);
                //获取游戏编号
                String botAccount = work.getBotAccount();
                log.warn("botAccount:{}",botAccount);
                //存入游戏编号集合
                gameNumber.put(gameName, botAccount);
                //存入游戏评分集合
                gameRating.put(gameName, fraction);
            }
        }


        //将游戏按照评分降序排序
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(gameRating.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //循环遍历集合，提取游戏名游戏编号
        for (int i=0;i<list.size();i++){
            //判断是否是最后
            if(i==list.size()-1){
                //获取游戏名
                String gameName2 = list.get(i).getKey();
                //获取游戏名编号
                String number2 = gameNumber.get(gameName2);
                text+=number2+"+"+gameName2;
                title+="《"+gameName2+"》,你可以说：打开某作品";
                String recommendText ="☛推荐"+text+"☚";
                String recommendName="为您推荐以上作品："+title;
                return TypeRecommendation.packageResult(recommendName,recommendText);
            }

            //获取游戏名
            String gameName2 = list.get(i).getKey();
            //获取收费游戏名编号
            String number2 = gameNumber.get(gameName2);
            text+=number2+"+"+gameName2+",";
            title+="《"+gameName2+"》,";

        }
        return null;
    }

    /**
     * 手表推荐之作品类型查询
     * @param map 接口返回数据
     * @param semantics 语义
     * @return
     */
    public static String typeOfWork(Map map, String semantics) {
        //定义一个String类型的变量用于存储筛选的游戏
        String text="";
        String title="";

        //获取works将map转对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName =  work.getName();
            if (gameName.equals(semantics)){
                List<String> labels = work.getLabels();
                for(int i=0;i<labels.size();i++){
                    if(i==labels.size()-1){
                        String style = labels.get(i);
                        title+=style;
                        String recommendText ="";
                        String recommendName=gameName+"的类型是："+title;
                        return TypeRecommendation.packageResult(recommendName,recommendText);
                    }
                    String style = labels.get(i);
                    title+=style+",";
                }
            }
        }
        return null;
    }

    /**
     * 手表推荐之作者查询
     * @param map 接口返回数据
     * @param semantics 语义
     * @return
     */
    public static String authorQuery(Map map, String semantics) {
        //定义一个String类型的变量用于存储筛选的游戏
        String text="";
        String title="";

        //获取works将map转对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName =  work.getName();
            if (gameName.equals(semantics)){
                //获取作者名字
                String authorName = work.getAuthorName();
                String recommendText ="";
                String recommendName=gameName+"的作者是："+authorName;
                return TypeRecommendation.packageResult(recommendName,recommendText);
            }
        }
        return null;
    }

    /**
     * 手表推荐之作品编号查询
     * @param map 接口返回数据
     * @param semantics 语义
     * @return
     */
    public static String workNumber(Map map, String semantics) {
        //定义一个String类型的变量用于存储筛选的游戏
        String text="";
        String title="";

        //获取works将map转对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName =  work.getName();
            if (gameName.equals(semantics)){
                //获取作者名字
                String botAccount = work.getBotAccount();
                String recommendText ="";
                String recommendName=botAccount;
                return TypeRecommendation.packageResult(recommendName,recommendText);
            }
        }
        return null;
    }

    /**
     * 手表推荐之人群推荐
     * @param map 接口返回数据
     * @param semantics 语义
     * @return
     */
    public static String crowdRecommendation(Map map, String semantics) {
        //定义一个String类型的变量用于存储筛选的游戏
        String text="";
        String title="";
        String name = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, Double> gameRating = new HashMap<>();

        //获取works将map转对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取作者名字
            List<String> suitCrowds = work.getSuitCrowds();
            for (int i=0;i<suitCrowds.size();i++){
                name = suitCrowds.get(i);
                if (name.equals(semantics)){
                    //获取游戏名
                    String gameName =  work.getName();
                    log.warn("gameName:{}",gameName);
                    //获取游戏分数
                    Double fraction = work.getScore();
                    log.warn("fraction:{}",fraction);
                    //获取游戏编号
                    String botAccount = work.getBotAccount();
                    log.warn("botAccount:{}",botAccount);
                    //存入游戏编号集合
                    gameNumber.put(gameName, botAccount);
                    //存入游戏评分集合
                    gameRating.put(gameName, fraction);
                }
            }

        }


        //将游戏按照评分降序排序
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(gameRating.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //循环遍历集合，提取游戏名游戏编号
        for (int i=0;i<list.size();i++){
            //判断是否是最后
            if(i==list.size()-1){
                //获取游戏名
                String gameName2 = list.get(i).getKey();
                //获取游戏名编号
                String number2 = gameNumber.get(gameName2);
                text+=number2+"+"+gameName2;
                title+="《"+gameName2+"》,你可以说：打开某作品";
                String recommendText ="☛推荐"+text+"☚";
                String recommendName="为您推荐以上适合"+name+"作品："+title;
                return TypeRecommendation.packageResult(recommendName,recommendText);
            }

            //获取游戏名
            String gameName2 = list.get(i).getKey();
            //获取收费游戏名编号
            String number2 = gameNumber.get(gameName2);
            text+=number2+"+"+gameName2+",";
            title+="《"+gameName2+"》,";

        }
        return null;
    }

    /**
     * 手表推荐之收藏最多的作品
     * @param map
     * @param semantics
     * @return
     */
    public static String mostFavorites(Map map, String semantics) {
        return null;
    }
}
