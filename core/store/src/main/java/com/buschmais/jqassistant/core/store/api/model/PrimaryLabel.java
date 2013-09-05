package com.buschmais.jqassistant.core.store.api.model;

import org.neo4j.graphdb.Label;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 04.09.13
 * Time: 20:59
 * To change this template use File | Settings | File Templates.
 */
public interface PrimaryLabel extends Label {

    boolean isIndexed();
}
