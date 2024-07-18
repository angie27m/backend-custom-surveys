package com.acsendo.api.survey.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.survey.dto.QuestionDTO;
import com.acsendo.api.survey.dto.ResponseOptionsDTO;
import com.acsendo.api.survey.dto.TemplateQuestionDTO;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.acsendo.api.survey.service.QuestionService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
@Api(value = "Core de preguntas de encuestas", description = "Servicios para la administración de las preguntas de una encuesta y sus detalles")
public class QuestionController {
	
	private static final String MAIN_PATH_SURVEY = "/survey";
	private static final String QUESTION_BASE = "/questionBase";
	private static final String QUESTION_CODE = "/{questionCode}";
	private static final String QUESTIONS_MENU = "/questionsMenu";
	private static final String LIST_OBJECT = "/list";
	private static final String QUESTION = "/question";
	private static final String QUESTION_ID = "/{questionId}";
	private static final String SAVE = "/save";
	private static final String DELETE = "/delete";
	private static final String CLONE = "/clone";
	private static final String SURVEY_ID = "/{surveyId}";
	private static final String RESPONSE_OPTIONS = "/responseOptions";
	
	@Autowired
	private QuestionService questionService;

	// Servicios que se van a implementar
	private static final String GET_LIST_MENU = MAIN_PATH_SURVEY + QUESTIONS_MENU + LIST_OBJECT;
	private static final String GET_QUESTION_BASE = MAIN_PATH_SURVEY + QUESTION_BASE + QUESTION_CODE;
	private static final String PUT_SAVE_QUESTION = MAIN_PATH_SURVEY + QUESTION + SAVE;
	private static final String POST_CLONE_QUESTION = MAIN_PATH_SURVEY + QUESTION + QUESTION_ID + CLONE;
	private static final String DELETE_QUESTION = MAIN_PATH_SURVEY + QUESTION + QUESTION_ID + DELETE;
	private static final String GET_QUESTION_BY_ID = MAIN_PATH_SURVEY + QUESTION + QUESTION_ID;
	private static final String GET_LIST_QUESTIONS_BY_SURVEY_ID = MAIN_PATH_SURVEY + QUESTION + LIST_OBJECT + SURVEY_ID;
	private static final String GET_LIST_RESPONSE_OPTIONS_BY_SURVEY_ID = MAIN_PATH_SURVEY + RESPONSE_OPTIONS + LIST_OBJECT + SURVEY_ID;
	
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener los detalles de una pregunta base", notes = "Recupera los detalles generales de una pregunta base según el code", 
	response = TemplateQuestionDTO.class, httpMethod = "GET")
	@GetMapping(GET_QUESTION_BASE)
	public TemplateQuestionDTO getDetailQuestionBase(@ApiParam(name="questionCode", value="Código de la pregunta base", required=true) @PathVariable String questionCode) throws SurveyException{
		return questionService.getDetailQuestionBaseService(questionCode);
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de preguntas para el menu", notes = "Recupera las preguntas para editar en la pestaña crear encuesta", 
	response = TemplateQuestionDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_LIST_MENU)
	public List<TemplateQuestionDTO> getListTemplateQuestions() throws SurveyException{
		return questionService.getListTemplateQuestionsService();
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Salvar una pregunta de una encuesta con sus opciones de respuesta", notes = "Guarda una pregunta editada o nueva junto con sus opciones de respuesta", 
	response = QuestionDTO.class, httpMethod = "PUT")
	@PutMapping(PUT_SAVE_QUESTION)
	public ResponseEntity<QuestionDTO> putSaveQuestion(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @RequestParam long surveyId, @RequestBody QuestionDTO questionObject){
		try {
			return ResponseEntity.ok(questionService.putSaveQuestionService(surveyId, questionObject));
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save question survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Clonar Pregunta", notes = "Clona una pregunta a partir de otra pasando por parámetro el id de la pregunta a clonar", 
	response = QuestionDTO.class, httpMethod = "POST")
	@PostMapping(POST_CLONE_QUESTION)
	public ResponseEntity<QuestionDTO> getCloneQuestion(@ApiParam(name="questionId", value="Id de la pregunta a clonar", required=true) @PathVariable long questionId, @RequestParam String newNameDuplicate, @RequestBody QuestionDTO questionObject) throws SurveyException, CloneNotSupportedException{
		try {
			return ResponseEntity.ok(questionService.getQuestionCloneService(questionId, newNameDuplicate, questionObject));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save clone question survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Eliminar una pregunta según el id pasado por parámetro", notes = "Realiza una eliminación lógica de una pregunta cambiando el state a DELETE", response = Boolean.class, httpMethod = "DELETE")
	@DeleteMapping(DELETE_QUESTION)
	public ResponseEntity<Boolean> deleteSurveyById(@ApiParam(name="questionId", value="Id de la pregunta", required=true) @PathVariable long questionId){
		try {
			return ResponseEntity.ok(questionService.deleteQuestionByIdService(questionId));
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error delete question", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error delete question", e);
		}
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Recuperar question por id (question.id)", notes = "Recupera la pregunta question segun el id enviado", response = QuestionDTO.class, httpMethod = "GET")
	@GetMapping(GET_QUESTION_BY_ID)
	public ResponseEntity<QuestionDTO> getQuestionById(@ApiParam(name="questionId", value="Id de la pregunta", required=true) @PathVariable long questionId){
		try {
			return ResponseEntity.ok(questionService.getQuestionById(questionId));
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error delete question", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error delete question", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Listar preguntas por encuesta", notes = "Obtiene la información general de cada una de las preguntas de una encuesta", 
	response = QuestionDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_LIST_QUESTIONS_BY_SURVEY_ID)
	public ResponseEntity<List<QuestionDTO>> getListQuestionsBySurveyId(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId) throws SurveyException{
		try {
			return ResponseEntity.ok(questionService.getListQuestionsBySurveyIdService(surveyId));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get list questions of survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Listar preguntas con sus opciones de respuesta", notes = "Obtiene las opciones de respuesta de cada una de las preguntas de una encuesta", 
	response = QuestionDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_LIST_RESPONSE_OPTIONS_BY_SURVEY_ID)
	public ResponseEntity<List<ResponseOptionsDTO>> getListResponseOptionsByQuestionsBySurvey(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId) throws SurveyException{
		try {
			return ResponseEntity.ok(questionService.getListResponseOptionsByQuestionsBySurveyService(surveyId));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get list response options of questions of survey", e);
		}
	}

}
