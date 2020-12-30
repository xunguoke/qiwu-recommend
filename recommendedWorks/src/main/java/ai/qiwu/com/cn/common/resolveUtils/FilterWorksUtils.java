package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.pojo.connectorPojo.TemporaryWorks;
import ai.qiwu.com.cn.pojo.connectorPojo.WorkInformation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 该类主要用于筛选作品，类型
 * @author hjd
 */
public class FilterWorksUtils {
    /**
     * 筛选不包含禁用标签且包含意图的作品
     * @param dataResponse 封装的作品信息
     * @param semantics 用户意图
     * @param strings 禁用标签
     * @return
     */
    public static DataResponse nonProhibitedWorks(DataResponse dataResponse, String semantics, List<String> strings) {
        if(strings!=null&&strings.contains(semantics)){
            return null;
        }else {
            return dataResponse;
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
     * @param dataResponse 封装的作品信息
     * @param semantics 用户意图
     * @return
     */
    public static List<WorksPojo> authorWorks(DataResponse dataResponse, String semantics) {
        List<WorksPojo> worksPojos=new ArrayList<>();
        for (WorksPojo work : dataResponse.getWorks()) {
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
            if (work.getName().equals(semantics)){
                List<String> labels = work.getLabels();
                labels.retainAll(strings);
                type+=work.getName()+"的作品类型是：";
                for (int i = 0; i < labels.size(); i++) {
                    if (!labels.get(i).equals("VIP")&&!labels.get(i).equals("New")){
                        if (i==labels.size()-1){
                            type+=labels.get(i);
                        }
                        type+=labels.get(i)+"、";
                    }
                }
                return type;
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
        HashSet hashSet = new HashSet(labels);
        labels.clear();
        labels.addAll(hashSet);
        labels.removeAll(jy);
        labels.removeAll(strings);
        return labels;
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
}
