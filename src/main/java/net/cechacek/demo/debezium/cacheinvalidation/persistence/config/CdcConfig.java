package net.cechacek.demo.debezium.cacheinvalidation.persistence.config;

import io.smallrye.config.ConfigMapping;

import java.util.List;


@ConfigMapping(prefix = "debezium")
public interface CdcConfig {
    String name();
    String prefix();
    List<String> tables();

    default String topic(String table) {
        if (!tables().contains(table)) {
            throw new IllegalArgumentException("Table " + table + " is not configured for CDC");
        }
        return prefix() + "." + table;
    }

    default String tablesAsString() {
        return String.join(",", tables());
    }
}
