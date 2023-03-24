package searchengine;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SearchEngineApplication {
    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(SearchEngineApplication.class, args);
    }
}
