package com.khoros.conversation.service;


import com.khoros.conversation.config.UnAvailableException;
import com.khoros.conversation.dto.ActionLog;
import com.khoros.conversation.dto.TimeInterval;
import com.khoros.conversation.repo.ActionLogsCustomRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Log4j2
public class ConversationService {

    @Autowired
    private RestClient restClient;

    @Autowired
    private Downloader downloader;


    @Autowired
    private RetryTemplate retryTemplate;

    @Value("${khoros.api.conversationexport.url}")
    private String actionLogURL;

    @Value("${khoros.api.auth.username}")
    private String authUser;

    @Value("${khoros.api.auth.password}")
    private String authPwd;

    @Value("${khoros.api.time.interval}")
    private int period;

    @Value("${db.insert.logBatchSize}")
    private int logBatchSize;

    @Autowired
    private DBConfig dbConfig;

    @Autowired
    private ActionLogsCustomRepository customRepository;


    public void actionLogsExport() {
        String startTime = null, endTime = null;
        String timeInterval = null;
        TimeInterval interval = null;
        try {
            interval = pickIntervalToProcess();
            if (interval != null) {
                invokeKhorosAndSaveLogs(interval);
            }
        } catch (Exception ex) {
            log.error("Exception at actionLogsExport for Time Interval : " + timeInterval + "Error Message : " + ex.getMessage());
            log.error("Stacktrace : " + ex);
            if (interval != null) {
                customRepository.updateActionLogStatus(interval.getId(), "ERROR");
            }
        }
    }


    public void invokeKhorosAndSaveLogs(TimeInterval interval) throws UnAvailableException {

        String timeInterval = null;
        try{
            String startTime = interval.getStartTimeEpoch();
            String endTime = interval.getEndTimeEpoch();
            timeInterval = startTime + "-" + endTime;

            String actionLogReportURL = actionLogURL + "&startTime=" + startTime + "&endTime=" + endTime;
            restClient.setHeaders(authUser, authPwd);
            LinkedHashMap response = restClient.sendRequest(actionLogReportURL, HttpMethod.GET, LinkedHashMap.class);
            JSONObject json = new JSONObject(response);
            log.debug("Action Logs Export API Response json : " + json);
            json = json.getJSONObject("result");
            String statusURL = json.getString("statusUrl");
            log.debug("Action Logs Status URL for Time Interval (" + timeInterval + " ) : " + statusURL);
            if (StringUtils.isNotEmpty(statusURL)) {
                String downloadURL = downloader.getActionLogExportDownloadURL(statusURL, restClient);
                log.info("Action Logs Download URL for Time Interval (" + timeInterval + " ) : " + downloadURL);
                List<ActionLog> actionLogs = downloader.invokeDownloadURL(downloadURL, restClient);
                if (!CollectionUtils.isEmpty(actionLogs)) {
                    log.info(" No .of Action Logs Records received for Time Interval   " + timeInterval + " : " + actionLogs.size());
                    dbConfig.createTableForDate(interval.getDate());
                    saveActionLogs(actionLogs, timeInterval, interval);
                    log.info("Action Logs Record Batch Updated for Interval Id: " + interval.getId());
                    customRepository.updateActionLogStatus(interval.getId(), "COMPLETED");

                } else {
                    log.info("No Action Logs Records received for Time Interval : " + timeInterval);
                }
            }
        }catch (Exception ex) {
            log.error("Exception at invokeKhorosAndSaveLogs for Time Interval : " + interval + "Error Message : " + ex.getMessage());
            log.error("Stacktrace : " + ex); throw ex;
        }
    }


    private void saveActionLogs(List<ActionLog> actionLogs, String interval, TimeInterval timeInterval) {

        try {
            int totalItems = actionLogs.size();
            String tableName = dbConfig.getActionLogTableName(timeInterval.getDate());
            int counter =0;
            long startTime = System.nanoTime();
            for (int i = 0; i < totalItems; i += logBatchSize) {
                int endIndex = Math.min(i + logBatchSize, totalItems);
                List<ActionLog> batch = actionLogs.subList(i, endIndex);
                log.info("Batch Number : " + counter++ + " .Batch Size : " +  batch.size());
                customRepository.saveActionLogs(batch, interval, timeInterval,tableName);
            }
            long endTime = System.nanoTime();
            double timeTakenSeconds = (endTime - startTime) / 1_000_000_000.0;
            log.info("Time taken by saveActionLog loops: " + timeTakenSeconds + " seconds for Record count : " + actionLogs.size());

        } catch (Exception ex) {
            log.error("Exception at Service Class : saveActionLogs " + ex.getMessage());
            throw ex;
        }

    }


    private TimeInterval pickIntervalToProcess() {
        try {
            TimeInterval latestInterval = null, newInterval = null;
            Date endDate, startDate;
            log.info("Latest Interval Picked : " + latestInterval);
            Boolean recordExist = customRepository.checkIfRecordExist();
            if (recordExist) {
                latestInterval = customRepository.getNewStatusInterval();
                if (latestInterval != null && latestInterval.getId() != null) {
                    return latestInterval;
                } else {
                    Date latestTime = customRepository.getLatestInterval();
                    startDate = latestTime;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(startDate);
                    calendar.add(Calendar.MINUTE, period);
                    endDate = calendar.getTime();
                    if (endDate.after(new Date())) {
                        return null;
                    }
                    newInterval = TimeInterval.builder()
                            .startTimeEpoch(startDate.getTime() + "")
                            .endTimeEpoch(endDate.getTime() + "")
                            .startTime(startDate)
                            .endTime(endDate)
                            .date(endDate)
                            .build();
                }
            } else {
                endDate = new Date(System.currentTimeMillis());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.add(Calendar.MINUTE, -period);
                startDate = calendar.getTime();
                newInterval = TimeInterval.builder()
                        .startTimeEpoch(startDate.getTime() + "")
                        .endTimeEpoch(endDate.getTime() + "")
                        .startTime(startDate)
                        .endTime(endDate)
                        .date(endDate)
                        .build();
            }

            BigInteger id = customRepository.saveTimeInterval(newInterval);
            newInterval.setId(id);
            return newInterval;

        } catch (Exception ex) {
            log.error("Exception at pickIntervalToProcess " + ex.getMessage());
            throw ex;
        }

    }


}
