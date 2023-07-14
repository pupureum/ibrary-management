package com.plee.library.config;

import com.plee.library.dto.book.request.SearchApiBookRequest;
import com.plee.library.dto.book.response.SearchBookResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class NaverBookSearchConfig {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.url}")
    private String bookSearchUrl;

    public SearchBookResponse searchBook(SearchApiBookRequest searchBookRequest) {
        var uri = UriComponentsBuilder.fromUriString(bookSearchUrl)
                .queryParams(searchBookRequest.toMultiValueMap())
                .build()
                .encode()
                .toUri();

        var headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var httpEntity = new HttpEntity<>(headers);
        var responseType = new ParameterizedTypeReference<SearchBookResponse>(){};
        var responseEntity = new RestTemplate().exchange(
                uri, HttpMethod.GET, httpEntity, responseType);

        return responseEntity.getBody();
    }
}
