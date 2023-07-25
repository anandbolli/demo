package com.khoros.conversation.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class RestClient {

    @Autowired
    RestTemplate restTemplate;

    private HttpHeaders headers;

    public RestClient() {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
    }

    public void setHeaders(String username, String password) {

        String credentials = username + ":" + password;
        String encodedCredentials = Base64Utils.encodeToString(credentials.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        this.headers = headers;

    }

    public void setBearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        this.headers = headers;

    }

//    public void resetHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        this.headers = headers;
//    }

    public <T> T sendRequest(String url, HttpMethod method, Class<T> responseType) {
        URI requestUri = URI.create(url);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<T> responseEntity = restTemplate.exchange(requestUri, method, requestEntity, responseType);
        return responseEntity.getBody();
    }


}
