package com.ssafy.apm.gamequiz.service;

import com.ssafy.apm.game.domain.GameEntity;
import com.ssafy.apm.game.exception.GameNotFoundException;
import com.ssafy.apm.game.repository.GameRepository;
import com.ssafy.apm.gamequiz.domain.GameQuizEntity;
import com.ssafy.apm.gamequiz.dto.response.GameQuizGetResponseDto;
import com.ssafy.apm.gamequiz.exception.GameQuizNotFoundException;
import com.ssafy.apm.gamequiz.repository.GameQuizRepository;
import com.ssafy.apm.gameuser.domain.GameUserEntity;
import com.ssafy.apm.gameuser.exception.GameUserNotFoundException;
import com.ssafy.apm.gameuser.repository.GameUserRepository;
import com.ssafy.apm.multiplechoice.domain.MultipleChoiceEntity;
import com.ssafy.apm.multiplechoice.repository.MultipleChoiceRepository;
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
import java.util.NoSuchElementException;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameQuizServiceImpl implements GameQuizService {

    private final MultipleChoiceRepository multipleChoiceRepository;
    private final GameQuizRepository gameQuizRepository;
    private final GameUserRepository gameUserRepository;
    private final GameRepository gameRepository;
    private final QuizRepository quizRepository;
    private final UserService userService;

    //    맨 앞에 있는 놈을 뽑아서 보내줌
    @Override
    public GameQuizGetResponseDto getGameQuizDetail(Long gameId) {
        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

//        현재 라운드 가져와서
        Integer round = gameEntity.getCurRound();
//        현재 라운드에 해당하는 정답 주는거
        GameQuizEntity entity = gameQuizRepository.findByGameIdAndRound(gameId, round)
                .orElseThrow(() -> new GameQuizNotFoundException("No entities exists!"));

        GameQuizGetResponseDto response = new GameQuizGetResponseDto(entity);

        return response;
    }

    @Override
    @Transactional
    public Boolean createAnswerGameQuiz(Long gameId) {
        User user = userService.loadUser();
        GameUserEntity gameUser = gameUserRepository.findByUserId(user.getId())
                .orElseThrow(() -> new GameUserNotFoundException(user.getId()));

//        방장이 아니면 게임 시작할 수 없음
        if (!gameUser.getIsHost()) {
            return false;
        }

        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        String gameStyle = gameEntity.getStyle();

        List<Quiz> quizList;

        if (gameStyle.equals("random")) {
            quizList = quizRepository.extractRandomQuizzes(gameEntity.getRounds())
                    .orElseThrow(() -> new QuizNotFoundException("No entities exists by random!"));
        } else {
            quizList = quizRepository.extractRandomQuizzesByStyle(gameStyle, gameEntity.getRounds())
                    .orElseThrow(() -> new QuizNotFoundException("No entities exists by style!"));
        }

        List<GameQuizEntity> gameQuizEntityList = new ArrayList<>();
//        게임 문제 유형
        Integer gameType = gameEntity.getType();
//        라운드 별로 문제 출제
        Integer currentRound = 1;
//        랜덤 숫자
        int randomType = 0;
        Random random = new Random();

//        문제 유형이 하나일 경우
        if (gameType == 1 || gameType == 2 || gameType == 4) {
            for (Quiz quiz : quizList) {
                GameQuizEntity entity = createGameQuizEntity(gameId, quiz.getId(), currentRound, gameType);
                currentRound += 1;
                gameQuizEntityList.add(entity);
            }
        }
//        문제 유형이 객관식, 빈칸객관식일 경우
        else if (gameType == 3) {
            for (Quiz quiz : quizList) {
                // 1 또는 2
                randomType = random.nextInt(3) + 1;
                GameQuizEntity entity = createGameQuizEntity(gameId, quiz.getId(), currentRound, randomType);
                currentRound += 1;
                gameQuizEntityList.add(entity);
            }

        }
//        문제 유형이 객관식, 빈칸주관식일 경우(1, 4)
        else if (gameType == 5) {
            int[] numbers = {1, 4}; // 선택하고 싶은 숫자들을 배열에 저장
            createGameQuizEntityList(gameId, quizList, gameQuizEntityList, currentRound, random, numbers);

        }
//          문제 유형이 빈칸객관식, 빈칸주관식일 경우(2, 4)
        else if (gameType == 6) {
            int[] numbers = {2, 4}; // 선택하고 싶은 숫자들을 배열에 저장
            createGameQuizEntityList(gameId, quizList, gameQuizEntityList, currentRound, random, numbers);
        }
//        문제 유형 랜덤일 경우
        else {
            int[] numbers = {1, 2, 4}; // 선택하고 싶은 숫자들을 배열에 저장
            createGameQuizEntityList(gameId, quizList, gameQuizEntityList, currentRound, random, numbers);

        }

        for (GameQuizEntity entity : gameQuizEntityList) {
//            객관식일 때
            if(entity.getType() == 1) {
                Quiz quiz = quizRepository.findById(entity.getQuizId())// 정답 quiz찾아
                        .orElseThrow(() -> new QuizNotFoundException(entity.getQuizId()));
                List<Quiz> quizListByGroupCode = quizRepository.extractRandomQuizzesByStyleAndGroupCode(quiz.getStyle(), quiz.getGroupCode(), 3)
                        .orElseThrow(() -> new QuizNotFoundException("No entities exists by groupCode!"));// 오답 quiz 리스트 찾아

//                정답, 오답 리스트를 받아 문제 보기 리스트를 생성하는 함수
                List<MultipleChoiceEntity> multipleChoiceEntityList = createMultipleChoiceList(entity.getId(), quiz.getId(), quizListByGroupCode);

                multipleChoiceRepository.saveAll(multipleChoiceEntityList);// 보기들 저장
            }
//            빈칸 객관식일 때
            else if(entity.getType() == 2) {

                Quiz quiz = quizRepository.findById(entity.getQuizId())
                        .orElseThrow(() -> new QuizNotFoundException(entity.getQuizId()));
                List<Quiz> randomQuizList = quizRepository.extractRandomQuizzesByStyle(quiz.getStyle(), 3) // 같은 스타일의 quiz 찾아
                        .orElseThrow(() -> new QuizNotFoundException("No entities exists by style!"));

                List<MultipleChoiceEntity> multipleChoiceEntityList = createMultipleChoiceList(entity.getId(), quiz.getId(), randomQuizList);

                multipleChoiceRepository.saveAll(multipleChoiceEntityList);
            }
//            빈칸 주관식일 때는 보기에 정답 하나만 담아
            else if(entity.getType() == 4) {
                Quiz quiz = quizRepository.findById(entity.getQuizId())
                        .orElseThrow(() -> new QuizNotFoundException(entity.getQuizId()));

                MultipleChoiceEntity answer = MultipleChoiceEntity.builder()
                        .gameQuizId(entity.getId())
                        .quizId(quiz.getId())
                        .build();

                multipleChoiceRepository.save(answer);
            }
        }

        gameQuizRepository.saveAll(gameQuizEntityList);
        return true;
    }

//    문제 보기를 만드는 함수
    private List<MultipleChoiceEntity> createMultipleChoiceList(Long gameQuizId,
                                                                Long answerQuizId,
                                                                List<Quiz> randomQuizList){
        MultipleChoiceEntity answer = MultipleChoiceEntity.builder()
                .gameQuizId(gameQuizId)
                .quizId(answerQuizId)
                .build();

        List<MultipleChoiceEntity> multipleChoiceEntityList = new ArrayList<>();

        multipleChoiceEntityList.add(answer);

        for (Quiz q : randomQuizList) {
            MultipleChoiceEntity multipleChoice = MultipleChoiceEntity.builder()
                    .gameQuizId(gameQuizId)
                    .quizId(q.getId())
                    .build();

            multipleChoiceEntityList.add(multipleChoice);
        }

        return multipleChoiceEntityList;
    }

    private void createGameQuizEntityList(Long gameId, List<Quiz> quizList, List<GameQuizEntity> gameQuizEntityList, Integer currentRound, Random random, int[] numbers) {
        int randomIndex;
        int randomType;
        for (Quiz quiz : quizList) {
            // 배열의 길이를 최대값으로 하는 랜덤 인덱스 생성
            randomIndex = random.nextInt(numbers.length);
            // 1 또는 4
            // 랜덤 인덱스를 사용하여 배열에서 하나의 숫자 선택
            randomType = numbers[randomIndex];
            GameQuizEntity entity = createGameQuizEntity(gameId, quiz.getId(), currentRound, randomType);
            currentRound += 1;
            gameQuizEntityList.add(entity);
        }
    }

    //    GameQuizEntity를 반환하는 함수
    private GameQuizEntity createGameQuizEntity(Long gameId, Long quizId, Integer round, Integer type) {
        GameQuizEntity entity = GameQuizEntity.builder()
                .gameId(gameId)
                .quizId(quizId)
                .round(round)
                .type(type)
                .build();
        return entity;
    }
}
