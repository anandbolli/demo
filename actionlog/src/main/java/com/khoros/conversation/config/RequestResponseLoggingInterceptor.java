package com.khoros.conversation.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

@Log4j2
public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
       // traceResponse(response);
        return response;
    }

    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
        log.info("=========================== REQUEST ===========================");
        log.info("URI         : " + request.getURI());
        log.debug("Method      : " + request.getMethod());
        log.debug("Headers     : " + request.getHeaders());
        if(request.getMethod().matches("POST")){
            log.debug("Request body: " + new String(body, Charset.defaultCharset()));
        }
        log.info("==============================================================");
    }

    private void traceResponse(ClientHttpResponse response) throws IOException {
        log.info("=========================== RESPONSE ===========================");
        log.info("Status code  : " + response.getStatusCode());
        log.debug("Headers      : " + response.getHeaders());
        log.debug("Response body: " + StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
        log.info("==============================================================");
    }
}

