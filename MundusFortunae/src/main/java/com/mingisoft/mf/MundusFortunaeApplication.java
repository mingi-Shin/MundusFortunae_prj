package com.mingisoft.mf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication // = @Configuration + @EnableAutoConfiguration + @ComponentScan -->@Service, @Repository, @Component 자동 스캔
@ServletComponentScan // @WebFilter, @WebServlet, @WebListener 인식
@EnableScheduling //@Scheduled 달린 메서드들이 자동으로 돌기 시작
public class MundusFortunaeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MundusFortunaeApplication.class, args);
	}

}
