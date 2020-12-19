import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class text {
    public static void main(String[] args) {
/*

        Order o1 = new Order("2018-01-01",1234567890,3.14);
        Order o2 = new Order("2018-01-02",1234567891,3.14);
        Order o3 = new Order("2018-01-01",1234567892,3.15);
        Order o4 = new Order("2018-01-02",1234567893,3.16);
        Order o5 = new Order("2018-01-01",1234567893,3);

        List<Order> list = new ArrayList<Order>();
        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);

        Collections.sort(list, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                String s1 = o1.getDateStr();
                String s2 = o2.getDateStr();

                int temp = s1.compareTo(s2);

                if(temp != 0){
                    return  temp;
                }

                double m1 = o1.getMoney();
                double m2 = o2.getMoney();

                BigDecimal data1 = new BigDecimal(m1);
                BigDecimal data2 = new BigDecimal(m2);

                return data2.compareTo(data1);
            }
        });

        System.out.println(list);
    }

    // 订单类
    static class Order{
        // 订单日期
        private String dateStr;
        // 订单号
        private long order;
        // 订单金额
        private double money;

        public String getDateStr() {
            return dateStr;
        }

        public long getOrder() {
            return order;
        }

        public double getMoney() {
            return money;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "dateStr='" + dateStr + '\'' +
                    ", order=" + order +
                    ", money=" + money +
                    '}';
        }
        public Order(String dateStr, long order, double money) {
            this.dateStr = dateStr;
            this.order = order;
            this.money = money;
        }
    }
*/


        List<String> listA_01 = new ArrayList<String>() {{
            add("A");
            add("B");
        }};
        List<String> listB_01 = new ArrayList<String>() {{
            add("B");
            add("C");
        }};
        boolean b = listB_01.retainAll(listA_01);
        System.out.println(listB_01); // 结果:[B]
        System.out.println(b); // 结果:[B, C]
/*        String aa = "2020-12-11T12:26:57.000+0000";
        System.out.println(dealDateFormat(aa));*/


       /* Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("d", 2);
        map.put("c", 1);
        map.put("b", 1);
        map.put("a", 3);
        List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
//排序前
        for (int i = 0; i < infoIds.size(); i++) {
            String id = infoIds.get(i).toString();
            System.out.println(id);
        }
//根据value排序
        Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });
//排序后
        infoIds.get(10).getKey();
        for (int i = 0; i < infoIds.size(); i++) {
            String key = infoIds.get(i).getKey();
            System.out.println(key);
        }*/



/*    public static String dealDateFormat(String oldDate) {
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
    }*/
    }
}