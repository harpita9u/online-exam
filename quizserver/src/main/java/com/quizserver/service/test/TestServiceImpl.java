package com.quizserver.service.test;

import com.quizserver.dto.*;
import com.quizserver.entities.Question;
import com.quizserver.entities.Test;
import com.quizserver.entities.TestResult;
import com.quizserver.entities.User;
import com.quizserver.repository.QuestionRepository;
import com.quizserver.repository.TestRepository;
import com.quizserver.repository.TestResultRepository;
import com.quizserver.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TestServiceImpl implements TestService{
    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private UserRepository userRepository;

    public TestDTO createTest(TestDTO dto){
        Test test = new Test();
        test.setTitle(dto.getTitle());
        test.setDescription(dto.getDescription());
        test.setTime(dto.getTime());
        return testRepository.save(test).getDto();
    }

    public QuestionDTO addQuestionInTest(QuestionDTO dto) {
        Optional<Test> optionalTest = testRepository.findById(dto.getTestId());
        if (optionalTest.isPresent()) {
            Question question = new Question();
            question.setTest(optionalTest.get());
            question.setQuestionText(dto.getQuestionText());
            question.setOptionA(dto.getOptionA());
            question.setOptionB(dto.getOptionB());
            question.setOptionC(dto.getOptionC());
            question.setOptionD(dto.getOptionD());
            question.setCorrectOption(dto.getCorrectOption()); // ✅ add this line
            return questionRepository.save(question).getDto();
        }
        throw new EntityNotFoundException("Test Not Found");
    }




    public List<TestDTO> getAllTests(){
        return testRepository.findAll().stream().peek(
                test -> test.setTime(test.getQuestions().size() * test.getTime())).collect(Collectors.toList())
                .stream().map(Test::getDto).collect(Collectors.toList());
    }

    public TestDetailsDTO getAllQuestionsByTest(Long id){
        Optional<Test> optionalTest = testRepository.findById(id);
        TestDetailsDTO testDetailsDTO = new TestDetailsDTO();
        if (optionalTest.isPresent()){
            TestDTO testDTO = optionalTest.get().getDto();
            testDTO.setTime(optionalTest.get().getTime() * optionalTest.get().getQuestions().size());
            testDetailsDTO.setTestDTO(testDTO);
            testDetailsDTO.setQuestions(optionalTest.get().getQuestions().stream().map(Question::getDto).toList());
            return testDetailsDTO;
        }
        return testDetailsDTO;
    }

    public TestResultDTO submitTest(SubmitTestDTO request) {
        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new EntityNotFoundException("Test not found."));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        int correctAnswers = 0;

        for (QuestionResponse response : request.getResponses()) {
            Question question = questionRepository.findById(response.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found."));

            String selected = response.getSelectedOption();
            String correct = question.getCorrectOption();

            // ✅ Handle nulls safely
            if (selected == null || correct == null) {
                System.out.println("⚠️ Skipping question " + question.getId() + " because selected or correct option is null");
                continue;
            }

            selected = selected.trim();
            correct = correct.trim();

            // ✅ Convert correct option (A/B/C/D) → actual text
            String correctText = switch (correct.toUpperCase()) {
                case "A" -> question.getOptionA();
                case "B" -> question.getOptionB();
                case "C" -> question.getOptionC();
                case "D" -> question.getOptionD();
                default -> "";
            };

            // ✅ Compare the text safely
            if (selected.equalsIgnoreCase(correctText != null ? correctText.trim() : "")) {
                correctAnswers++;
            }

            // Debug logs
            System.out.println("Question ID: " + response.getQuestionId());
            System.out.println("Correct Option Code: " + correct);
            System.out.println("Correct Text: " + correctText);
            System.out.println("Selected: " + selected);
        }

        int totalQuestions = request.getResponses().size();
        double percentage = ((double) correctAnswers / totalQuestions) * 100;

        TestResult testResult = new TestResult();
        testResult.setTest(test);
        testResult.setUser(user);
        testResult.setTotalQuestions(totalQuestions);
        testResult.setCorrectAnswers(correctAnswers);
        testResult.setPercentage(percentage);

        System.out.println("✅ Correct Answers: " + correctAnswers);
        System.out.println("✅ Total Questions: " + totalQuestions);
        System.out.println("✅ Percentage: " + percentage);

        return testResultRepository.save(testResult).getDto();
    }



    public List<TestResultDTO> getAllTestResults(){
        return testResultRepository.findAll().stream().map(TestResult::getDto).collect(Collectors.toList());
    }

    public List<TestResultDTO> getAllTestResultsOfUser(Long userId){
        return testResultRepository.findAllByUserId(userId).stream().map(TestResult::getDto).collect(Collectors.toList());
    }

}
