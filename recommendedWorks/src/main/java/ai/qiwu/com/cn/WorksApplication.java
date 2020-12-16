package ai.qiwu.com.cn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 推荐作品启动类
 * @author hjd
 */
@SpringBootApplication
@EnableScheduling
public class WorksApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorksApplication.class,args);
    }
}
