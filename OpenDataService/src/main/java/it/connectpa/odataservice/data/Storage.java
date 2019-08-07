package it.connectpa.odataservice.data;

import it.connectpa.odataservice.dao.ODataDAO;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Storage {

    private static final Logger LOG = LoggerFactory.getLogger(Storage.class);

    @Autowired
    private ODataDAO oDataDAO;

    public EntityCollection readEntitySetData(final EdmEntitySet edmEntitySet) throws ODataApplicationException {
        List<Map<String, Object>> data = oDataDAO.findData(edmEntitySet.getEntityType().getName());
        EntityCollection retEntitySet = new EntityCollection();
        data.forEach(entity -> retEntitySet.getEntities().add(getEntity(entity, edmEntitySet)));

        return retEntitySet;
    }

    public Entity readEntityData(final EdmEntitySet edmEntitySet, final UriParameter keyParam) throws
            ODataApplicationException {
        Map<String, Object> entity =
                oDataDAO.findDataById(edmEntitySet.getEntityType().getName(), keyParam.getName(), keyParam.getText());

        return getEntity(entity, edmEntitySet);
    }

    private Entity getEntity(final Map<String, Object> entity, final EdmEntitySet edmEntitySet) {
        Entity retEntity = new Entity();
        entity.keySet().forEach(key
                -> retEntity.addProperty(new Property(null, key, ValueType.PRIMITIVE, entity.get(key))));
        retEntity.setId(createId(edmEntitySet.getName(), entity.get("ID")));

        return retEntity;
    }

    private URI createId(final String entitySetName, final Object id) {
        try {
            return new URI(entitySetName + "(" + String.valueOf(id) + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }
}
