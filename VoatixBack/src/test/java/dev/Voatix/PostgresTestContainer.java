package dev.Voatix;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainer {

    private static final PostgreSQLContainer<?> container;

    static {
        container = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("voatix")
                .withUsername("postgres")
                .withPassword("postgres");
        container.start();
    }

    public static PostgreSQLContainer<?> getInstance() {
        return container;
    }
}
