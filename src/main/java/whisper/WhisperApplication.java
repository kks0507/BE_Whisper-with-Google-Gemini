package whisper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WhisperApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhisperApplication.class, args);
	}
	
	@Bean // RestTemplate 빈 등록
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
