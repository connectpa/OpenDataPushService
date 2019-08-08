package it.connectpa.odataservice.service;

import it.connectpa.odataservice.data.Storage;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
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
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ODataEntityCollectionProcessor implements EntityCollectionProcessor {

    private OData odata;

    private ServiceMetadata serviceMetadata;

    @Autowired
    private Storage storage;

    @Override
    public void readEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
            final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        EntityCollection entitySet = storage.readEntitySetData(edmEntitySet);

        List<Entity> entityList = entitySet.getEntities();
        EntityCollection returnEntityCollection = new EntityCollection();

        // Apply system query options
        // $count implementation
        CountOption countOption = uriInfo.getCountOption();
        if (countOption != null) {
            boolean isCount = countOption.getValue();
            if (isCount) {
                returnEntityCollection.setCount(entityList.size());
            }
        }

        // $skip implementation
        SkipOption skipOption = uriInfo.getSkipOption();
        if (skipOption != null) {
            int skipNumber = skipOption.getValue();
            if (skipNumber >= 0) {
                if (skipNumber <= entityList.size()) {
                    entityList = entityList.subList(skipNumber, entityList.size());
                } else {
                    // The client skipped all entities
                    entityList.clear();
                }
            } else {
                throw new ODataApplicationException("Invalid value for $skip", HttpStatusCode.BAD_REQUEST.
                        getStatusCode(), Locale.ROOT);
            }
        }

        // $top implementation
        TopOption topOption = uriInfo.getTopOption();
        if (topOption != null) {
            int topNumber = topOption.getValue();
            if (topNumber >= 0) {
                if (topNumber <= entityList.size()) {
                    entityList = entityList.subList(0, topNumber);
                }  // else the client has requested more entities than available => return what we have
            } else {
                throw new ODataApplicationException("Invalid value for $top", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                        Locale.ROOT);
            }
        }

        // $orderby implementation
        OrderByOption orderByOption = uriInfo.getOrderByOption();
        if (orderByOption != null) {
            entityList = executeOrderByOption(orderByOption, entityList);
        }

        // $select implementation
        SelectOption selectOption = uriInfo.getSelectOption();

        // $filter implementation
        FilterOption filterOption = uriInfo.getFilterOption();
        if (filterOption != null) {
            entityList = executeFilterOption(filterOption, entityList);
        }

        entityList.forEach(returnEntityCollection.getEntities()::add);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        String selectList = odata.createUriHelper()
                .buildContextURLSelectList(edmEntityType, null, selectOption);

        ContextURL contextUrl = ContextURL.with()
                .entitySet(edmEntitySet)
                .selectList(selectList)
                .build();

        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.
                with().
                count(countOption).
                contextURL(contextUrl).
                select(selectOption).
                id(id).
                build();

        ODataSerializer serializer = odata.createSerializer(responseFormat);

        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType,
                returnEntityCollection, opts);
        InputStream serializedContent = serializerResult.getContent();

        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    @Override
    public void init(final OData odata, final ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    private List<Entity> executeOrderByOption(final OrderByOption orderByOption, final List<Entity> entityList) {
        List<OrderByItem> orderItemList = orderByOption.getOrders();
        // this case for only one item 
        final OrderByItem orderByItem = orderItemList.get(0);

        Expression expression = orderByItem.getExpression();
        if (expression instanceof Member) {
            UriInfoResource resourcePath = ((Member) expression).getResourcePath();
            UriResource uriResource = resourcePath.getUriResourceParts().get(0);
            if (uriResource instanceof UriResourcePrimitiveProperty) {
                EdmProperty edmProperty = ((UriResourcePrimitiveProperty) uriResource).getProperty();
                final String sortPropertyName = edmProperty.getName();
                entityList.sort((Entity entity1, Entity entity2)
                        -> {
                    String propertyValue1 = entity1.getProperty(sortPropertyName).getValue().toString();
                    String propertyValue2 = entity2.getProperty(sortPropertyName).getValue().toString();

                    int compareResult = propertyValue1.compareTo(propertyValue2);
                    // if 'desc' is specified in the URI, change the order
                    if (orderByItem.isDescending()) {
                        return -compareResult; // just reverse order
                    }
                    return compareResult;
                });
            }
        }
        return entityList;
    }

    private List<Entity> executeFilterOption(final FilterOption filterOption, final List<Entity> entityList)
            throws ODataApplicationException {
        try {
            Iterator<Entity> entityIterator = entityList.iterator();

            // Evaluate the expression for each entity
            // If the expression is evaluated to "true", keep the entity otherwise remove it from the entityList
            while (entityIterator.hasNext()) {
                Entity currentEntity = entityIterator.next();
                Expression filterExpression = filterOption.getExpression();
                FilterExpressionVisitor expressionVisitor = new FilterExpressionVisitor(currentEntity);

                // Start evaluating the expression
                Object visitorResult = filterExpression.accept(expressionVisitor);
                if (visitorResult instanceof Boolean) {
                    if (!Boolean.TRUE.equals(visitorResult)) {
                        // The expression evaluated to false (or null), so we have to remove the currentEntity from entityList
                        entityIterator.remove();
                    }
                } else {
                    throw new ODataApplicationException("A filter expression must evaulate to type Edm.Boolean",
                            HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
                }
            }

        } catch (ExpressionVisitException e) {
            throw new ODataApplicationException("Exception in filter evaluation",
                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
        }
        return entityList;
    }

}
