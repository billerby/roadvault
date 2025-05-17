package com.billerby.roadvault.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * Test configuration for using an in-memory H2 database.
 */
@TestConfiguration
public class TestH2Config {

    /**
     * Creates an embedded H2 datasource for testing.
     * 
     * @return H2 in-memory DataSource
     */
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("testdb;MODE=PostgreSQL")
                .build();
    }
}
