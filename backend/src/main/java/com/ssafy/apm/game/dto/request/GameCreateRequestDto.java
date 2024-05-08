package com.ssafy.apm.game.dto.request;

import com.ssafy.apm.game.domain.Game;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameCreateRequestDto {
    private String channelCode;
    private String password;
    private String title;

    private Integer mode;
    private Integer style;
    private Boolean isTeam;
    private Boolean isPrivate;
    private Integer timeLimit;

    private Integer maxRounds;
    private Integer curPlayers;
    private Integer maxPlayers;

    public Game toEntity() {
        return Game.builder()
                .channelCode(this.channelCode)
                .password(this.password)
                .title(this.title)
                .mode(this.mode)
                .style(this.style)
                .isTeam(this.isTeam)
                .isPrivate(this.isPrivate)
                .isStarted(false)
                .timeLimit(this.timeLimit)
                .curRounds(0)
                .maxRounds(this.maxRounds)
                .curPlayers(this.curPlayers)
                .maxPlayers(this.maxPlayers)
                .build();
    }

}
