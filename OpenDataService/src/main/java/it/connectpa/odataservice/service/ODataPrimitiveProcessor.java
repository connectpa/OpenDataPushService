package it.connectpa.odataservice.service;

import it.connectpa.odataservice.data.Storage;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ODataPrimitiveProcessor implements PrimitiveProcessor {

    private OData odata;

    private ServiceMetadata serviceMetadata;

    @Autowired
    private Storage storage;

    @Override
    public void readPrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
            final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriEntityset = (UriResourceEntitySet) resourceParts.get(0);
        EdmEntitySet edmEntitySet = uriEntityset.getEntitySet();
        List<UriParameter> keyPredicates = uriEntityset.getKeyPredicates();

        UriResourceProperty uriProperty = (UriResourceProperty) resourceParts.get(resourceParts.size() - 1);
        EdmProperty edmProperty = uriProperty.getProperty();
        String edmPropertyName = edmProperty.getName();

        EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) edmProperty.getType();

        Entity entity = storage.readEntityData(edmEntitySet, keyPredicates.get(0));
        if (entity == null) { // Bad request
            throw new ODataApplicationException("Entity not found",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        Property property = entity.getProperty(edmPropertyName);
        if (property == null) {
            throw new ODataApplicationException("Property not found",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        Object value = property.getValue();
        if (value != null) {
            ODataSerializer serializer = odata.createSerializer(responseFormat);

            ContextURL contextUrl = ContextURL.
                    with().
                    entitySet(edmEntitySet).
                    navOrPropertyPath(edmPropertyName).
                    build();
            PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

            SerializerResult serializerResult = serializer.
                    primitive(serviceMetadata, edmPropertyType, property, options);
            InputStream propertyStream = serializerResult.getContent();

            response.setContent(propertyStream);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        } else {
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        }
    }

    @Override
    public void updatePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
            final ContentType requestFormat, final ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
    }

    @Override
    public void deletePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {
    }

    @Override
    public void init(final OData odata, final ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

}
