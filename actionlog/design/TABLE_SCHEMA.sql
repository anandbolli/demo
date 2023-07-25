CREATE TABLE CONVERSATION_ACTION_LOG (
ID  BIGINT IDENTITY(1,1) PRIMARY KEY,
    TIME_PERIOD VARCHAR(255),
SPOTIFY_DATA_TIME_INTERVAL_ID BIGINT,
CONVERSATION_ID NVARCHAR(255) ',
ACTION_PERFORMED NVARCHAR(255) ',
ACTION_PERFORMEDDATE DATETIME ,
HISTORY_ACTION NVARCHAR(max) ',
ACTING_AGENT NVARCHAR(255) ',
ACTING_AGENTEMAIL NVARCHAR(255) ',
WORKQUEUE_CURRENT NVARCHAR(255) ',
TAGS NVARCHAR(max) ',
COMMENT NVARCHAR(max) ,
NOTE NVARCHAR(max) ,
CREATED_DATE DATETIME NOT NULL,
CREATED_BY VARCHAR(20) NOT NULL,
UPDATED_DATE DATETIME,
UPDATED_BY VARCHAR(20)
)



CREATE TABLE SPOTIFY_DATA_TIME_INTERVALS (
    ID BIGINT IDENTITY,
    PROCESS_DATE DATE,
    START_TIME_EPOCH VARCHAR(255) NOT NULL,
    END_TIME_EPOCH VARCHAR(255) NOT NULL,
    START_TIME DATETIME NOT NULL,
    END_TIME DATETIME NOT NULL,
    ACTION_LOG_STATUS VARCHAR(20),
    AUTHOR_STATUS VARCHAR(20),
     BATCH_PROCESS_STATUS VARCHAR(20),
      PLAYVOX_STATUS VARCHAR(20),
    CREATED_DATE DATETIME,
    CREATED_BY VARCHAR(20) NOT NULL,
    ACTION_LOG_STATUS_UPDATED_AT DATETIME,
    AUTHOR_STATUS_UPDATED_AT DATETIME,
     BATCH_STATUS_UPDATED_AT DATETIME,
     PLAYVOX_STATUS_UPDATED_AT DATETIME,
    PRIMARY KEY (ID)
);
