package com.acsendo.api.survey.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.survey.dto.QuestionDTO;
import com.acsendo.api.survey.dto.ResponseDTO;
import com.acsendo.api.survey.dto.ResultsExtraFieldsDTO;
import com.acsendo.api.survey.dto.ResultsGeneralSurveyDTO;
import com.acsendo.api.survey.dto.SurveyDTO;
import com.acsendo.api.survey.dto.SurveySummaryDTO;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.acsendo.api.survey.service.ResponseService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
public class ResultsSurveyController {
	
	private static final String MAIN_PATH_SURVEY = "/survey";
	private static final String PATH_RESULTS = "/results";
	private static final String SURVEY_ID = "/{surveyId}";
	private static final String PATH_GENERAL_RESULTS = "/generalResults";
	private static final String PATH_RESULTS_DETAIL = "/resultsDetails";
	private static final String PATH_RESULTS_DETAIL_PAGE = "/resultsDetailsPage";
	private static final String PATH_RESULTS_BY_QUESTIONS_BY_SURVEY = "/resultsByQuestions";
	private static final String PATH_EXTRA_FIELDS_EMPLOYEES = "/extraFieldsEmployee";
	
	private static final String GET_RESULTS_GENERALS = MAIN_PATH_SURVEY + PATH_RESULTS + SURVEY_ID + PATH_GENERAL_RESULTS;
	private static final String GET_RESULTS_DETAILS = MAIN_PATH_SURVEY + PATH_RESULTS + SURVEY_ID + PATH_RESULTS_DETAIL;
	private static final String GET_RESULTS_DETAILS_PAGE = MAIN_PATH_SURVEY + PATH_RESULTS + SURVEY_ID + PATH_RESULTS_DETAIL_PAGE;
	private static final String GET_RESULTS_BY_QUESTIONS_BY_SURVEY = MAIN_PATH_SURVEY + PATH_RESULTS + SURVEY_ID + PATH_RESULTS_BY_QUESTIONS_BY_SURVEY;
	private static final String POST_RESULTS_EXTRA_FIELDS_EMPLOYEES = MAIN_PATH_SURVEY + PATH_RESULTS + SURVEY_ID + PATH_EXTRA_FIELDS_EMPLOYEES;
	
	@Autowired
	private ResponseService responseService;
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener el resultado de respuestas, preguntas y tiempo promedio de respuestas de una encuesta", notes = "Recupera el número de respuestas, número de preguntas y el tiempo promedio"
			+ "de respuesta de una encuesta", 
	response = ResultsGeneralSurveyDTO.class, httpMethod = "GET")
	@GetMapping(GET_RESULTS_GENERALS)
	public ResponseEntity<ResultsGeneralSurveyDTO> getGeneralResults(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId) throws SurveyException{
		try {
			return ResponseEntity.ok(responseService.getGeneralResultsService(surveyId));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get results general survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene los resultados por cada una de las preguntas de una encuesta", notes = "Obtiene los resultados por cada una de las preguntas de una encuesta para resultados generales e individuales", 
	response = QuestionDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_RESULTS_DETAILS)
	public ResponseEntity<List<QuestionDTO>> getResultsDetailsByQuestionsOfSurvey(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId) throws SurveyException{
		try {
			return ResponseEntity.ok(responseService.getResultsDetailsByQuestionsOfSurveyService(surveyId));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get results details survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Paginar resultados de encuestador", notes = "Se listan las preguntas con sus respuestas segun los index enviados.", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_RESULTS_DETAILS_PAGE)
	public ResponseEntity<PageableResponse<QuestionDTO>> getResultsDetailsByQuestionsOfSurveyPaginator(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId, Pageable pageable) throws SurveyException{
		
		try {
			return ResponseEntity.ok().body(responseService.getResultsDetailsByQuestionsOfSurveyServicePaginator(surveyId, pageable));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error list results pageable", e);
		} 
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene las respuestas por cada una de las preguntas de una encuesta", notes = "Obtiene las respuestas por cada una de las preguntas de una encuesta para resultados generales e individuales", 
	response = QuestionDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_RESULTS_BY_QUESTIONS_BY_SURVEY)
	public ResponseEntity<List<ResponseDTO>> getResponsesByQuestionsBySurveyId(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId) throws SurveyException{
		try {
			return ResponseEntity.ok(responseService.getResponsesByQuestionsBySurveyIdService(surveyId));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get results details survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Campos extra para participantes que respondieron", notes = "Obtiene los campos extra de cada empleado que ya respondió a una encuesta", httpMethod = "POST")
	@PostMapping(POST_RESULTS_EXTRA_FIELDS_EMPLOYEES)
	public ResponseEntity<ResultsExtraFieldsDTO> postExtraFieldsPartakersResponses(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId, 
			@ApiParam(name="companyId", value="Id de la compañía", required=true) @RequestParam long companyId, 
			@RequestBody SurveyDTO surveyPartakers) throws SurveyException{
		try {
			return ResponseEntity.ok(responseService.postExtraFieldsPartakersResponsesService(surveyId, companyId, surveyPartakers.getPartakers()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get results details extra fields partakers", e);
		}
	}
	
	

}
