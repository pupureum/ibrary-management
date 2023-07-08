package com.plee.library.dto.admin.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookRequest {

    @NotNull(message = "수량을 입력해주세요.")
    @Min(value = 1, message = "수량은 최소 1 이상이어야 합니다.")
    @Max(value = 9999, message = "수량은 최대 9999 이하여야 합니다.")
    private Integer newQuantity;
}
