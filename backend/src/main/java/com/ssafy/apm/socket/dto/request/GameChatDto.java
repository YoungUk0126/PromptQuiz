package com.ssafy.apm.socket.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class GameChatDto {
    // 사용자 아이디
    private Long userId;

    // 사용자 닉네임
    private String nickname;

    // 게임방 코드
    private String uuid;

    // 게임방 아이디
    private Long gameId;

    // 현재 라운드
    private Integer round;

    // 메시지의 내용을 저장하기 위한 변수
    private String content;

    // 메시지 작성시간
    private String createdDate;
}