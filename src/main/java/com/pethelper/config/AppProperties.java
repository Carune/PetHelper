package com.pethelper.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
@Component
public class AppProperties {
    private final Frontend frontend = new Frontend();
    private final Api api = new Api();
    private final Cors cors = new Cors();

    @Getter
    @Setter
    public static class Frontend {
        private String url;
    }

    @Getter
    @Setter
    public static class Api {
        private String url;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
    }
} 