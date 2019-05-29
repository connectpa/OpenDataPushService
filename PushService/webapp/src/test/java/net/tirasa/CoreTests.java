package net.tirasa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CoreTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Value("${spring.application.name}")
    private String springAppName;

    @Test
    public void env() {
        assertEquals("Push Service", springAppName);
    }

    @Test
    public void healthcheck() throws IOException {
        ResponseEntity<String> entity = testRestTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        JsonNode health = MAPPER.readTree(entity.getBody());
        assertTrue(health.has("status"));
        assertEquals("UP", health.get("status").textValue());
    }
}
