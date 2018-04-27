package com.ssdi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssdi.dao.QuestionRepository;
import com.ssdi.dao.QuizRepository;
import com.ssdi.dao.UserRepository;
import com.ssdi.model.Examiner;
import com.ssdi.model.Question;
import com.ssdi.model.Quiz;
import com.ssdi.model.User;
import com.ssdi.service.QuizService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/quizmania")
public class QuizController {

	@Autowired
	QuizService quizService;

	@Autowired
	QuizRepository quizRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	QuestionRepository questionRepository;

	@RequestMapping(method = RequestMethod.POST, value = "/examiner/{userid}/quiz")
	public void createQuiz(@RequestBody Quiz quiz, @PathVariable(value = "userid") int userID) throws Exception {
		System.out.println("Yay, in quiz controller!! create quiz... ");
		Optional<User> user = userRepository.findById(userID);
		if (user != null)
			quiz.setAuthor(user.get());
		else
			throw new Exception("User is unauthorized to create quiz.");
		quizService.createQuiz(quiz);
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/{userid}/viewQuizzes")
	public Collection<Quiz> getAllQuizzes(@PathVariable(value = "userid") int userID) throws Exception {
		System.out.println("In the quiz controller..., getting quizzes for user " + userID + "!");
		List<Quiz> quizzes = null;
		User user = null;
		try {
			user = userRepository.findById(userID).get();
			System.out.println("User: \n" + user.getEmail());
		} catch (NoSuchElementException e) {
			if (userID != 0) {
				throw new Exception("User is unauthorized to create quizzes");
			}
		}

		// if the user is admin, return all (pending quizzes) - need to be fixed!
		// ToDo !!!

		if (user.getUserType().equalsIgnoreCase("ADMIN")) {
			quizzes = quizRepository.findByStatusOrderByCategoryAsc("pending");
		}
		// else if the user is an examiner, return only his/her quizzes
		else if (user.getUserType().equalsIgnoreCase("Examiner")) {
			quizzes = quizRepository.findByAuthorOrderByTitleAsc(user);
		}
		// else (user is an examinee), return all the quizzes in the system
		else {
			quizzes = quizRepository.findByStatusOrderByCategoryAsc("approved");
		}

		// System.out.println(quizzes.get(0).toString());
		return quizzes;
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@Transactional
	@RequestMapping(method = RequestMethod.GET, value = "/updateStatusYes/{id}")
	public void updateQuiz(@PathVariable(value = "id") Integer quizID) {

		quizRepository.setQuizStatus("Approved", quizID);

	}

	@CrossOrigin(origins = "http://localhost:4200")
	@Transactional
	@RequestMapping(method = RequestMethod.GET, value = "/updateStatusNo/{id}")
	public void updateQuiz1(@PathVariable(value = "id") Integer quizID) {

		quizRepository.setQuizStatus("Rejected", quizID);

	}

	// @RequestMapping(value = "/updateStatusYes/{id}", method = RequestMethod.POST
	// )
	// public @ResponseBody ResponseEntity<Quiz> update(@PathVariable int id,
	// HttpServletRequest request) throws IOException
	// {
	// Quiz quiz = quizService.findById(id);
	// Quiz updatedQuiz =
	// objectMapper.readerForUpdating(quiz).readValue(request.getReader());
	// quizRepository.saveAndFlush(updatedQuiz);
	// return new ResponseEntity<>(updatedQuiz, HttpStatus.ACCEPTED);
	// }

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/{userid}/viewquiz/{id}")
	public List<Question> getQuizQuestionsById(@PathVariable(value = "userid") User userID,
			@PathVariable(value = "id") Quiz quizID) {
		System.out
				.println("In the quiz controller..., getting questions for quiz " + quizID + "(user " + userID + ")!");
		List<Question> questions = questionRepository.findByQuizIDOrderByNumberAsc(quizID);
		return questions;
	}
	
	@Transactional
	@CrossOrigin(origins = "http://localhost:4200")
	@RequestMapping(method=RequestMethod.GET, value="/quizByCategory/{id}")
	public Collection<Quiz> getQuizByCategory(@PathVariable(value = "id") String id) {

	    return quizRepository.findCategory(id);
	    }

}
