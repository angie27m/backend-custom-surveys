package com.acsendo.api.survey.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.survey.dto.QuestionDTO;
import com.acsendo.api.survey.dto.SurveyResponseDTO;
import com.acsendo.api.survey.service.ResponseService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
@Api(value = "Core de respuesta de encuestas", description = "Servicios para respuestas")
public class ResponseQuestionController {
	
	private static final String MAIN_PATH_SURVEY = "/survey/{surveyId}";
	private static final String RESPONSES_OBJECT = "/responses";
	private static final String LIST = "/list";
	private static final String SAVE = "/save";
	private static final String DELETE = "/delete";
	
	
	@Autowired
	private ResponseService responseService;

	// Servicios que se van a implementar
	private static final String POST_SAVE_RESPONSES = MAIN_PATH_SURVEY + RESPONSES_OBJECT + SAVE + LIST;
	private static final String DELETE_RESPONSES = MAIN_PATH_SURVEY + RESPONSES_OBJECT + DELETE + LIST;
	
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Salvar todas las respuestas de una encuesta", notes = "Se guarda un response por cada ocpion de respuesta seleccionada o digitada. (una pregunta ", 
	response = QuestionDTO.class, httpMethod = "POST")
	@PostMapping(POST_SAVE_RESPONSES)
	public ResponseEntity<Boolean> postSaveResponseToSurvey(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId, @RequestBody SurveyResponseDTO surveyResponseDTO){
		try {
			return ResponseEntity.ok(responseService.postSaveResponseToSurvey(surveyId, surveyResponseDTO));
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save question survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Reinicia las respuestas de una encuesta", notes = "Elimina todas las respuestas de una encuesta y coloca en estado 'SENDED' a todos los participantes", response = Boolean.class, httpMethod = "DELETE")
	@DeleteMapping(DELETE_RESPONSES)
	public ResponseEntity<Boolean> deleteResponses(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId){
		try {
			return ResponseEntity.ok(responseService.deleteResponsesService(surveyId));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save delete survey", e);
		}
	}
	
	

}
