package com.khoros.conversation.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionLog {

    @JsonProperty("conversationId")
    private String conversationId;

    @JsonProperty("actionPerformed")
    private String actionPerformed;

    @JsonProperty("actionPerformedDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy hh:mm:ss a Z")
    private Date actionPerformedDate;

    @JsonProperty("historyAction")
    private String historyAction;

    @JsonProperty("actingAgent")
    private String actingAgent;

    @JsonProperty("actingAgentEmail")
    private String actingAgentEmail;

    @JsonProperty("workqueueCurrent")
    private String workqueueCurrent;

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("note")
    private String note;




}

