package org.example.oct19;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Environment-only JDBC configuration, with a startable but unready application when DB configuration is absent. */
@Configuration
public class DatabaseConfiguration {
    /** Creates the selected PostgreSQL connection source without inventing URL, username or password defaults. */
    @Bean
    public DataSource dataSource(Environment environment) {
        String url = environment.getProperty("DATABASE_URL");
        if (url == null || url.isBlank()) {
            return new AbstractDataSource() {
                /** {@inheritDoc} */
                @Override
                public Connection getConnection() throws SQLException {
                    throw new SQLException("DATABASE_URL, DATABASE_USER and DATABASE_PASSWORD require configuration");
                }

                /** {@inheritDoc} */
                @Override
                public Connection getConnection(String username, String password) throws SQLException {
                    return getConnection();
                }
            };
        }
        DriverManagerDataSource source = new DriverManagerDataSource();
        source.setDriverClassName("org.postgresql.Driver");
        source.setUrl(url);
        source.setUsername(environment.getProperty("DATABASE_USER"));
        source.setPassword(environment.getProperty("DATABASE_PASSWORD"));
        return source;
    }

    /** Applies Flyway migrations when a DB is configured; an unconfigured app never pretends database readiness. */
    @Bean
    public FlywayMigrationStrategy migrationStrategy(Environment environment) {
        return flyway -> {
            String url = environment.getProperty("DATABASE_URL");
            if (url != null && !url.isBlank()) {
                flyway.migrate();
            }
        };
    }
}
