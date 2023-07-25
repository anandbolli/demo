package com.khoros.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeInterval {

    private BigInteger id;
    private Date date;
    private String startTimeEpoch;
    private String endTimeEpoch;
    private Date startTime;
    private Date endTime;
    private String actionLogStatus;
    private String authorStatus;
    private Date createdDate;
    private Date actionLogStatusUpdatedAt;
    private Date authorStatusUpdatedAt;

}
