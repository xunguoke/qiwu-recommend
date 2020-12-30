package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.ReturnedMessages;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 该类用于提取作品
 * @author hjd
 */
@Slf4j
public class WorkExtractionUtils {
    /**
     * 将作品按照分数排序，且免费收费作品交替出现
     * @param dataResponse 所有作品的对象
     * @return
     */
    public static ReturnedMessages fractionalCharge(DataResponse dataResponse) {
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();

        //获取返回信息
        String text = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        String listWorks= "";
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
        //获取所有作品集合
        List<WorksPojo> works = dataResponse.getWorks();

        for (WorksPojo work : works) {
            //获取是否收费信息
            List<String> synopsis = work.getLabels();
            for (String s : synopsis) {
                if (s.equals("New") || s.equals("免费")) {
                    //获取游戏名
                    String gameName = work.getName();
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
                    String gameName = work.getName();
                    //获取游戏分数
                    Double fraction = work.getScore();
                    //获取游戏编号
                    String botAccount = work.getBotAccount();
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
        Collections.sort(list2, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        log.warn("list:{}", list);

        //判断集合长度
        if (list.size() > list2.size()) {
            for (int i = 0; i < list.size(); i++) {

                //判断是否最后
                if (i == list.size() - 1) {
                    //获取免费游戏名
                    String freeName = list.get(i).getKey();
                    //获取免费游戏名编号
                    String number = freeGameNumber.get(freeName);
                    text += number + "+" + freeName;
                    listWorks="☛推荐" + text + "☚";
                    titleList.add(freeName);

                    if (titleList.size() >= 3) {
                        for (int j = 0; j < 3; j++) {

                            if (j == 2) {
                                titleText += "《" + titleList.get(j) + "》，";
                            }else {
                                titleText += "《" + titleList.get(j) + "》、";
                            }
                        }
                    } else {
                        for (int y = 0; y < titleList.size(); y++) {
                            if (y == titleList.size() - 1) {
                                titleText += "《" + titleList.get(y) + "》，";
                            }else {
                                titleText += "《" + titleList.get(y) + "》、";
                            }
                        }
                    }

                    //封装对象后返回
                    messages.setWorksList(listWorks);
                    messages.setWorkInformation(titleText);
                    messages.setWorksName(titleList);
                    return messages;
                }

                //获取免费游戏名
                String freeName = list.get(i).getKey();
                //获取免费游戏名编号
                String number = freeGameNumber.get(freeName);
                text += number + "+" + freeName + ",";
                titleList.add(freeName);

                if (list2.size() > i) {
                    //获取收费游戏名
                    String freeGameName2 = list2.get(i).getKey();
                    //获取收费游戏名编号
                    String number2 = paidGameNumber.get(freeGameName2);
                    text += number2 + "+" + freeGameName2 + ",";
                    titleList.add(freeGameName2);

                }
            }
        } else {
            for (int i = 0; i < list2.size(); i++) {


                if (list.size() >= i) {
                    //获取免费游戏名
                    String freeName = list.get(i).getKey();
                    //获取免费游戏名编号
                    String number = freeGameNumber.get(freeName);
                    text += number + "+" + freeName + ",";
                    titleList.add(freeName);
                    game.put(freeName, number);
                }
                if (i == list2.size() - 1) {
                    //获取收费游戏名
                    String freeGameName2 = list.get(i).getKey();
                    //获取收费游戏名编号
                    String number2 = paidGameNumber.get(freeGameName2);
                    text += number2 + "+" + freeGameName2;

                    titleList.add(freeGameName2);

                    if (titleList.size() >= 3) {
                        for (int j = 0; j < 3; j++) {

                            if (j == 2) {
                                titleText += "《" + titleList.get(j) + "》，";
                            }else {
                                titleText += "《" + titleList.get(j) + "》、";
                            }
                        }
                    } else {
                        for (int y = 0; y < titleList.size(); y++) {
                            if (y == titleList.size() - 1) {
                                titleText += "《" + titleList.get(y) + "》，";
                            }else {
                                titleText += "《" + titleList.get(y) + "》、";
                            }
                        }
                    }
                    //封装对象后返回
                    messages.setWorksList(listWorks);
                    messages.setWorkInformation(titleText);
                    messages.setWorksName(titleList);
                    return messages;
                }
                //获取收费游戏名
                String freeGameName2 = list2.get(i).getKey();
                //获取收费游戏名编号
                String number2 = paidGameNumber.get(freeGameName2);
                text += number2 + "+" + freeGameName2 + ",";

                titleList.add(freeGameName2);
                game.put(freeGameName2, number2);
            }
        }
        return null;
    }
}
