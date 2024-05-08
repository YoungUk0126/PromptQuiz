package com.ssafy.apm.game.service;

import com.ssafy.apm.game.domain.Game;
import com.ssafy.apm.game.dto.request.GameCreateRequestDto;
import com.ssafy.apm.game.dto.request.GameUpdateRequestDto;
import com.ssafy.apm.game.dto.response.GameResponseDto;
import com.ssafy.apm.game.exception.GameAlreadyStartedException;
import com.ssafy.apm.game.exception.GameFullException;
import com.ssafy.apm.game.exception.GameNotFoundException;
import com.ssafy.apm.game.repository.GameRepository;
import com.ssafy.apm.gamequiz.domain.GameQuizEntity;
import com.ssafy.apm.gamequiz.repository.GameQuizRepository;
import com.ssafy.apm.gameuser.domain.GameUser;
import com.ssafy.apm.gameuser.dto.response.GameUserSimpleResponseDto;
import com.ssafy.apm.gameuser.exception.GameUserNotFoundException;
import com.ssafy.apm.gameuser.repository.GameUserRepository;
import com.ssafy.apm.quiz.domain.Quiz;
import com.ssafy.apm.quiz.exception.QuizNotFoundException;
import com.ssafy.apm.quiz.repository.QuizRepository;
import com.ssafy.apm.user.domain.User;
import com.ssafy.apm.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameServiceImpl implements GameService {

    private final UserService userService;
    private final GameRepository gameRepository;
    private final QuizRepository quizRepository;
    private final GameQuizRepository gameQuizRepository;
    private final GameUserRepository gameUserRepository;
    private final ChoiceService choiceService;
    private final BlankChoiceService blankChoiceService;
    private final BlankSubjectiveService blankSubjectiveService;

    @Override
    @Transactional
    public GameResponseDto createGame(GameCreateRequestDto requestDto) {
        User user = userService.loadUser();
        Game game = gameRepository.save(requestDto.toEntity());
        GameUser gameUser = GameUser.builder()
                .gameCode(game.getCode())
                .userId(user.getId())
                .isHost(true)
                .score(0)
                .team("NOTHING")
                .build();
        gameUserRepository.save(gameUser);
        return new GameResponseDto(game);
    }
    /* FIXME: 입장 조건을 GameService 에서 판단하고,
        GameService 에서 createGameUser() 를 호출하는 것이 나아보임 */
    //    게임 입장할때
    @Override
    @Transactional
    public GameUserSimpleResponseDto enterGame(String gameCode) {
        User user = userService.loadUser();
        Long userId = user.getId();
        GameUser entity = GameUser.builder()
                .gameCode(gameCode)
                .userId(userId)
                .isHost(false)
                .score(0)
                .team("NOTHING")
                .build();

        Game game = gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException(gameCode));
        if (game.getIsStarted()) throw new GameAlreadyStartedException(gameCode);
        if (game.getCurPlayers() >= game.getMaxPlayers()) throw new GameFullException(gameCode);
        game.increaseCurPlayers();

        gameRepository.save(game);
        entity = gameUserRepository.save(entity);

        return new GameUserSimpleResponseDto(entity);
    }
    /* FIXME: GameService 로 이동하는 것이 나아보임 */
//    @Override
//    @Transactional
//    public GameUserSimpleResponseDto postFastEnterGame() {
////        Todo: 프론트에 던져줄 CustomException 만들기
////        로그인 한놈 유저 정보 불러오기
//        User user = userService.loadUser();
//        Long userId = user.getId();
//
//        UserChannelEntity userChannel = userChannelRepository.findByUserId(userId)
//                .orElseThrow(() -> new UserChannelNotFoundException("No entity exist by userId!"));
//
//        List<Game> gameList = gameRepository.findAllByChannelCode(userChannel.getChannelCode())
//                .orElseThrow(() -> new GameNotFoundException("No entities exists by channelId!"));// 채널에 생성된 방이 없다면
//
////        에러 코드를 프론트에서 받아 방을 만들 수 있게 처리해야함
//
//        for (Game entity : gameList) {
//            if (!entity.getIsStarted() && entity.getCurPlayers() < entity.getMaxPlayers()) { // 아직 입장할 수 있고 curPlayers가 maxPlayers보다 작을 때
//                //        일반유저
//                GameUser gameUser = GameUser.builder()
//                        .gameCode(entity.getCode())
//                        .userId(userId)
//                        .isHost(false)
//                        .score(0)
//                        .team("NOTHING")
//                        .build();
//
//                //        방에 접속 중인 인원 하나 늘려줌
//                entity.increaseCurPlayers();
//
//                gameRepository.save(entity);
//                gameUser = gameUserRepository.save(gameUser);
//
//                return new GameUserSimpleResponseDto(gameUser);
//            }
//        }
////        입장 가능한 방이 없으므로 방을 만들어야 한다고 프론트에 전달
//
//        return null;
//    }
    @Override
    public List<GameResponseDto> findGamesByChannelCode(String channelCode) {
        List<Game> entityList = gameRepository.findAllByChannelCode(channelCode)
                .orElseThrow(() -> new GameNotFoundException("No entities exists by channelId"));

        return entityList.stream().map(GameResponseDto::new).toList();
    }

    @Override
    public GameResponseDto findGameByGameCode(String gameCode) {
        Game gameEntity = gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException(gameCode));

        GameResponseDto dto = new GameResponseDto(gameEntity);
        return dto;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    @Transactional
    public GameResponseDto updateGame(GameUpdateRequestDto gameUpdateRequestDto) {
        Game gameEntity = gameRepository.findByCode(gameUpdateRequestDto.getCode())
                .orElseThrow(() -> new GameNotFoundException(gameUpdateRequestDto.getCode()));

        gameEntity.update(gameUpdateRequestDto);
        gameRepository.save(gameEntity);
        return new GameResponseDto(gameEntity);
    }
    /* FIXME: GameService 로 이동하는 것이 나아보임 */
//    @Override
//    @Transactional
//    public void updateUserScore(String gameCode) {
//        Game game = gameRepository.findByCode(gameCode)
//                .orElseThrow(() -> new GameNotFoundException(gameCode));
//        List<GameUser> gameUserList = gameUserRepository.findAllByGameCode(gameCode)
//                .orElseThrow(() -> new GameUserNotFoundException("No entities exists by gameCode!"));
//        List<User> userList = new ArrayList<>();
//
//        int winnerListSize = gameUserList.size() / 2;
//        int loserListSize = winnerListSize + gameUserList.size() % 2;
//        int totalScore = 10 * game.getMaxRounds();
////        12가 최대 게임 참여자 수
//        int getWinnerMaxScore = Math.round(totalScore * (Math.min(game.getCurPlayers(), 12) / 12));
//
//        //            score를 기준으로 높은 순서대로 리스트가 정렬됨
//        List<GameUser> GameUsers = gameUserList.stream()
//                .map(obj -> (GameUser) obj)
//                .sorted((user1, user2) -> user2.getScore().compareTo(user1.getScore()))
//                .toList();
//
////        팀전일때
//        if (game.getIsTeam()) {
////            테스트 완료되면 리팩토링 해야합니다 ㅎㅎ..
//            int redTeamTotalScore = 0, blueTeamTotalScore = 0;
//            List<GameUser> redTeamEntity = new ArrayList<>();
//            List<GameUser> blueTeamEntity = new ArrayList<>();
//
////            누가 이겼는가
//            for (GameUser entity : GameUsers) {
//                if (entity.getTeam() == "RED") {
//                    redTeamTotalScore += entity.getScore();
//                    redTeamEntity.add(entity);
//                } else {
//                    blueTeamTotalScore += entity.getScore();
//                    blueTeamEntity.add(entity);
//                }
//            }
////            RedTeam이 점수가 더 높다면
//            if (redTeamTotalScore > blueTeamTotalScore) {
//                winnerTeamScore(userList, redTeamEntity, getWinnerMaxScore);
//                loserTeamScore(userList, blueTeamEntity, getWinnerMaxScore);
//            }
////            Blue팀의 점수가 더 높다면
//            else {
//                winnerTeamScore(userList, blueTeamEntity, getWinnerMaxScore);
//                loserTeamScore(userList, redTeamEntity, getWinnerMaxScore);
//            }
//        }
////        개인전일때
//        else {
////            점수 받는 놈들 로직
//            /* 10라운드 6명 기준
//             * getWinnerMaxScore = 10*10(10라운드) * (curPlayers/maxPlayers) = 50( 6명이서 게임할 때 1등이 받을 점수 )
//             * 1등 : 50 * 0.8^0 = 50,  2등 : 50 * 0.8^1 = 40, 3등 : 50*0.8^2 = 32
//             * 절반 이상부터는 점수 잃는 놈들
//             * 4등 : -50 * 0.8^2(3-1) = -32, 5등 : -50 * 0.8^1(3-2) = -40, 6등 : -40 * 0.8^0 = -50
//             *
//             * */
//            /* 10라운드 7명 기준
//             * getWinnerMaxScore = 10*10(10라운드) * (curPlayers/maxPlayers) = 58( 7명이서 게임할 때 1등이 받을 점수, 반올림 )
//             * 1등 : 58 * 0.8^0 = 58,  2등 : 58 * 0.8^1 = 46(반올림), 3등 : 58 * 0.8^2 = 37(반올림)
//             * 절반 이상부터는 점수 잃는 놈들
//             * 4등 : -58 * 0.8^3(4-1) = -29, 5등 : -58 * 0.8^2(4-2) = -37, 6등 : -58 * 0.8^1(4-3) : -46, 7등 : -58 * 0.8^0 : -58
//             *
//             * */
//            for (int i = 0; i < winnerListSize; i++) {
//                GameUser entity = GameUsers.get(i);
//                User user = userRepository.findById(entity.getUserId())
//                        .orElseThrow(() -> new UserNotFoundException(entity.getUserId()));
//
//                int earnUserScore = (int) Math.round(getWinnerMaxScore * Math.pow(0.8, i));
//
//                UserScoreUpdateRequestDto dto = UserScoreUpdateRequestDto.builder()
//                        .soloScore(earnUserScore)
//                        .totalScore(earnUserScore)
//                        .teamScore(0)
//                        .build();
//
//                user.updateScore(dto);
//                userList.add(user);
//            }
////            점수 잃는 놈들 로직
//            int j = 1;
//            for (int i = winnerListSize; i < GameUsers.size(); i++) {
//                GameUser entity = GameUsers.get(i);
//                User user = userRepository.findById(entity.getUserId())
//                        .orElseThrow(() -> new UserNotFoundException(entity.getUserId()));
//
//                int loseUserScore = -1 * (int) Math.round(getWinnerMaxScore * Math.pow(0.8, loserListSize - j++));
//
//                UserScoreUpdateRequestDto dto = UserScoreUpdateRequestDto.builder()
//                        .soloScore(loseUserScore)
//                        .totalScore(loseUserScore)
//                        .teamScore(0)
//                        .build();
//
//                user.updateScore(dto);
//                userList.add(user);
//            }
//        }
//        userRepository.saveAll(userList);
//    }
    public Integer updateGameRoundCnt(String gameCode, Boolean flag) {
        Game game = gameRepository.findById(gameCode)
                .orElseThrow(() -> new GameNotFoundException(gameCode));
        if (flag) {
//            curRound 1로 초기화
            /* TODO: initCurRounds() 로 추가 및 수정 필요 */
//            response = game.updateCurRound();
        } else {
//        마지막 라운드라면
            if (game.getCurRounds() >= game.getMaxRounds()) {
                return -1;
            }
            game.increaseCurRounds();
        }
        gameRepository.save(game);
        return game.getCurRounds();
    }
    @Override
    @Transactional
    public Game updateGameIsStarted(String gameCode, Boolean isStarted) {
        Game gameEntity = gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException(gameCode));

        gameEntity = gameEntity.updateIsStarted(isStarted);
        gameRepository.save(gameEntity);
        return gameEntity;
    }
    @Override
    @Transactional
    public GameResponseDto deleteGame(String code) {
        Game game = gameRepository.findByCode(code).orElseThrow(
                () -> new GameNotFoundException(code));
        List<GameUser> gameUsers = gameUserRepository.findAllByGameCode(code).orElseThrow(
                () -> new GameUserNotFoundException("No entities exists by gameCode: " + code));

        gameUserRepository.deleteAll(gameUsers);
        gameRepository.delete(game);
        return new GameResponseDto(game);
    }

    /* FIXME 1: 퇴장 조건을 GameService 에서 판단하고,
        GameService 에서 deleteGameUser() 를 호출하는 것이 나아보임 */
    /* FIXME 2: 혹은 단순 deleteGameUser API 호출을 통해 처리하고,
        WebSocket 통신이 끊어지는 경우에도, deleteGameUser()를 호출하는 것이 나아보임*/
    // 게임 나갈때
    @Override
    @Transactional
    public String exitGame(String gameCode) {
        User user = userService.loadUser();
        Long userId = user.getId();
        GameUser gameUser = gameUserRepository.findByGameCodeAndUserId(gameCode, userId)
                .orElseThrow(() -> new GameUserNotFoundException("No entity exist by gameCode, userId"));
        Game game = gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException(gameCode));

        String gameUserCode = gameUser.getCode();

        if (game.getCurPlayers() == 1) {// 방에 방장 혼자였다면
            gameRepository.delete(game);// 방 자체를 지움
        } else {
            game.decreaseCurPlayers();// 방 현재 인원 수를 줄임
            gameRepository.save(game);
            if (gameUser.getIsHost()) {// 나가는 유저가 방장이라면
                List<GameUser> userList = gameUserRepository.findAllByGameCode(game.getCode())
                        .orElseThrow(() -> new GameUserNotFoundException("No entities exists by gameId"));// 방 안에 있는 유저 목록 가져와서
                for (GameUser entity : userList) {
                    if (!entity.getIsHost()) {// 방장이 아닌 놈을 찾아서 방장 권한을 준다
                        entity.updateIsHost(true);
                        gameUserRepository.save(entity);
                        break;
                    }
                }
            }
        }
        gameUserRepository.delete(gameUser);

        return gameUserCode;
    }

    @Override
    @Transactional
    public String exitGameByUserId(Long userId, String gameCode) {
        Game game = gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException("No entity exist by code!"));
        GameUser gameUser = gameUserRepository.findByGameCodeAndUserId(gameCode, userId)
                .orElseThrow(() -> new GameUserNotFoundException("No entity exist by gameCode, userId!"));
        String gameUserCode = gameUser.getCode();

        if (game.getCurPlayers() == 1) {// 방에 방장 혼자였다면
            gameRepository.delete(game);// 방 자체를 지움
        } else {
            game.decreaseCurPlayers();// 방 현재 인원 수를 줄임
            gameRepository.save(game);
            if (gameUser.getIsHost()) {// 나가는 유저가 방장이라면
                List<GameUser> userList = gameUserRepository.findAllByGameCode(gameCode)
                        .orElseThrow(() -> new GameUserNotFoundException("No entities exists by gameCode!"));// 방 안에 있는 유저 목록 가져와서
                for (GameUser entity : userList) {
                    if (!entity.getIsHost()) {// 방장이 아닌 놈을 찾아서 방장 권한을 준다
                        entity.updateIsHost(true);
                        gameUserRepository.save(entity);
                        break;
                    }
                }
            }
        }
        gameUserRepository.delete(gameUser);
        return gameUserCode;
    }

    @Override
    @Transactional
    public Boolean createGameQuiz(String gameCode) {
        User user = userService.loadUser();
        GameUser gameUser = gameUserRepository.findByUserId(user.getId())
                .orElseThrow(() -> new GameUserNotFoundException("No entities exists by userId"));
        if (!gameUser.getIsHost()) return false;

        Game game = gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException(gameCode));
        List<Quiz> quizList = createQuizListByStyle(game.getStyle(), game);
//        각 quiz마다 4가지 문제가 있어야함
        List<GameQuizEntity> gameQuizEntityList = createGameQuizListByMode(game, game.getMode(), quizList);

        gameQuizRepository.saveAll(gameQuizEntityList);
        return true;
    }

    private List<GameQuizEntity> createGameQuizListByMode(Game gameEntity, Integer gameType, List<Quiz> quizList) {
        List<GameQuizEntity> mainGameQuizList = new ArrayList<>();
        switch (gameType) {
            case 1 -> mainGameQuizList = choiceService.createGameQuizList(gameEntity, gameType, quizList);
            case 2 -> mainGameQuizList = blankChoiceService.createGameQuizList(gameEntity, gameType, quizList);
            case 4 -> mainGameQuizList = blankSubjectiveService.createGameQuizList(gameEntity, gameType, quizList);
            case 3, 5, 6, 7 -> mainGameQuizList = randomCreateGameQuizList(gameEntity, gameType, quizList);
        }

        return mainGameQuizList;
    }

    private List<GameQuizEntity> randomCreateGameQuizList(Game gameEntity, Integer gameType, List<Quiz> quizList) {
        List<GameQuizEntity> response = new ArrayList<>();
        Random random = new Random();
        int curRound = 1;

        if (gameType == 3) {
            for (Quiz quiz : quizList) {
                int randomMode = random.nextInt(3) + 1;
                if (randomMode == 1) response.addAll(choiceService.createGameQuiz(gameEntity, quiz, curRound));
                if (randomMode == 2) response.addAll(blankChoiceService.createGameQuiz(gameEntity, quiz, curRound));
                curRound++;
            }
        } else if (gameType == 5) {
            for (Quiz quiz : quizList) {
                int randomMode = random.nextInt(3) + 1;
                if (randomMode == 1) response.addAll(choiceService.createGameQuiz(gameEntity, quiz, curRound));
                if (randomMode == 2) response.add(blankSubjectiveService.createGameQuiz(gameEntity, quiz, curRound));
                curRound++;
            }
        } else if (gameType == 6) {
            for (Quiz quiz : quizList) {
                int randomMode = random.nextInt(3) + 1;
                if (randomMode == 1) response.addAll(blankChoiceService.createGameQuiz(gameEntity, quiz, curRound));
                if (randomMode == 2) response.add(blankSubjectiveService.createGameQuiz(gameEntity, quiz, curRound));
                curRound++;
            }
        } else if (gameType == 7) {
            for (Quiz quiz : quizList) {
                int randomMode = random.nextInt(4) + 1;
                if (randomMode == 1) response.addAll(choiceService.createGameQuiz(gameEntity, quiz, curRound));
                if (randomMode == 2) response.addAll(blankChoiceService.createGameQuiz(gameEntity, quiz, curRound));
                if (randomMode == 3) response.add(blankSubjectiveService.createGameQuiz(gameEntity, quiz, curRound));
                curRound++;
            }
        }
        return response;
    }

    private List<Quiz> createQuizListByStyle(String gameStyle, Game gameEntity) {
        List<Quiz> quizList;
        if (gameStyle.equals("random")) {
            quizList = quizRepository.extractRandomQuizzes(gameEntity.getMaxRounds())
                    .orElseThrow(() -> new QuizNotFoundException("No entities exists by random!"));
        } else {
            quizList = quizRepository.extractRandomQuizzesByStyle(gameStyle, gameEntity.getMaxRounds())
                    .orElseThrow(() -> new QuizNotFoundException("No entities exists by style!"));
        }
        return quizList;
    }
    /* FIXME: GameService 로 이동하는 것이 나아보임 */
    // 이긴 팀 점수 계산
//    public void winnerTeamScore(List<User> userList, List<GameUser> winnerTeamEntity, int getWinnerMaxScore) {
//        for (int i = 0; i < winnerTeamEntity.size(); i++) {
//            GameUser entity = winnerTeamEntity.get(i);
//            User user = userRepository.findById(entity.getUserId())
//                    .orElseThrow(() -> new UserNotFoundException(entity.getUserId()));
//
//            int earnUserScore = (int) Math.round(getWinnerMaxScore * Math.pow(0.8, i));
//
//            UserScoreUpdateRequestDto dto = UserScoreUpdateRequestDto.builder()
//                    .soloScore(0)
//                    .totalScore(earnUserScore)
//                    .teamScore(earnUserScore)
//                    .build();
//
//            user.updateScore(dto);
//            userList.add(user);
//        }
//    }

    /* FIXME: GameService 로 이동하는 것이 나아보임 */
//    public void loserTeamScore(List<User> userList, List<GameUser> loserTeamEntity, int getWinnerMaxScore) {
//        //            점수 잃는 놈들 로직
//        int j = 1;
//        for (int i = 0; i < loserTeamEntity.size(); i++) {
//            GameUser entity = loserTeamEntity.get(i);
//            User user = userRepository.findById(entity.getUserId())
//                    .orElseThrow(() -> new UserNotFoundException(entity.getUserId()));
//
//            int loseUserScore = -1 * (int) Math.round(getWinnerMaxScore * Math.pow(0.8, loserTeamEntity.size() - j++));
//
//            UserScoreUpdateRequestDto dto = UserScoreUpdateRequestDto.builder()
//                    .soloScore(loseUserScore)
//                    .totalScore(loseUserScore)
//                    .teamScore(0)
//                    .build();
//
//            user.updateScore(dto);
//            userList.add(user);
//        }
//    }
}
