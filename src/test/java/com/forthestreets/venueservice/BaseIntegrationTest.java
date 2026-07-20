package com.forthestreets.venueservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 🐳 Base Integration Test Setup
 * Leverages Testcontainers with a PostGIS enabled Docker image.
 *
 * 🌟 SINGLETON CONTAINER PATTERN: By declaring the container statically and starting it
 * in a static initializer block, the same container instance is shared across all test files.
 * This prevents Docker recycle overhead, dropping test boot times from minutes to seconds.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    // We use a dedicated, official PostGIS image that matches our local PostgreSQL version
    @Container
    protected static final PostgreSQLContainer postgisContainer =
            new PostgreSQLContainer(DockerImageName.parse("postgis/postgis:16-3.4-alpine")
                    .asCompatibleSubstituteFor("postgres"))
                    .withDatabaseName("fts_test")
                    .withUsername("fts_test_user")
                    .withPassword("test_secret");

    static {
        // Start the container manually before Spring contexts initialize
        postgisContainer.start();
    }

    /**
     * Dynamically overrides application configuration properties on the fly,
     * pointing Spring Boot and Flyway to the random ephemeral port assigned by Docker.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgisContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgisContainer::getUsername);
        registry.add("spring.datasource.password", postgisContainer::getPassword);

        // Ensure Hibernate validates the dynamically generated PostGIS schemas correctly
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }
}