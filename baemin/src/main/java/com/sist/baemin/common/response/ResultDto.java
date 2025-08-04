package com.sist.baemin.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ResultDto<T> {

    private final Integer resultCode;
    private final String message;
    private final T data;

    public ResultDto(Integer resultCode, String message, T data) {
        this.resultCode = resultCode;
        this.message = message;
        this.data = data;
    }
}