package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.ReturnedMessages;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 该类用于作品按照某种方式排序
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
            }
        }
        return null;
    }

    /**
     * 将作品按照分数排序
     * @param dataResponses 作品对象
     * @return
     */
    public static ReturnedMessages scoreSort(DataResponse dataResponses) {
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();
        //获取返回信息
        String text = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        String listWorks= "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, Double> gameRating = new HashMap<>();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName = work.getName();
            log.warn("gameName:{}", gameName);
            //获取游戏分数
            Double fraction = work.getScore();
            log.warn("fraction:{}", fraction);
            //获取游戏编号
            String botAccount = work.getBotAccount();
            log.warn("botAccount:{}", botAccount);
            //存入游戏编号集合
            gameNumber.put(gameName, botAccount);
            //存入游戏评分集合
            gameRating.put(gameName, fraction);
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
        for (int i = 0; i < list.size(); i++) {
            //判断是否最后
            if (i == list.size() - 1) {
                //获取游戏名
                String freeName = list.get(i).getKey();
                //获取游戏名编号
                String number = gameNumber.get(freeName);
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
            String number = gameNumber.get(freeName);
            text += number + "+" + freeName + ",";
            titleList.add(freeName);
        }
        return null;
    }

    /**
     * 将作品按照时间排序
     * @param dataResponse 作品对象
     * @return
     */
    public static ReturnedMessages timeOrder(DataResponse dataResponse) {
        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();
        //获取返回信息
        String text = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        String listWorks= "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏上线时间
        HashMap<String, String> gameLaunchTime = new HashMap<>();
        //循环所有作品，
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName = work.getName();
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
        for (int i = 0; i < list.size(); i++) {
            //判断是否最后
            if (i == list.size() - 1) {
                //获取游戏名
                String freeName = list.get(i).getKey();
                //获取游戏名编号
                String number = gameNumber.get(freeName);
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
            String number = gameNumber.get(freeName);
            text += number + "+" + freeName + ",";
            titleList.add(freeName);
        }
        return null;
    }
}
