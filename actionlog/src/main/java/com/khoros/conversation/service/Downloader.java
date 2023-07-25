package com.khoros.conversation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khoros.conversation.config.UnAvailableException;
import com.khoros.conversation.dto.ActionLog;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@Log4j2
public class Downloader {


    public String getActionLogExportDownloadURL(String statusURL, RestClient restClient) throws UnAvailableException {

        try {

            LinkedHashMap response = restClient.sendRequest(statusURL, HttpMethod.GET, LinkedHashMap.class);
            JSONObject json = new JSONObject(response);
            //log.info("AuthorExport Status Response json : " + json);
            json = json.getJSONObject("result");
            json = json.getJSONObject("jobInfo");
            String downloadUrl = json.getString("downloadUrl");
            if (StringUtils.isEmpty(downloadUrl)) {
                log.info("Attempting to fetch download URL again for : " + statusURL);
                throw new UnAvailableException("Download URL is Empty.Process Running retry after sometime.");
            }
            return downloadUrl;

        } catch (Exception ex) {
            log.error(" Exception at  getActionLogExportDownloadURL : " + ex.getMessage());
            throw ex;
        }
    }


    public List<ActionLog> invokeDownloadURL(String downloadURL, RestClient restClient) {

        try {
            String response = restClient.sendRequest(downloadURL, HttpMethod.GET, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            List<ActionLog> authorList = objectMapper.readValue(response, new TypeReference<List<ActionLog>>() {
            });

            return authorList;
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException at invokeDownloadURL" + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception ex) {
            log.error("Exception at invokeDownloadURL" + ex.getMessage());
            throw ex;
        }
    }


}
