package net.tirasa;

import static org.junit.Assert.assertEquals;

import io.swagger.client.ApiClient;
import io.swagger.client.api.PushServiceApi;
import io.swagger.client.model.Body;
import io.swagger.client.model.Body1;
import io.swagger.client.model.InlineResponse201;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PushServiceTests {

    @LocalServerPort
    private int port;

    @Test
    public void addType() {
        PushServiceApi api = new PushServiceApi(new ApiClient().setBasePath("http://localhost:" + port));
        Body1 payload = new Body1();
        api.addType(payload, "abcd-efgh");
    }

    @Test
    public void createMetadataTest() {
        PushServiceApi api = new PushServiceApi(new ApiClient().setBasePath("http://localhost:" + port));
        Body payload = new Body();
        InlineResponse201 response = api.createMetadata(payload);
        assertEquals("abcd-efgh", response.getId());
    }

}
