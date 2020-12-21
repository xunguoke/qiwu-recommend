package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 解析各种意图工具类
 * @author hjd
 */
@Service
public class MeaningUtils {
    /**
     * 手表推荐之推荐
     * @param map 接口返回数据
     * @param semantics
     * @param dataResponse
     * @return
     */
    public static String recommenda(Map map, String semantics, DataResponse dataResponse) {
        //获取返回信息
        String text = "";
        String title="";
        List<String> titles = null;

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
        //DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();

        //判断是否有作品
        if(works.size()<=0){
            String recommendName="暂无作品！";
            String recommendText="";
            return TypeRecommendation.packageResult(recommendName,recommendText);
        }

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
                    titles.add(freeName);
                    for(int j=0;i<titles.size();j++){
                        if(j==2||j==titles.size()){
                            title+="《"+freeName+"》,你可以说：打开某作品";
                        }
                        title+="《"+titles.get(i)+"》,";
                    }
                    //title+="《"+freeName+"》,你可以说：打开某作品";
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
                titles.add(freeName);
                //title+="《"+freeName+"》,";
                game.put(freeName,number);
                if(list2.size()>i){
                    //获取收费游戏名
                    String freeGameName2 = list2.get(i).getKey();
                    //获取收费游戏名编号
                    String number2 = paidGameNumber.get(freeGameName2);
                    text+=number2+"+"+freeGameName2+",";
                    titles.add(freeGameName2);
                    //title+="《"+freeGameName2+"》,";
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
                    titles.add(freeName);
                    //title+="《"+freeName+"》,";
                    game.put(freeName,number);
                }
                if(i==list2.size()-1){
                    //获取收费游戏名
                    String freeGameName2 = list.get(i).getKey();
                    //获取收费游戏名编号
                    String number2 = paidGameNumber.get(freeGameName2);
                    text+=number2+"+"+freeGameName2;
                    titles.add(freeGameName2);
                    for(int j=0;i<titles.size();j++){
                        if(j==2||j==titles.size()){
                            title+="《"+freeGameName2+"》,你可以说：打开某作品";
                        }
                        title+="《"+titles.get(i)+"》,";
                    }
                    //title+="《"+freeGameName2+"》,你可以说：打开某作品";
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
                titles.add(freeGameName2);
                //title+="《"+freeGameName2+"》,";
                game.put(freeGameName2,number2);
            }
        }

        return null;
    }
}
