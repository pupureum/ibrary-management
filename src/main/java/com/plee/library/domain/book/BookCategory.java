package com.plee.library.domain.book;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookCategory {
    ECONOMICS("경제경영"),
    SELF_DEVELOPMENT("자기계발"),
    LITERATURE_ESSAY("시/에세이"),
    HUMANITIES("인문"),
    NOVEL("소설"),
    LANGUAGE("국어/외국어"),
    POLITICS_SOCIAL("정치/사회"),
    HISTORY_CULTURE("역사/문화"),
    SCIENCE_ENGINEERING("과학/공학"),
    IT_PROGRAMMING("IT/프로그래밍"),
    HEALTH_MEDICAL("건강/의학"),
    HOME_LIFE_COOKING("가정/생활/요리"),
    TRAVEL_HOBBY("여행/취미"),
    ART_POPULAR_CULTURE("예술/대중문화"),
    CHILDREN("아동"),
    TEENAGER("청소년"),
    TEXTBOOK_EXAM("교재/수험서");

    private final String title;
}
