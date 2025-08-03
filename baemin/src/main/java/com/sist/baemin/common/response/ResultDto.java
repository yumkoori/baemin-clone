package com.sist.baemin.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ResultDto<T> {

    private final Integer resultCode;
    private final String message;
    private final T data;

}