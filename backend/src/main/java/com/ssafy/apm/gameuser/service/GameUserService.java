package com.ssafy.apm.gameuser.service;

import com.ssafy.apm.gameuser.dto.request.GameUserCreateRequestDto;
import com.ssafy.apm.gameuser.dto.request.GameUserUpdateRequestDto;
import com.ssafy.apm.gameuser.dto.response.GameUserDetailResponseDto;
import com.ssafy.apm.gameuser.dto.response.GameUserSimpleResponseDto;

import java.util.List;

public interface GameUserService {
    GameUserSimpleResponseDto createGameUser(GameUserCreateRequestDto requestDto);
    GameUserSimpleResponseDto updateGameUser(GameUserUpdateRequestDto requestDto);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    GameUserSimpleResponseDto deleteGameUser(String code);
    GameUserSimpleResponseDto deleteGameUser(String gameCode, Long userId);
    List<GameUserSimpleResponseDto> deleteGameUsersByGameCode(String gameCode);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    GameUserSimpleResponseDto findGameUser(String code);
    GameUserSimpleResponseDto findGameUser(String gameCode, Long userId);
    List<GameUserSimpleResponseDto> findSimpleGameUsersByGameCode(String gameCode);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* TODO: SimpleResponseDto 처럼 3가지 경우로 추가 기능 구현 필요 */
    List<GameUserDetailResponseDto> findDetailGameUsersByGameCode(String gameCode);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    GameUserSimpleResponseDto updateGameUserTeam(String team);
    GameUserSimpleResponseDto updateGameUserScore(Integer score);
    GameUserSimpleResponseDto updateGameUserIsHost(Boolean isHost);
    GameUserSimpleResponseDto updateGameUserScore(Long userId, Integer score);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* REFACTORED: findGameUser() 으로 리팩토링됨 */
    // GameUserSimpleResponseDto getGameUser(Long gameUserId);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* FIXME: 입장 조건을 GameService 에서 판단하고,
        GameService 에서 createGameUser() 를 호출하는 것이 나아보임 */
    // GameUserSimpleResponseDto postEnterGame(String gameCode);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* FIXME: GameService 로 이동하는 것이 나아보임 */
    // GameUserSimpleResponseDto postFastEnterGame();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* FIXME: GameService 로 이동하는 것이 나아보임 */
    // void updateUserScore(String gameCode);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* FIXME 1: 퇴장 조건을 GameService 에서 판단하고,
        GameService 에서 deleteGameUser() 를 호출하는 것이 나아보임 */
    /* FIXME 2: 혹은 단순 deleteGameUser API 호출을 통해 처리하고,
        WebSocket 통신이 끊어지는 경우에도, deleteGameUser()를 호출하는 것이 나아보임*/
    // Long deleteExitGame(Long gameId);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* FIXME: 중복기능으로 판단됨, userId 보단 해시 ID인 code로 처리하는 것이 나아보임. */
    // Long deleteExitGameByUserId(Long userId, String gameCode);

}
