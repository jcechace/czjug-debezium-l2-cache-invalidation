package net.cechacek.demo.debezium.cacheinvalidation.persistence.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "quarkus.datasource")
public interface DataSourceConfig {

    String username();
    String password();
    @WithName("jdbc.url")
    String url();

    default ConnectionConfig connectionConfig() {
        var url = url();
        var hostStart = url.indexOf("://") + 3;
        var hostEnd = url.indexOf(":", hostStart);
        var portStart = hostEnd + 1;
        var portEnd = url.indexOf("/", portStart);
        var databaseStart = portEnd + 1;
        var databaseEnd = url.indexOf("?", databaseStart);

        var username = username();
        var password = password();
        var database = url.substring(databaseStart, (databaseEnd != -1)? databaseEnd : url.length());
        var host = url.substring(hostStart, hostEnd);
        var port = url.substring(portStart, portEnd);

        return new ConnectionConfig(username, password, database, host, Integer.parseInt(port));
    }

    record ConnectionConfig(String username, String password, String database, String host, int port) {}

}
