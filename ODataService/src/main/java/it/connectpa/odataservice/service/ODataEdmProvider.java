package it.connectpa.odataservice.service;

import it.connectpa.odataservice.dao.ODataDAO;
import it.connectpa.odataservice.model.EntityProperty;
import it.connectpa.odataservice.model.MetadataInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.atteo.evo.inflector.English;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ODataEdmProvider extends CsdlAbstractEdmProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ODataEdmProvider.class);

    // Service Namespace
    public static final String NAMESPACE = "OData.Service";

    // EDM Container
    public static final String CONTAINER_NAME = "Container";

    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    @Autowired
    private ODataDAO oDataDAO;

    @Override
    public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) throws ODataException {
        List<MetadataInfo> entities = oDataDAO.findAllEntities();

        MetadataInfo entity = entities.stream().
                filter(e -> entityTypeName.equals(new FullQualifiedName(NAMESPACE, e.getName()))).
                findFirst().orElse(null);
        if (entity != null) {
            CsdlProperty id = new CsdlProperty().setName("ID").
                    setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());

            // create CsdlPropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName(entity.getName());

            List<CsdlProperty> properties = new ArrayList<>();
            properties.add(id);
            List<EntityProperty> entityProperties = oDataDAO.findProperties(entity.getName());
            entityProperties.forEach(e -> {
                if (!"ID".equals(e.getField().toUpperCase())) {
                    properties.add(new CsdlProperty().setName(e.getField()).setType(transferType(e.getType())));
                }
            });

            entityType.setProperties(properties);
            entityType.setKey(Collections.singletonList(propertyRef));
            return entityType;
        }

        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(final FullQualifiedName entityContainer, final String entitySetName) throws
            ODataException {
        if (entityContainer.equals(CONTAINER)) {
            List<MetadataInfo> entities = oDataDAO.findAllEntities();
            MetadataInfo entity = entities.stream().
                    filter(e -> entitySetName.equals(English.plural(e.getName()))).
                    findFirst().orElse(null);
            if (entity != null) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(English.plural(entity.getName()));
                entitySet.setType(new FullQualifiedName(NAMESPACE, entity.getName()));

                return entitySet;
            }
        }

        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        List<MetadataInfo> entities = oDataDAO.findAllEntities();
        entities.forEach(entity -> {
            try {
                entitySets.add(getEntitySet(CONTAINER, English.plural(entity.getName())));
            } catch (ODataException e) {
                LOG.error("While adding an entitySet {}", e);
            }
        });

        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        // create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        List<MetadataInfo> entities = oDataDAO.findAllEntities();

        LOG.info("entities " + entities);
        entities.forEach(entity -> {
            try {
                entityTypes.add(getEntityType(new FullQualifiedName(NAMESPACE, entity.getName())));
            } catch (ODataException e) {
                LOG.error("While adding an entityType {}", e);
            }
        });
        schema.setEntityTypes(entityTypes);

        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        List<CsdlSchema> schemas = new ArrayList<>();
        schemas.add(schema);

        return schemas;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName) {
        // This method is invoked when displaying the Service Document at e.g. http://localhost:8080/odata
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }

        return null;
    }

    private FullQualifiedName transferType(final String type) {
        if (type.toUpperCase().contains("VARCHAR")) {
            return EdmPrimitiveTypeKind.String.getFullQualifiedName();
        } else if (type.toUpperCase().contains("DATE")) {
            return EdmPrimitiveTypeKind.Date.getFullQualifiedName();
        } else if (type.toUpperCase().contains("TIMESTAMP")) {
            return EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName();
        } else if (type.toUpperCase().contains("BOOLEAN")) {
            return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
        } else {
            return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
        }
    }

}
