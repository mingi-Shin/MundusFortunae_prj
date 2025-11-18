package com.mingisoft.mf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication // = @Configuration + @EnableAutoConfiguration + @ComponentScan -->@Service, @Repository, @Component 자동 스캔
@ServletComponentScan // @WebFilter, @WebServlet, @WebListener 인식
public class MundusFortunaeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MundusFortunaeApplication.class, args);
	}

}
