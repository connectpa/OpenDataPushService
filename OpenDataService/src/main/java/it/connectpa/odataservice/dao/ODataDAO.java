package it.connectpa.odataservice.dao;

import it.connectpa.odataservice.model.EntityProperty;
import it.connectpa.odataservice.model.MetadataInfo;
import java.util.List;

public interface ODataDAO {

    List<MetadataInfo> findAllEntities();

    List<EntityProperty> findProperties(String entity);
}
