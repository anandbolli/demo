package com.khoros.conversation.test;

import com.khoros.conversation.dto.TimeInterval;
import com.khoros.conversation.service.ConversationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExportTest extends ActionLogServiceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;


    @Autowired
    ConversationService actionLogService;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testCompleteAPIFlow() throws Exception {
        mockMvc.perform(get("/khoros/conversation/")).andExpect(status().isOk());
    }

    @Test
    public void testEmptyInterval() throws Exception {

        assertThrows(HttpClientErrorException.class, () -> actionLogService.invokeKhorosAndSaveLogs(new TimeInterval()),
                "Bad request Exception");
    }

    @Test
    public void testFutureInterval() throws Exception {

        Date startDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DATE, 1);
        startDate = calendar.getTime();

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(startDate);
        calendar1.add(Calendar.MINUTE, 5);
        Date endDate = calendar1.getTime();

        TimeInterval interval = TimeInterval.builder()
                .startTimeEpoch(startDate.getTime() + "")
                .endTimeEpoch(endDate.getTime() + "")
                .endTime(endDate)
                .startTime(startDate)
                .build();

        assertThrows(HttpClientErrorException.class, () -> actionLogService.invokeKhorosAndSaveLogs(interval),
                "Bad request Exception");
    }
}
