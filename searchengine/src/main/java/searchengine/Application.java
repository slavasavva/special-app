package searchengine;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import searchengine.model.Site;

@SpringBootApplication
public class Application {
    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
