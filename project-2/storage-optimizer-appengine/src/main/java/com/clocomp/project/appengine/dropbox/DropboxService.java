package com.clocomp.project.appengine.dropbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DropboxService {
    private String appKey = "";

    @Configuration
    public class RestTemplateConfig {

//    private final RestTemplate restTemplate;

        @Bean
        public RestTemplate restTemplate(RestTemplateBuilder builder) {
            return builder.build();
        }
    }

    @Autowired
    private RestTemplate restTemplate;

    public String getDropBoxToken( String url){
        String resp = restTemplate.getForObject(url, String.class);
        return resp;
    }



}


