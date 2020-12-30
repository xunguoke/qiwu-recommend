package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.pojo.connectorPojo.TemporaryWorks;
import ai.qiwu.com.cn.pojo.connectorPojo.WorkInformation;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类主要用于筛选作品
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
}
