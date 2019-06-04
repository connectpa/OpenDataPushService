package it.connectpa.odatapushservice;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import it.connectpa.odatapushservice.client.ApiClient;
import it.connectpa.odatapushservice.client.api.PushServiceApi;
import it.connectpa.odatapushservice.client.model.Column;
import it.connectpa.odatapushservice.client.model.CreatedColumn;
import it.connectpa.odatapushservice.client.model.CreatedMetadata;
import it.connectpa.odatapushservice.client.model.InstertedData;
import it.connectpa.odatapushservice.client.model.Metadata;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
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
    public void insertData() throws IOException {
        ByteSource payload = new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                return getClass().getResourceAsStream("/test.csv");
            }
        };

        PushServiceApi api = new PushServiceApi(new ApiClient().setBasePath("http://localhost:" + port));
        InstertedData response = api.insertData("abcd-efgh", payload.asCharSource(Charsets.UTF_8).read());
        assertEquals(Integer.valueOf(10), response.getRecordsNumber());
        assertEquals("abcd-efgh", response.getId());
    }

    @Test
    public void issue5() throws IOException {
        ByteSource payload = new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                return getClass().getResourceAsStream("/test.csv");
            }
        };

        HttpPut insertData = new HttpPut("http://localhost:" + port + "/resource/abcd-efgh.json");
        insertData.setHeader(HttpHeaders.CONTENT_TYPE, "text/csv");
        insertData.setEntity(new StringEntity(
                payload.asCharSource(Charsets.UTF_8).read(), ContentType.create("text/csv", Consts.UTF_8)));
        CloseableHttpResponse httpResponse = HttpClients.createDefault().execute(insertData);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());

        InstertedData response = new ObjectMapper().
                readValue(httpResponse.getEntity().getContent(), InstertedData.class);
        assertEquals(Integer.valueOf(10), response.getRecordsNumber());
        assertEquals("abcd-efgh", response.getId());
    }
}
