package com.cts.claimbridge.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Runs once at startup to drop stale columns that Hibernate's ddl-auto=update
 * will never remove on its own.
 */
@Component
public class DatabaseMigration implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        dropColumnIfExists("policyholders", "user_id");
        dropColumnIfExists("policyholders", "policy_id");
        dropColumnIfExists("policy",        "claim_id");
    }

    private void dropColumnIfExists(String table, String column) {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet cols = conn.getMetaData().getColumns(
                    conn.getCatalog(), null, table, column);
            if (cols.next()) {
                try (var stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE `" + table + "` DROP COLUMN `" + column + "`");
                    System.out.println("[DatabaseMigration] Dropped column " + table + "." + column);
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseMigration] Could not drop " + table + "." + column + ": " + e.getMessage());
        }
    }
}
