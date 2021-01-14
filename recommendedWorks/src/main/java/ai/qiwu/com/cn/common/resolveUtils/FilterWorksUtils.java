package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.PayControl;
import ai.qiwu.com.cn.pojo.SeriesPay;
import ai.qiwu.com.cn.pojo.UserHistory;
import ai.qiwu.com.cn.pojo.connectorPojo.PublicData;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.ReturnedMessages;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.pojo.connectorPojo.TemporaryWorks;
import ai.qiwu.com.cn.pojo.connectorPojo.WorkInformation;
import ai.qiwu.com.cn.service.handleService.WatchService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 该类主要用于筛选作品，类型
 * @author hjd
 */
@Slf4j
public class FilterWorksUtils {
    /**
     * 筛选不包含禁用标签且包含意图的作品
     * @param works 所有作品
     * @param semantics 用户意图
     * @param strings 禁用标签
     * @return
     */
    public static List<WorksPojo> nonProhibitedWorks(List<WorksPojo> works, String semantics, List<String> strings) {
        List<WorksPojo> worksPojos=new ArrayList<>();
        if(strings==null){
            for (WorksPojo work : works) {
                List<String> labels = work.getLabels();
                if(labels.contains(semantics)){
                    worksPojos.add(work);
                }
            }
            return worksPojos;
        }else {
            if(strings.contains(semantics)){
                return null;
            }else{
                for (WorksPojo work : works) {
                    List<String> labels = work.getLabels();
                    if(labels.contains(semantics)){
                        worksPojos.add(work);
                    }
                }
                return worksPojos;
            }
        }
    }

    /**
     * 筛选作品标签有相同的作品
     * @param dataResponse 封装的作品信息
     * @param semantics 用户意图
     * @param strings 禁用标签
     * @return
     */
    public static TemporaryWorks scoreLabel(DataResponse dataResponse, String semantics, List<String> strings) {
        List<WorksPojo> worksList=new ArrayList<>();
        //用于存储临时数据
        List<WorkInformation> listWork = new ArrayList<WorkInformation>();
        //用于存储用户说的作品类型列表
        List<String> typeList = new ArrayList<>();
        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();

        //获取作品类型列表
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName1 = work.getName();
            //判断游戏名是否和语义相同
            if (gameName1.equals(semantics)) {
                //获取类型列表
                typeList = work.getLabels();
            }
        }
        //获取相似作品
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName = work.getName();
            if (!gameName.equals(semantics)) {
                //获取游戏分数
                Double fraction = work.getScore();
                //获取游戏编号
                String botAccount = work.getBotAccount();
                //获取类型列表
                List<String> labels = work.getLabels();
                labels.retainAll(typeList);
                //判断有相同标签才存入集合
                int size = labels.size();
                if (size > 0) {
                    //创建对象传入参数
                    WorkInformation information = new WorkInformation(gameName, botAccount, fraction, size);
                    listWork.add(information);
                    worksList.add(work);
                }

            }
        }
        TemporaryWorks temporaryWorks=new TemporaryWorks();
        temporaryWorks.setWorksPojos(worksList);
        temporaryWorks.setWorkInformations(listWork);
        return temporaryWorks;
    }

    /**
     * 筛选指定作者的作品
     * @param works 封装的作品信息
     * @param semantics 用户意图
     * @return
     */
    public static List<WorksPojo> authorWorks(List<WorksPojo> works, String semantics) {
        List<WorksPojo> worksPojos=new ArrayList<>();
        for (WorksPojo work : works) {
            if (work.getAuthorName().equals(semantics)){
                worksPojos.add(work);
            }
        }
        return worksPojos;
    }

    /**
     * 筛选指定作品的类型
     * @param dataResponse 封装的作品信息
     * @param semantics 用户意图
     * @param strings 禁用标签
     * @return
     */
    public static String designatedWorks(DataResponse dataResponse, String semantics, List<String> strings) {

        String type="";
        for (WorksPojo work : dataResponse.getWorks()) {
            if (work.getName().equals(semantics)) {
                List<String> labels = work.getLabels();
                if (labels == null) {
                    return null;
                } else {
                    if(strings==null){
                        type += work.getName() + "的作品类型是：";
                        for (int i = 0; i < labels.size(); i++) {
                            if (i == labels.size() - 1) {
                                if (!labels.get(i).equals("VIP") && !labels.get(i).equals("New")) {
                                    type += labels.get(i);
                                }
                            } else {
                                if (!labels.get(i).equals("VIP") && !labels.get(i).equals("New")) {
                                    type += labels.get(i) + "、";
                                }
                            }
                        }
                        return type;
                    }else {
                        labels.retainAll(strings);
                        type += work.getName() + "的作品类型是：";
                        for (int i = 0; i < labels.size(); i++) {
                            if (i == labels.size() - 1) {
                                if (!labels.get(i).equals("VIP") && !labels.get(i).equals("New")) {
                                    type += labels.get(i);
                                }
                            }
                            if (!labels.get(i).equals("VIP") && !labels.get(i).equals("New")) {
                                type += labels.get(i) + "、";
                            }
                        }
                        return type;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据意图筛选指定人群类型的作品
     * @param dataResponse 封装的作品信息
     * @param semantics 用户意图
     * @return
     */
    public static List<WorksPojo> crowdType(DataResponse dataResponse, String semantics) {
        List<WorksPojo> works = new ArrayList<>();
        for (WorksPojo work : dataResponse.getWorks()) {
            //获取作品类型
            List<String> suitCrowds = work.getSuitCrowds();
            if(suitCrowds.contains(semantics)){
                works.add(work);
            }
        }
        return works;
    }

    /**
     * 根据系列筛选作品
     * @param works
     * @param semantics
     * @return
     */
    public static List<WorksPojo> seriesScreening(List<WorksPojo> works, String semantics) {
        List<WorksPojo> worksa = new ArrayList<>();
        for (WorksPojo work : works) {
            //获取作品系列
            String seriesName = work.getSeriesName();
            if(seriesName.equals(semantics)){
                worksa.add(work);
            }
        }
        return worksa;
    }

    /**
     * 获取所有类型
     * @param works
     * @param strings
     * @return
     */
    public static List<String> typeSelection(List<WorksPojo> works, List<String> strings) {
        //定义一个String类型的变量用于存储筛选的游戏
        List<String> labels = new ArrayList<>();
        List<String> jy = new ArrayList<>();
        jy.add("New");
        jy.add("VIP");

        //循环所有作品获取作品类型
        for (int a = 0; a < works.size(); a++) {
            List<String> multipleLabels = works.get(a).getLabels();
            //循环标签中的类型
            for (String multipleLabel : multipleLabels) {
                labels.add(multipleLabel);
            }
        }
        List<String> label = labels.stream().distinct().collect(Collectors.toList());
        label.removeAll(jy);
        if(strings==null){
            return label;
        }else{
            List<String> stringList=new ArrayList<>(strings);
            label.removeAll(stringList);
            return label;
        }
    }

    /**
     * 获取所有系列
     * @param works
     * @return
     */
    public static List<String> allSeries(List<WorksPojo> works) {
        List<String> seriesName = new ArrayList<>();
        for (WorksPojo work : works) {
            if (work.getSeriesName() != null && !work.getSeriesName().equals("")) {
                seriesName.add(work.getSeriesName());
            }
        }
        HashSet hashSet=new HashSet(seriesName);
        seriesName.clear();
        seriesName.addAll(hashSet);
        return seriesName;
    }

    /**
     * 获取已玩作品的作品名时间集合
     * @param byUidOfDate 指定时间段内用户玩过的作品
     * @return
     */
    public static List<Map.Entry<String, Date>> workTime(List<UserHistory> byUidOfDate) {
        //定义一个map集合用于存储已玩作品名和最后一次玩的时间
        HashMap<String , Date> hashMap = new HashMap<>();
        for (UserHistory userHistory : byUidOfDate) {
            hashMap.put(userHistory.getWorkname(),userHistory.getGmtmodified());
        }
        //将集合按照时间降序排序
        List<Map.Entry<String, Date>> list = new ArrayList<Map.Entry<String, Date>>(hashMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Date>>() {
            @Override
            public int compare(Map.Entry<String, Date> o1, Map.Entry<String, Date> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return list;
    }

    /**
     * 所有作品，作品的交集作品（返回的作品信息已经按照时间排序）
     * @param workTime 用户玩过的作品时间集合（已经降序排序）
     * @param maps 所有作品接口中的作品
     * @param workTime
     * @return
     */
    public static DataResponse workResult(Map maps, List<Map.Entry<String, Date>> workTime) {
        //定义两个List用于存储渠道作品名和接口作品名
        List<String> interfaceWorks=new ArrayList<>();
        List<String> historicalWorks=new ArrayList<>();
        List<WorksPojo> worksList = new ArrayList<>();
        PublicData publicData = new PublicData();
        //todo 2.获取接口中的所有作品
        //获取接口中所有作品
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();
        publicData.setLabels(dataResponse.getLabels());
        //循环遍历接口中的作品名
        for (WorksPojo work : works) {
            interfaceWorks.add(work.getName());
        }
        //todo 3.获取用户玩过的所有作品名
        for (int i = 0; i < workTime.size(); i++) {
            historicalWorks.add(workTime.get(i).getKey());
        }
        historicalWorks.retainAll(historicalWorks);
        //todo 5.封装作品
        //循环遍历接口中的所有作品
        for (WorksPojo work : works) {
            String name = work.getName();
            if(historicalWorks.contains(name)){
                worksList.add(work);
            }
        }
        dataResponse.setWorks(worksList);
        //根据不同意图返回作品
        return dataResponse;
    }

    /**
     * 根据用户id和指定时间段查询玩过的作品
     */
    public static List<UserHistory> findByUidOfDate(String uid, WatchService watchService, String semantics) {
        //获取开始时间
        String startingTime;
        //获取结束时间
        String endTime;
        //调用方法解析语义，获取时间
        List<String> date = JudgmentIntention.getDate(semantics);
        if(date.size()>1){
            //获取开始时间
            startingTime = date.get(0);
            //获取结束时间
            endTime = date.get(1);
        }else{
            //获取开始时间
            startingTime = date.get(0);
            //获取当前时间
            endTime = DateUtil.currentTimes();
        }
        //数据库中查询
        List<UserHistory> userHistory = watchService.findByUidOfDate(uid,startingTime,endTime);
        return userHistory;
    }

    /**
     * 根据类型筛选作品
     * @param works 作品信息
     * @param semantics 筛选条件
     * @return
     */
    public static List<WorksPojo> typeSelection(List<WorksPojo> works, String semantics) {
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //判断作品类型是否是免费或者付费
        if(semantics.equals("New")||semantics.equals("免费")){
            //循环遍历所有作品
            for (WorksPojo work : works) {
                //获取作品类型列表
                List<String> labels = work.getLabels();
                //判断是否包含指定的类型
                if(labels.contains("New")||labels.contains("免费")){
                    //包含
                    worksList.add(work);
                }
            }
            return worksList;
        }else if(semantics.equals("VIP")||semantics.equals("付费")){
            //循环遍历所有作品
            for (WorksPojo work : works) {
                //获取作品类型列表
                List<String> labels = work.getLabels();
                //判断是否包含指定的类型
                if(labels.contains("VIP")||labels.contains("付费")){
                    //包含
                    worksList.add(work);
                }
            }
            return worksList;
        }
        //循环遍历所有作品
        for (WorksPojo work : works) {
            //获取作品类型列表
            List<String> labels = work.getLabels();
            //判断是否包含指定的类型
            if(labels.contains(semantics)){
                //包含
                worksList.add(work);
            }
        }
        return worksList;
    }

    /**
     * 根据分数排序获取作品列表以及返回信息
     * @param works 作品信息
     * @return
     */
    public static ReturnedMessages scoreScreening(List<WorksPojo> works) {
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();
        //最后返回的作品列表
        String listWorks="";
        //定义一个String类型的变量用于存储筛选的游戏
        String listOfWorks = "";
        List<String> titleList = new ArrayList<>();
        String workInformation = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, Double> gameRating = new HashMap<>();
        //循环所有作品，
        for (WorksPojo work : works) {
            //获取作品名
            String gameName = work.getName();
            //获取作品分数
            Double fraction = work.getScore();
            //获取作品编号
            String botAccount = work.getBotAccount();
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
            //获取作品名
            String workName = list.get(i).getKey();
            //获取作品编号
            String botAccount = gameNumber.get(workName);
            //判断是否是最后一个元素
            if(i==list.size()-1){
                //设置返回作品列表
                listOfWorks += botAccount + "+" + workName;
                //设置最后返回的作品列表
                listWorks="☛推荐" + listOfWorks + "☚";
                //将作品名添加到集合中去
                titleList.add(workName);
                //判断作品是否大于三个
                if (titleList.size()>=3){
                    //循环获取作品列表信息
                    for (int j = 0; j < 3; j++) {
                        //判断是否是最后一个元素
                        if(j==2){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }else{
                    //循环获取作品列表信息
                    for (int j = 0; j < titleList.size(); j++) {
                        //判断是否是最后一个元素
                        if(j==titleList.size()-1){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }

            }
            //设置返回作品列表
            listOfWorks += botAccount + "+" + workName + ",";
            //将作品名添加到集合中去
            titleList.add(workName);
        }
        return null;
    }

    /**
     * 筛选不包含渠道禁用标签的作品
     * @param works 所有作品
     * @param strings1 禁用标签
     * @return
     */
    public static List<WorksPojo> filterDisabled(List<WorksPojo> works, List<String> strings1) {
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //循环遍历所有作品
        for (WorksPojo work : works) {
            //获取作品类型
            List<String> asList1 = work.getLabels();
            //取交集判断是否含有相同的标签
            //取差集
            List<String> asList = new ArrayList<>(asList1);
            if(strings1!=null){
                List<String> strings = new ArrayList<>(strings1);
                asList.retainAll(strings);
                if(asList.size()<=0){
                    worksList.add(work);
                }
            }else{
                worksList.add(work);
            }
        }
        return worksList;
    }

    /**
     * 根据时间排序获取作品列表以及返回信息
     * @param works
     * @return
     */
    public static ReturnedMessages timeOrder(List<WorksPojo> works) {
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();
        //最后返回的作品列表
        String listWorks="";
        //最后返回的作品信息
        String returnedMessages="";
        //定义一个String类型的变量用于存储筛选的游戏
        String listOfWorks = "";
        List<String> titleList = new ArrayList<>();
        String workInformation = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, String> gameRating = new HashMap<>();
        //循环所有作品，
        for (WorksPojo work : works) {
            //获取作品名
            String gameName = work.getName();
            //获取作品上线时间
            String fraction = work.getGmtApply();
            //获取作品编号
            String botAccount = work.getBotAccount();
            //存入游戏编号集合
            gameNumber.put(gameName, botAccount);
            //存入游戏时间集合
            gameRating.put(gameName, fraction);
        }
        //将游戏按照时间降序排序
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(gameRating.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        //循环遍历集合，提取游戏名游戏编号
        for (int i = 0; i < list.size(); i++) {
            //获取作品名
            String workName = list.get(i).getKey();
            //获取作品编号
            String botAccount = gameNumber.get(workName);
            //判断是否是最后一个元素
            if(i==list.size()-1){
                //设置返回作品列表
                listOfWorks += botAccount + "+" + workName;
                //设置最后返回的作品列表
                listWorks="☛推荐" + listOfWorks + "☚";
                //将作品名添加到集合中去
                titleList.add(workName);
                //判断作品是否大于三个
                if (titleList.size()>=3){
                    //循环获取作品列表信息
                    for (int j = 0; j < 3; j++) {
                        //判断是否是最后一个元素
                        if(j==2){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }else{
                    //循环获取作品列表信息
                    for (int j = 0; j < titleList.size(); j++) {
                        //判断是否是最后一个元素
                        if(j==titleList.size()-1){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }

            }
            //设置返回作品列表
            listOfWorks += botAccount + "+" + workName + ",";
            //将作品名添加到集合中去
            titleList.add(workName);
        }
        return null;
    }

    /**
     * 获取指定时间范围内的作品
     * @param dataResponses 所有作品
     * @param semantics 时间条件
     * @return
     */
    public static DataResponse latestTime(DataResponse dataResponses, String semantics) {
        List<WorksPojo> worksList = new ArrayList<>();
        //定义一个数组接收作品名
        List<String> interfaceWorks=new ArrayList<>();
        //获取开始时间
        String startingTime;
        //获取结束时间
        String endTime;
        //调用方法解析语义，获取时间
        List<String> date = JudgmentIntention.getDate(semantics);
        if(date.size()>1){
            //获取开始时间
            startingTime = date.get(0);
            //获取结束时间
            endTime = date.get(1);
        }else{
            //获取开始时间
            startingTime = date.get(0);
            //获取当前时间
            endTime = DateUtil.currentTimes();
        }
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        //循环所有作品
        for (WorksPojo work : works) {
            //获取作品上线时间
            String r = work.getGmtApply();
            //将作品上线时间以及时间区间范围转成时间格式后比较大小
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date parse1 = dateFormat.parse(startingTime);
                Date parse2 = dateFormat.parse(endTime);
                Date parse3 = formatter.parse(r);
                //根基条件判断合适的作品
                if(parse2.getTime()>=parse3.getTime()&& parse3.getTime()>=parse1.getTime()){
                    //满足条件获取作品名
                    interfaceWorks.add(work.getName());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //循环遍历所有作品
        for (WorksPojo work : works) {
            String name = work.getName();
            for (String channel : interfaceWorks) {
                if(name.equals(channel)){
                    worksList.add(work);
                }
            }
        }
        dataResponses.setWorks(worksList);
        return dataResponses;
    }

    /**
     * 用户多个意图，结合渠道警用标签筛选
     * @param works
     * @param strings1
     * @param semantics
     * @return
     */
    public static List<WorksPojo> multiConditionScreening(List<WorksPojo> works, List<String> strings1, String semantics) {
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //解析语义
        String[] split = semantics.split("[+]");
        //转list
        List<String> asList1 = Arrays.asList(split);
        //取差集
        List<String> asList = new ArrayList<>(asList1);
        if(strings1!=null){
            List<String> strings = new ArrayList<>(strings1);
            //取差集
            asList.removeAll(strings);
        }
        if(asList.size()<=0){
            return null;
        }else{

                for (WorksPojo work : works) {
                    //获取作品类型
                    List<String> labels = work.getLabels();
                    if (labels.contains(asList.get(0))||labels.contains(asList.get(1))){
                        //包含
                        worksList.add(work);
                    }
                }

            return worksList;
        }
    }

    /**
     * 筛选不包含渠道禁用标签的作品且满足所有意图的作品
     * @param works
     * @param strings1
     * @param semantics
     * @return
     */
    public static List<WorksPojo> allIntentions(List<WorksPojo> works, List<String> strings1, String semantics) {
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //定义一个变量用记录用户意图个数
        int i;
        boolean flag=true;
        //解析语义
        String[] split = semantics.split("[+]");
        //转list
        List<String> asList1 = Arrays.asList(split);
        i=asList1.size();
        List<String> asList = new ArrayList<>(asList1);
        if(strings1!=null){
            List<String> strings = new ArrayList<>(strings1);
            //取差集
            asList.removeAll(strings);
        }
        //判断是否少了意图
        if(asList.size()==i) {
            //循环遍历所有作品
            for (WorksPojo work : works) {
                //获取所有作品类型
                List<String> labels = work.getLabels();
                //判断作品类型是否包含所有意图
                for (String s : asList) {
                    if(!labels.contains(s)){
                        flag=false;
                    }
                }
                //判断作品是否包含所有意图
                if (flag==true){
                    worksList.add(work);
                }
                flag=true;
            }
            return worksList;


        }else{
            return worksList;
        }
    }

    /**
     * 根据历史玩过的时间排序获取作品列表以及返回信息
     * @param works
     * @return
     */
    public static ReturnedMessages historicalTimeSequence(List<WorksPojo> works) {
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();
        //最后返回的作品列表
        String listWorks="";
        //最后返回的作品信息
        String returnedMessages="";
        //定义一个String类型的变量用于存储筛选的游戏
        String listOfWorks = "";
        List<String> titleList = new ArrayList<>();
        String workInformation = "";


        //循环遍历集合，提取游戏名游戏编号
        for (int i = 0; i < works.size(); i++) {
            //获取作品名
            String workName = works.get(i).getName();
            //获取作品编号
            String botAccount = works.get(i).getBotAccount();
            //判断是否是最后一个元素
            if(i==works.size()-1){
                //设置返回作品列表
                listOfWorks += botAccount + "+" + workName;
                //设置最后返回的作品列表
                listWorks="☛推荐" + listOfWorks + "☚";
                //将作品名添加到集合中去
                titleList.add(workName);
                //判断作品是否大于三个
                if (titleList.size()>=3){
                    //循环获取作品列表信息
                    for (int j = 0; j < 3; j++) {
                        //判断是否是最后一个元素
                        if(j==2){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }else{
                    //循环获取作品列表信息
                    for (int j = 0; j < titleList.size(); j++) {
                        //判断是否是最后一个元素
                        if(j==titleList.size()-1){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }
            }
            //设置返回作品列表
            listOfWorks += botAccount + "+" + workName + ",";
            //将作品名添加到集合中去
            titleList.add(workName);
        }
        return null;
    }

    /**
     * 筛选收费或者免费作品
     * @param works
     * @param strings
     * @param semantics
     * @return
     */
    public static List<WorksPojo> collectionAndPaymentScreening(List<WorksPojo> works, List<String> strings, String semantics) {
        //创建一个集合用于存储免费付费作品关键词
        Map map = new HashMap();
        map.put("免费","New");
        map.put("付费","VIP");
        map.put("New","免费");
        map.put("VIP","付费");
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //判断标签是否被禁用
        if(strings.contains(semantics)&&strings.contains(map.get(semantics))){
            return worksList;
        }else if(strings.contains(semantics)){
            for (WorksPojo work : works) {
                //获取作品类型
                List<String> labels = work.getLabels();
                if(labels.contains(map.get(semantics))){
                    worksList.add(work);
                }
            }
            return worksList;
        }else if(strings.contains(map.get(semantics))){
            for (WorksPojo work : works) {
                //获取作品类型
                List<String> labels = work.getLabels();
                if(labels.contains(semantics)){
                    worksList.add(work);
                }
            }
            return worksList;
        }else {
            for (WorksPojo work : works) {
                //获取作品类型
                List<String> labels = work.getLabels();
                if (labels.contains(semantics) || labels.contains(map.get(semantics))) {
                    worksList.add(work);
                }
            }
            return worksList;
        }
    }

    /**
     * 返回指定作品
     * @param works
     * @param semantics
     * @return
     */
    public static WorksPojo designatedWork(List<WorksPojo> works, String semantics) {
        //循环所有作品
        for (WorksPojo work : works) {
            //判断作品名是否相同
            if (work.getName().equals(semantics)){
                //返回指定作品
                return work;
            }
        }
        return null;
    }

    /**
     * 判断作品免费还是付费
     * @param strings
     * @return
     */
    public static List<String> chargeJudgment(List<String> labels, List<String> strings) {
        Map map = new HashMap();
        map.put("免费","New");
        map.put("付费","VIP");
        map.put("New","免费");
        map.put("VIP","付费");
        //定义一个集合用于保存筛选后的类型
        List<String> labelsList=new ArrayList<>();
        //作品免费还是付费
        for (String label : labels) {
            if(label.contains("免费")){
                labelsList.add("免费");
            }else if(label.contains("New")){
                labelsList.add("New");
            }else if(label.contains("付费")){
                labelsList.add("付费");
            }else if(label.contains("VIP")){
                labelsList.add("VIP");
            }
        }
        return labelsList;
    }

    /**
     * 获取数据库已购买作品，已购系列作品的交集
     * @param payControls 已购买作品
     * @param seriesPays 已购系列作品
     * @return
     */
    public static List<Map.Entry<String, Date>> purchasedIntersection(List<PayControl> payControls, List<SeriesPay> seriesPays) {
        //用于接收已购买作品的作品名，购买时间以及已购买系列作品的作品名，购买时间
        HashMap<String , Date> payMap = new HashMap<>();
        //获取已购买作品名
        for (PayControl payControl : payControls) {
            payMap.put(payControl.getWorkname(),payControl.getGmtmodified());
        }
        //获取已购买系列作品名
        for (SeriesPay seriesPay : seriesPays) {
            payMap.put(seriesPay.getWorkname(),seriesPay.getGmtmodified());
        }
        //将集合按照时间降序排序
        List<Map.Entry<String, Date>> list = new ArrayList<Map.Entry<String, Date>>(payMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Date>>() {
            @Override
            public int compare(Map.Entry<String, Date> o1, Map.Entry<String, Date> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return list;
    }

    /**
     * 根据用户id，渠道ID和指定时间段查询购买的作品
     * @return
     */
    public static List<PayControl> purchaseTime(WatchService watchService, String uid, String channelId, String semantics) {
        //获取开始时间
        String startingTime;
        //获取结束时间
        String endTime;
        //调用方法解析语义，获取时间
        List<String> date = JudgmentIntention.getDate(semantics);
        if(date.size()>1){
            //获取开始时间
            startingTime = date.get(0);
            //获取结束时间
            endTime = date.get(1);
        }else{
            //获取开始时间
            startingTime = date.get(0);
            //获取当前时间
            endTime = DateUtil.currentTimes();
        }
        //数据库中查询
        List<PayControl> payControl = watchService.findByUidOfTimeOfChannelId(uid,channelId,startingTime,endTime);
        return payControl;
    }

    /**
     * 根据用户id，渠道ID和指定时间段查询购买的系列作品
     * @return
     */
    public static List<SeriesPay> purchaseSeriesTimePeriod(WatchService watchService, String uid, String channelId, String semantics) {
        //获取开始时间
        String startingTime;
        //获取结束时间
        String endTime;
        //调用方法解析语义，获取时间
        List<String> date = JudgmentIntention.getDate(semantics);
        if(date.size()>1){
            //获取开始时间
            startingTime = date.get(0);
            //获取结束时间
            endTime = date.get(1);
        }else{
            //获取开始时间
            startingTime = date.get(0);
            //获取当前时间
            endTime = DateUtil.currentTimes();
        }
        //数据库中查询
        List<SeriesPay> seriesPay = watchService.seriesPayByUidOfTimeOfChannelId(uid,channelId,startingTime,endTime);
        return seriesPay;
    }
}
