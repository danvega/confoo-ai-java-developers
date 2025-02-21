package dev.danvega.confoo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Bean
	CommandLineRunner commandLineRunner(ChatClient.Builder builder) {
		return args -> {
			var client = builder.build();

			String content = client.prompt()
					.user("Tell me an Interesting fact about Spring AI")
					.call()
					.content();

			System.out.println(content);
		};
	}

}



