package dev.Voatix.config;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestPostgresContainer extends PostgreSQLContainer<TestPostgresContainer> {

    private static final String IMAGE = "postgres:17";

    private static TestPostgresContainer container;

    private TestPostgresContainer() {
        super(IMAGE);
    }

    public static TestPostgresContainer getInstance() {
        if (container == null) {
            container = new TestPostgresContainer();
            container.start();
        }
        return container;
    }
}

