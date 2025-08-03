package com.sist.baemin.user.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.user.dto.ArchSampleRequestDto;
import com.sist.baemin.user.dto.ArchSampleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class ArchSampleController {

	@GetMapping(path = "/archsample/member/register/{email}/{password}/{name}")
	ResponseEntity<ResultDto<ArchSampleResponseDto>> registerMember(@RequestBody ArchSampleRequestDto request) {

		ArchSampleResponseDto responseDto = null;

		ResultDto<ArchSampleResponseDto> result = new ResultDto<>(201, "테스트 메시지", responseDto);

		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}
}









