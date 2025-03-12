package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;

import java.util.List;
import java.util.Map;

@Component
public class EwmStatClient {

    private final RestTemplate rest;

    @Autowired
    public EwmStatClient(RestTemplateBuilder restTemplateBuilder) {
        this.rest = restTemplateBuilder
                .uriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:9090"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public ResponseEntity<Object> saveStat(EndpointHitDto endpointHitDto) {
        return post("/hit", endpointHitDto);
    }

    public ResponseEntity<Object> getStat(String start, String end, List<String> uris, Boolean unique) {
        Map<String, Object> params = Map.of(
                "start", start,
                "end", end,
                "uris", uris,
                "unique", unique);
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", params);
    }


    private ResponseEntity<Object> post(String path, EndpointHitDto endpointHitDto) {
        return makeAndSendRequest(HttpMethod.POST, path, null, endpointHitDto);
    }

    private ResponseEntity<Object> get(String path, @Nullable Map<String, Object> params) {
        return makeAndSendRequest(HttpMethod.GET, path, params, null);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod httpMethod,
                                                          String path,
                                                          @Nullable Map<String, Object> params,
                                                          @Nullable T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> response;
        try {
            if (params != null) {
                response = rest.exchange(path, httpMethod, requestEntity, Object.class, params);
            } else {
                response = rest.exchange(path, httpMethod, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }
        return responseBuilder.build();
    }
}