package com.khoros.conversation.repo;


import com.khoros.conversation.dto.ActionLog;
import com.khoros.conversation.dto.TimeInterval;
import com.khoros.conversation.service.DBConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Log4j2
public class ActionLogsCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${actionlog.table.insert}")
    private String insertSQL;

    @Autowired
    private DBConfig dbConfig;


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment env;


    public int[] saveActionLogs(List<ActionLog> entities, String timePeriod, TimeInterval interval,String tableName) {

        try {
            String insertQuery = insertSQL.replaceAll("tablename",tableName );
            int[] result = jdbcTemplate.batchUpdate(insertQuery, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                    ActionLog entity = entities.get(i);
                    preparedStatement.setString(1, entity.getConversationId());
                    preparedStatement.setString(2, entity.getActionPerformed());
                    preparedStatement.setDate(3, entity.getActionPerformedDate());
                    preparedStatement.setString(4, entity.getHistoryAction());
                    preparedStatement.setString(5, entity.getActingAgent());
                    preparedStatement.setString(6, entity.getActingAgentEmail());
                    preparedStatement.setString(7, entity.getWorkqueueCurrent());
                    preparedStatement.setString(8, entity.getTags());
                    preparedStatement.setString(9, entity.getComment());
                    preparedStatement.setString(10, entity.getNote());
                    preparedStatement.setString(11, timePeriod);
                    preparedStatement.setTimestamp(12, timestamp);
                    preparedStatement.setString(13, "ActionLog_Component");
                    preparedStatement.setLong(14, interval.getId().longValue());
                    preparedStatement.setTimestamp(15, timestamp);
                    preparedStatement.setString(16, "ActionLog_Component");
                }

                @Override
                public int getBatchSize() {
                    return entities.size();
                }

            });

            log.info("Action Logs Record Batch Update Completed  ");

            return result;
        } catch (Exception ex) {
            log.error("Exception at saveActionLogs " + ex.getMessage());
            throw ex;
        }
    }


    public Boolean checkIfRecordExist() {

        try {
            String intervals = env.getProperty("interval.table.records");
            log.debug("Intervals New Records Query : " + intervals);
            Boolean recordExists = jdbcTemplate.query(intervals, new RecordCheck());
            return recordExists;
        } catch (Exception ex) {
            log.error("Exception at checkIfRecordExist " + ex.getMessage());
            throw ex;
        }

    }

    public TimeInterval getNewStatusInterval() {

        try {
            String intervals = env.getProperty("interval.table.records.new");
            TimeInterval newStatusIntervals = jdbcTemplate.query(intervals, new TimeIntervalExtractor());
            return newStatusIntervals;

        } catch (Exception ex) {
            log.error("Exception at getNewStatusInterval " + ex.getMessage());
            throw ex;
        }

    }

    public java.util.Date getLatestInterval() {
        TimeInterval latestInterval = null;
        try {
            String intervals = env.getProperty("interval.table.records.latest");
            java.util.Date latestDate = jdbcTemplate.queryForObject(intervals, Timestamp.class);

            return latestDate;
        } catch (Exception ex) {
            log.error("Exception at getLatestInterval " + ex.getMessage());
            throw ex;
        }

    }

    private class RecordCheck implements ResultSetExtractor<Boolean> {
        @Override
        public Boolean extractData(ResultSet resultSet) throws SQLException {
            if (resultSet.next()) {
                return true;
            }
            return false;
        }

    }


    private class TimeIntervalExtractor implements ResultSetExtractor<TimeInterval> {
        @Override
        public TimeInterval extractData(ResultSet resultSet) throws SQLException {
            TimeInterval interval = null;
            if (resultSet.next()) {
                interval = TimeInterval.builder()
                        .id(BigInteger.valueOf(resultSet.getLong("ID")))
                        .startTimeEpoch(resultSet.getString("START_TIME_EPOCH"))
                        .endTimeEpoch(resultSet.getString("END_TIME_EPOCH"))
                        .startTime(resultSet.getTimestamp("START_TIME"))
                        .endTime(resultSet.getTimestamp("END_TIME"))
                        .date(resultSet.getDate("PROCESS_DATE"))
                        .build();
            }
            return interval;
        }

    }


    public BigInteger saveTimeInterval(TimeInterval interval) {

        try {
            String insertSQL = env.getProperty("interval.table.insert");

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(
                    new PreparedStatementCreator() {
                        @Override
                        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                            PreparedStatement ps = connection.prepareStatement(insertSQL, new String[]{"id"});
                            ps.setTimestamp(1, new Timestamp(interval.getDate().getTime()));
                            ps.setString(2, interval.getStartTimeEpoch());
                            ps.setString(3, interval.getEndTimeEpoch());
                            ps.setTimestamp(4, new Timestamp(interval.getStartTime().getTime()));
                            ps.setTimestamp(5, new Timestamp(interval.getEndTime().getTime()));
                            ps.setString(6, "PROCESSING");
                            ps.setString(7, "NEW");
                            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
                            ps.setString(9, "ActionLog_Component");
                            ps.setString(10, "NEW");
                            ps.setString(11, "NEW");
                            return ps;
                        }
                    },
                    keyHolder
            );
            log.info(" Time Interval Table insert completed");
            return BigInteger.valueOf(keyHolder.getKey().longValue());
        } catch (Exception ex) {
            log.error("Exception at saveTimeInterval " + ex.getMessage());
            throw ex;
        }
    }


    public void updateActionLogStatus(BigInteger id, String status) {

        try {
            String updateStatus = env.getProperty("interval.table.update");
            jdbcTemplate.update(updateStatus, status, new Timestamp(System.currentTimeMillis()), id);
            log.info(" Action Log Status UPDATE completed");

        } catch (Exception ex) {
            log.error("Exception at updateActionLogStatus " + ex.getMessage());
            throw ex;
        }
    }
}
