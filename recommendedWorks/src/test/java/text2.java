import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

public class text2 {
    public static void main(String[] args) {
/*        // 交集
        List<String> listA_01 = new ArrayList<String>(){{
            add("A");
            add("B");
        }};
        List<String> listB_01 = new ArrayList<String>(){{
            add("B");
            add("C");
        }};
        listA_01.retainAll(listB_01);
        System.out.println(listA_01); // 结果:[B]
        System.out.println(listB_01); // 结果:[B, C]*/

// 差集
        List<String> listA_02 = new ArrayList<String>(){{
            add("A");
            add("B");
        }};
        List<String> listB_02 = new ArrayList<String>(){{
            add("B");
            add("C");
        }};
        listA_02.removeAll(listB_02);
        System.out.println(listA_02); // 结果:[A]
        System.out.println(listB_02); // 结果:[B, C]

/*// 并集
        List<String> listA_03 = new ArrayList<String>(){{
            add("A");
            add("B");
        }};
        List<String> listB_03 = new ArrayList<String>(){{
            add("B");
            add("C");
        }};
        listA_03.removeAll(listB_03);
        listA_03.addAll(listB_03);
        System.out.println(listA_03); // 结果:[A, B, C]
        System.out.println(listB_03); // 结果:[B, C]*/
/*        String str = " 111,222，333   444  555";
        //方法1：str.trim()
        System.out.println("1--->"+str.trim());
        //方法2：str.repalce(" ","")
        str.replace(" ", "");
        System.out.println(str);*/

/*        String str   = "111,222，333 444  555";

        String regex = ",|，|\\s+";

        String strAry[] = str.split(regex);

        for (int i = 0; i < strAry.length; i++) {

            System.out.println("i="+i+" Val="+strAry[i]);

        }*/
        //RedisTemplate redisTemplate;
    }
}
