package com.plee.library.dto.book.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchBookRequest {
    @NotEmpty
    private String keyword = "";
    private int display = 50;
    private int start = 1;

    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("query", keyword);
        requestMap.add("display", String.valueOf(display));
        requestMap.add("start", String.valueOf(start));
        return requestMap;
    }
}
