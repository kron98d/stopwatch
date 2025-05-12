package com.example.stopwatch;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Transactional
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    private static PostgreSQLContainer<?> POSTGRESQL_CONTAINER;
    private static ToxiproxyContainer TOXIPROXY_CONTAINER;
    protected static ToxiproxyContainer.ContainerProxy proxy;

    static {
        Network network = Network.newNetwork();

        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:14.2")
                .withNetwork(network)
                .withNetworkAliases("postgres");

        TOXIPROXY_CONTAINER = new ToxiproxyContainer("shopify/toxiproxy")
                .withNetwork(network);

        POSTGRESQL_CONTAINER.start();
        TOXIPROXY_CONTAINER.start();

        proxy = TOXIPROXY_CONTAINER.getProxy("postgres", 5432);
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        String proxiedUrl = String.format("jdbc:postgresql://%s:%d/test", proxy.getContainerIpAddress(), proxy.getProxyPort());

        registry.add("spring.datasource.url", () -> proxiedUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.liquibase.url", () -> proxiedUrl);
        registry.add("spring.liquibase.user", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.liquibase.password", POSTGRESQL_CONTAINER::getPassword);
    }
}
