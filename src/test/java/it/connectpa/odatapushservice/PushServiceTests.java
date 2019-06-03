package it.connectpa.odatapushservice;

import static org.junit.Assert.assertEquals;

import it.connectpa.odatapushservice.client.ApiClient;
import it.connectpa.odatapushservice.client.api.PushServiceApi;
import it.connectpa.odatapushservice.client.model.Column;
import it.connectpa.odatapushservice.client.model.CreatedColumn;
import it.connectpa.odatapushservice.client.model.CreatedMetadata;
import it.connectpa.odatapushservice.client.model.InstertedData;
import it.connectpa.odatapushservice.client.model.Metadata;
import java.io.File;
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
    public void createMetadataTest() {
        PushServiceApi api = new PushServiceApi(new ApiClient().setBasePath("http://localhost:" + port));
        Metadata payload = new Metadata();
        payload.setName("TEST");
        payload.setDescription("This metadata for test");
        CreatedMetadata response = api.createMetadata(payload);
        assertEquals("TEST", response.getName());
    }

    @Test
    public void addColumn() {
        PushServiceApi api = new PushServiceApi(new ApiClient().setBasePath("http://localhost:" + port));
        Column payload = new Column();
        payload.setName("Test");
        payload.setDataTypeName("number");
        CreatedColumn response = api.addColumn("abcd-efgh", payload);
        assertEquals("abcd-efgh", response.getId());
        assertEquals("number", response.getDataTypeName());
    }

    @Test
    public void insertData() {
        PushServiceApi api = new PushServiceApi(new ApiClient().setBasePath("http://localhost:" + port));
        File payload = new File("src/main/resources/test.csv");
        InstertedData response = api.insertData("abcd-efgh", payload);
        assertEquals(Integer.valueOf(10), response.getRecordsNumber());
        assertEquals("abcd-efgh", response.getId());
    }

}
