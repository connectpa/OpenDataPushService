package it.connectpa.odatapushservice;

import static org.junit.Assert.assertEquals;

import it.connectpa.odatapushservice.client.ApiClient;
import it.connectpa.odatapushservice.client.api.PushServiceApi;
import it.connectpa.odatapushservice.client.model.InlineObject;
import it.connectpa.odatapushservice.client.model.InlineObject1;
import it.connectpa.odatapushservice.client.model.InlineResponse201;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = { PushServiceTestConfiguration.class })
public class PushServiceTests {

    @LocalServerPort
    private int port;

    @Test
    public void addType() {
        PushServiceApi api = new PushServiceApi(new ApiClient().setBasePath("http://localhost:" + port));
        InlineObject1 payload = new InlineObject1();
        api.addType("abcd-efgh", payload);
    }

    @Test
    public void createMetadataTest() {
        PushServiceApi api = new PushServiceApi(new ApiClient().setBasePath("http://localhost:" + port));
        InlineObject payload = new InlineObject();
        InlineResponse201 response = api.createMetadata(payload);
        assertEquals(response.getId(), "abcd-efgh");
    }
}
