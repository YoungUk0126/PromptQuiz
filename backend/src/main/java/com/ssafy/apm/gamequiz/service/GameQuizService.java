package com.ssafy.apm.gamequiz.service;

import com.ssafy.apm.gamequiz.dto.response.GameQuizGetResponseDto;

public interface GameQuizService {
    GameQuizGetResponseDto getGameQuizDetail(Long gameId);
}