package it.connectpa.odataservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataPropertyRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8080")
public class ODataServiceTest {

    private ODataClient client;

    private Edm edm;

    private final static String NAME_SPACE = "OData.Service";

    private final String SERVICE_URL = "http://localhost:8080/odata";

    private final static String PRODUCT_ENTITY = "Product";

    private final static String PRODUCT_ENTITY_SET = "Products";

    private final static String ID_PROPERTY = "ID";

    private final static String NAME_PROPERTY = "NAME";

    private final static String DESCRIPTION_PROPERTY = "DESCRIPTION";

    @Before
    public void init() throws IOException {
        client = ODataClientFactory.getClient();
        edm = readEdm();
    }

    private Edm readEdm() throws IOException {
        EdmMetadataRequest request = client.getRetrieveRequestFactory().getMetadataRequest(SERVICE_URL);
        ODataRetrieveResponse<Edm> response = request.execute();
        return response.getBody();
    }

    @Test
    public void readEntityTypes() {
        List<FullQualifiedName> entityTypes = new ArrayList<>();
        for (EdmSchema schema : edm.getSchemas()) {
            for (EdmEntityType entityType : schema.getEntityTypes()) {
                entityTypes.add(entityType.getFullQualifiedName());
            }
        }
        assertTrue(entityTypes.contains(new FullQualifiedName(NAME_SPACE, PRODUCT_ENTITY)));
    }

    @Test
    public void readProperities() {
        EdmEntityType etype = edm.getEntityType(new FullQualifiedName(NAME_SPACE, PRODUCT_ENTITY));
        List<String> properities = new ArrayList<>();
        for (String propertyName : etype.getPropertyNames()) {
            properities.add(propertyName);

        }
        assertTrue(properities.contains(NAME_PROPERTY));
        EdmProperty property = etype.getStructuralProperty(NAME_PROPERTY);
        assertEquals("String", property.getType().getName());
    }

    @Test
    public void readEntities() {
        ClientEntitySet entities = readEntities(PRODUCT_ENTITY_SET);

        List<String> entitiesName = new ArrayList<>();
        entities.getEntities().forEach(entity -> {
            entitiesName.add(entity.getProperty(NAME_PROPERTY).getValue().toString());
        });

        assertTrue(entitiesName.contains("Test1"));
    }

    @Test
    public void readEntityWithKey() {
        ClientEntity entity = readEntityWithKey(PRODUCT_ENTITY_SET, 1);
        assertEquals("Test1", entity.getProperty(NAME_PROPERTY).getValue().toString());
    }

    @Test
    public void readSingleProperty() {
        ClientProperty readSingleProperty = readSingleProperty(PRODUCT_ENTITY_SET, 2, DESCRIPTION_PROPERTY);
        assertEquals("Description is for test2",
                readSingleProperty.getValue().toString());
    }

    @Test
    public void readEntitiesWithFilter() {
        ClientEntitySet entities = readEntitiesWithFilter(PRODUCT_ENTITY_SET, "ID gt 2");
        entities.getEntities().forEach(entity
                -> assertTrue(Integer.valueOf(entity.getProperty(ID_PROPERTY).getValue().toString()) > 2));
    }

    @Test
    public void readEntitiesWithTop() {
        ClientEntitySet entities = readEntitiesWithTop(PRODUCT_ENTITY_SET, 2);
        entities.getEntities().forEach(entity
                -> assertTrue(Integer.valueOf(entity.getProperty(ID_PROPERTY).getValue().toString()) < 3));
    }

    @Test
    public void readEntitiesWithSkip() {
        ClientEntitySet entities = readEntitiesWithSkip(PRODUCT_ENTITY_SET, 2);
        entities.getEntities().forEach(entity
                -> assertTrue(Integer.valueOf(entity.getProperty(ID_PROPERTY).getValue().toString()) > 2));
    }

    @Test
    public void readEntitiesWithOrder() {
        ClientEntitySet entities = readEntitiesWithOrder(PRODUCT_ENTITY_SET, "ID desc");
        assertEquals("3", entities.getEntities().get(0).getProperty(ID_PROPERTY).getValue().toString());
    }

    @Test
    public void readEntitiesWithCount() {
        ClientEntitySet entities = readEntitiesWithCount(PRODUCT_ENTITY_SET);
        assertEquals(Integer.valueOf(3), entities.getCount());
    }

    @Test
    public void readEntitiesWithSelectt() {
        ClientEntitySet entities = readEntitiesWithSelect(PRODUCT_ENTITY_SET, ID_PROPERTY);
        entities.getEntities().forEach(entity -> {
            assertNotNull(entity.getProperty(ID_PROPERTY));
            assertNull(entity.getProperty(NAME_PROPERTY));
        });
    }

    @Test
    public void readEntityWithSelect() {
        ClientEntity entity = readEntityWithSelect(PRODUCT_ENTITY_SET, 1, ID_PROPERTY);
        assertNotNull(entity.getProperty(ID_PROPERTY));
        assertNull(entity.getProperty(NAME_PROPERTY));
    }

    private ClientEntitySet readEntities(final String entitySetName) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName).build();
        return readEntities(absoluteUri);
    }

    private ClientEntitySet readEntities(final URI absoluteUri) {
        ODataEntitySetRequest<ClientEntitySet> request =
                client.getRetrieveRequestFactory().getEntitySetRequest(absoluteUri);
        ODataRetrieveResponse<ClientEntitySet> response = request.execute();

        return response.getBody();
    }

    private ClientEntity readEntityWithKey(final String entitySetName, final Object keyValue) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName)
                .appendKeySegment(keyValue).build();
        return readEntity(absoluteUri);
    }

    private ClientEntity readEntity(final URI absoluteUri) {
        ODataEntityRequest<ClientEntity> request = client.getRetrieveRequestFactory().getEntityRequest(absoluteUri);
        ODataRetrieveResponse<ClientEntity> response = request.execute();

        return response.getStatusCode() == 200 ? response.getBody() : null;
    }

    private ClientProperty readSingleProperty(final String entitySetName, final Object keyValue,
            final String propertyName) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName)
                .appendKeySegment(keyValue).appendPropertySegment(propertyName).build();
        return readSingleProperty(absoluteUri);
    }

    private ClientProperty readSingleProperty(final URI absoluteUri) {
        ODataPropertyRequest<ClientProperty> request =
                client.getRetrieveRequestFactory().getPropertyRequest(absoluteUri);
        ODataRetrieveResponse<ClientProperty> response = request.execute();

        return response.getBody();
    }

    private ClientEntitySet readEntitiesWithFilter(final String entitySetName, final String filter) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName).filter(filter).
                build();
        return readEntities(absoluteUri);
    }

    private ClientEntitySet readEntitiesWithTop(final String entitySetName, final int top) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName).top(top).
                build();
        return readEntities(absoluteUri);
    }

    private ClientEntitySet readEntitiesWithSkip(final String entitySetName, final int skip) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName).skip(skip).
                build();
        return readEntities(absoluteUri);
    }

    private ClientEntitySet readEntitiesWithOrder(final String entitySetName, final String orderBy) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName).orderBy(orderBy).
                build();
        return readEntities(absoluteUri);
    }

    private ClientEntitySet readEntitiesWithCount(final String entitySetName) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName).count(true).
                build();

        return readEntities(absoluteUri);
    }

    private ClientEntitySet readEntitiesWithSelect(final String entitySetName, final String select) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName).select(select).
                build();

        return readEntities(absoluteUri);
    }

    private ClientEntity readEntityWithSelect(final String entitySetName, final Object keyValue,
            final String select) {
        URI absoluteUri = client.newURIBuilder(SERVICE_URL).appendEntitySetSegment(entitySetName)
                .appendKeySegment(keyValue).select(select).build();
        return readEntity(absoluteUri);
    }
}
