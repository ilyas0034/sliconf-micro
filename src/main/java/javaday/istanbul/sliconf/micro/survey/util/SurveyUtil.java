package javaday.istanbul.sliconf.micro.survey.util;


import javaday.istanbul.sliconf.micro.model.event.Event;
import javaday.istanbul.sliconf.micro.survey.SurveyException;
import javaday.istanbul.sliconf.micro.survey.SurveyMessageProvider;
import javaday.istanbul.sliconf.micro.survey.model.Answer;
import javaday.istanbul.sliconf.micro.survey.model.Question;
import javaday.istanbul.sliconf.micro.survey.model.QuestionOption;
import javaday.istanbul.sliconf.micro.survey.model.Survey;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
public class SurveyUtil {

    private SurveyUtil() {}

    public static void generateQuestionIds(Survey survey) {
        survey.getQuestions().forEach(question -> {
            if (Objects.nonNull(question.getId()))
                return;
            question.setId(new ObjectId().toString());
            question.getOptions()
                    .forEach(questionOption -> questionOption.setId(new ObjectId().toString()));
        });

        survey.getQuestions().forEach(question -> {
            if (Objects.nonNull(question.getId()))
                return;
            question.setTotalVoters(0);
            question.getOptions()
                    .forEach(questionOption -> questionOption.setVoters(0));
        });
    }

    public static void updateSurveyVoteCount(Answer answer, Survey survey) {

        survey.setParticipants(survey.getParticipants() + 1);

        answer.getAnsweredQuestions().forEach((answeredQuestionId, answeredOption) -> {

            Predicate<Question> questionPredicate;
            Predicate<QuestionOption> optionPredicate;
            questionPredicate = surveyQuestion -> surveyQuestion.getId().equals(answeredQuestionId);
            optionPredicate = questionOption -> questionOption.getText().equals(answeredOption);

            survey.getQuestions()
                    .stream()
                    .filter(questionPredicate)
                    .forEach(question -> {
                        question.setTotalVoters(question.getTotalVoters() + 1);
                        question.getOptions()
                                .stream()
                                .filter(optionPredicate)
                                .forEach(questionOption -> questionOption.setVoters(questionOption.getVoters() + 1));
                    });
        });
    }

    public static void checkAnsweredQuestions(Survey survey, Answer answer, SurveyMessageProvider surveyMessageProvider) {

        answer.getAnsweredQuestions().forEach((answeredQuestionId, answeredOption) -> {
            Predicate<Question> questionPredicate;
            questionPredicate = surveyQuestion -> surveyQuestion.getId().equals(answeredQuestionId);

            if (survey.getQuestions().stream().noneMatch(questionPredicate)) {
                log.error("Question not found by id: {}", answeredQuestionId);
                String message = surveyMessageProvider.getMessage("questionCanNotFoundWithGivenId");
                throw new SurveyException(message, answeredQuestionId);
            }

            Predicate<QuestionOption> questionOptionPredicate;
            questionOptionPredicate = questionOption -> questionOption.getText().equals(answeredOption);

            survey.getQuestions()
                    .stream()
                    .filter(questionPredicate)
                    .forEach(question -> {
                        if (question.getOptions().stream().noneMatch(questionOptionPredicate)) {
                            log.error("Answer does not match with any question option: {}", answeredOption);
                            String message = surveyMessageProvider.getMessage("questionAndAnswerMismatch");
                            throw new SurveyException(message, answeredOption);
                        }
                    });
        });
    }

    public static void generateDates(Survey survey, Event event) {

        if (Objects.isNull(survey.getEndTime()))
            survey.setEndTime(String.valueOf(event.getEndDate().toEpochSecond(ZoneOffset.UTC)));

        if (Objects.isNull(survey.getStartTime())) {
            if (event.getStartDate().isAfter(LocalDateTime.now()))
                survey.setStartTime(String.valueOf(event.getStartDate().toEpochSecond(ZoneOffset.UTC)));
            else
                survey.setStartTime(String.valueOf(event.getStartDate().plusMinutes(1).toEpochSecond(ZoneOffset.UTC)));

        }
    }

}
