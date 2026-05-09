package com.cts.identity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Drops any stale cross-database FK on users.holder_id at startup.
 * The holder_id column is a plain Long reference to policy-service's DB
 * and must NOT have a FK constraint in identity_db.
 */
@Component
public class DatabaseMigration implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection()) {
            // Find and drop any FK constraint on the holder_id column of the users table
            ResultSet fks = conn.getMetaData().getImportedKeys(conn.getCatalog(), null, "users");
            while (fks.next()) {
                String fkName   = fks.getString("FK_NAME");
                String fkColumn = fks.getString("FKCOLUMN_NAME");
                if ("holder_id".equalsIgnoreCase(fkColumn) && fkName != null) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("ALTER TABLE users DROP FOREIGN KEY `" + fkName + "`");
                        System.out.println("[DatabaseMigration] Dropped stale FK '" + fkName
                                + "' on users.holder_id — holder_id is a plain column, no FK needed.");
                    } catch (Exception dropEx) {
                        System.err.println("[DatabaseMigration] Could not drop FK '" + fkName + "': " + dropEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // Non-fatal — log and continue startup
            System.err.println("[DatabaseMigration] FK cleanup skipped: " + e.getMessage());
        }
    }
}
