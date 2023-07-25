package com.khoros.conversation.controller;

import com.khoros.conversation.config.UnAvailableException;
import com.khoros.conversation.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "khoros/")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @RequestMapping(value = "conversation/")
    public void actionLogsExport()   {
         conversationService.actionLogsExport( );
    }

}
