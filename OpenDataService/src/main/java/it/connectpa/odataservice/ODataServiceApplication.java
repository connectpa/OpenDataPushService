package it.connectpa.odataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class ODataServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ODataServiceApplication.class, args);
    }
}
