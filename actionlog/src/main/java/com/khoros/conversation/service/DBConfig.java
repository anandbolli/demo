package com.khoros.conversation.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;


@Component
@EnableScheduling
@Log4j2
public class DBConfig {

    @Value("${actionlog.table.schema}")
    private String actionLogSchema;



    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void createTableForDate(Date processDate) {
        String tableDate = new SimpleDateFormat("dd_MM_YYYY").format(processDate);
        String tableName = "CONVERSATION_ACTION_LOG_" + tableDate;
        String authorSchemaSQL = actionLogSchema.replaceFirst("CONVERSATION_ACTION_LOG_", tableName);

        // Check if the table already exists
        if (!isTableExists(tableName)) {
            // Create the table
            entityManager.createNativeQuery(authorSchemaSQL).executeUpdate();
            log.info("Created New Table for Author : " + tableName);

        }
    }


    private boolean isTableExists(String tableName) {
        String query = "SELECT  COUNT(1) FROM " + tableName;
        try {
            jdbcTemplate.queryForObject(query, Integer.class);
            log.info(tableName + " Table Exist");

            return true;
        } catch (Exception ex) {
            log.error("Exception at isTableExists : " + ex.getMessage());
            return false;
        }
    }

    public String getActionLogTableName(Date processDate) {
        String tableDate = new SimpleDateFormat("dd_MM_YYYY").format(processDate);
        String tableName = "CONVERSATION_ACTION_LOG_" + tableDate;
        if (isTableExists(tableName)) {
            return tableName;
        }
        return null;
    }
}
