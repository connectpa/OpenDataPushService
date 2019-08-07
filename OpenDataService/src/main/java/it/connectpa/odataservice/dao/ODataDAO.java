package it.connectpa.odataservice.dao;

import it.connectpa.odataservice.model.EntityProperty;
import it.connectpa.odataservice.model.MetadataInfo;
import java.util.List;
import java.util.Map;

public interface ODataDAO {

    List<MetadataInfo> findAllEntities();

    List<EntityProperty> findProperties(String entity);

    List<Map<String, Object>> findData(String tableName);

    Map<String, Object> findDataById(String tableName, String column, String value);
}
