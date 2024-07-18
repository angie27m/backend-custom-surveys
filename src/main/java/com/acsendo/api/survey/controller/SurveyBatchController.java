package com.acsendo.api.survey.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.acsendo.api.hcm.dto.BatchProcessResult;
import com.acsendo.api.survey.dto.SurveySummaryDTO;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.acsendo.api.survey.service.SurveyCoreService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
@Api(value = "Batch de Encuestas", description = "Servicios para envio de las encuestas (emails)")
public class SurveyBatchController {
	
	private static final String MAIN_PATH_SURVEY = "/survey";
	private static final String SURVEY_ID = "/{surveyId}";
	private static final String START_SURVEY = "/startSurvey";
	private static final String PARTAKER_ID = "/{partakerId}";
	
	private static final String GET_SEND_SURVEY = MAIN_PATH_SURVEY + SURVEY_ID + START_SURVEY;
	private static final String GET_SEND_SURVEY_PARTAKER_ID = MAIN_PATH_SURVEY + SURVEY_ID + START_SURVEY + PARTAKER_ID;
	
	@Autowired
	private SurveyCoreService surveyCoreService;
			
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Recuperar la lista de participantes y enviar los correos de participación", notes = "Enviar correos estandar para responder encuesta", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_SEND_SURVEY)
	public BatchProcessResult startSendEmailToPartakers(@ApiParam(name="surveyId", value="id de la encuesta", required=true) @PathVariable Long surveyId) throws SurveyException{
		BatchProcessResult result = null;
		try {
			result = surveyCoreService.startSendEmailToPartakers(surveyId);
		} catch (Exception e) {
			result = BatchProcessResult.newERRORBatchProcessResult(e);
		}
		return result;
		
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Envia el correo de participación a un participante en específico", notes = "Enviar correo a participante en específico para responder encuesta", 
	response = SurveySummaryDTO.class, httpMethod = "GET")
	@GetMapping(GET_SEND_SURVEY_PARTAKER_ID)
	public BatchProcessResult sendEmailToPartakerSpecific(@ApiParam(name="surveyId", value="id de la encuesta", required=true) @PathVariable Long surveyId,
			@ApiParam(name="partakerId", value="id del participante", required=true) @PathVariable Long partakerId) throws SurveyException{
		BatchProcessResult result = null;
		try {
			result = surveyCoreService.sendEmailToPartakerSpecificById(surveyId, partakerId);
		} catch (Exception e) {
			result = BatchProcessResult.newERRORBatchProcessResult(e);
		}
		return result;
		
	}


}