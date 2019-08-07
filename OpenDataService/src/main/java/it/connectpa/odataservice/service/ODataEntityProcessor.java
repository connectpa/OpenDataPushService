package it.connectpa.odataservice.service;

import it.connectpa.odataservice.data.Storage;
import java.io.InputStream;
import java.util.List;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ODataEntityProcessor implements EntityProcessor {

    private OData odata;

    private ServiceMetadata serviceMetadata;

    @Autowired
    private Storage storage;

    @Override
    public void readEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
            final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        Entity entity = storage.readEntityData(edmEntitySet, keyPredicates.get(0));

        EdmEntityType entityType = edmEntitySet.getEntityType();

        ContextURL contextUrl = ContextURL.
                with().
                entitySet(edmEntitySet).
                build();
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entity, options);
        InputStream entityStream = serializerResult.getContent();

        response.setContent(entityStream);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    @Override
    public void createEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
            final ContentType requestFormat, final ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
    }

    @Override
    public void updateEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
            final ContentType requestFormat, final ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
    }

    @Override
    public void deleteEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {
    }

    @Override
    public void init(final OData odata, final ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

}
