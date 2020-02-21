package cn.hust.spike;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.hust.spike.dao")
public class SpikeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpikeApplication.class, args);
	}

}
