package it.connectpa.odatapushservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@SpringBootApplication
public class PushServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PushServiceApplication.class, args);
    }

    @Bean
    public Docket swagger2() {
        return new Docket(DocumentationType.SWAGGER_2).
                select().
                apis(RequestHandlerSelectors.basePackage("it.connectpa.odatapushservice.rest")).
                paths(PathSelectors.any()).
                build().
                useDefaultResponseMessages(false).
                apiInfo(new ApiInfoBuilder().
                        title("OpenData Push Service Documentation").
                        description("OpenData Push Service Documentation").
                        version("1.0.0").
                        build());
    }
}
