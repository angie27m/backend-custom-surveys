package com.acsendo.api.survey.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.survey.dto.PartakerDTO;
import com.acsendo.api.survey.dto.SurveyDTO;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.acsendo.api.survey.service.PartakerService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
@Api(value = "Core de participantes", description = "Servicios para la adminsitracion de partcipantes de la encuesta")
public class PartakerController {
	
	private static final String MAIN_PATH_SURVEY = "/survey/{surveyId}";
	private static final String PARTAKERS = "/partakers";
	private static final String PARTAKER_ID = "/{partakerId}";
	private static final String SAVE = "/save";
	private static final String DELETE = "/delete";
	private static final String LIST_OBJECT = "/list";
	private static final String PAGE = "/page";
	
	@Autowired
	private PartakerService partakerService;

	// Servicios que se van a implementar
//	/survey/{surveyId}/partakers/save
	private static final String POST_PARTAKERS_SAVE = MAIN_PATH_SURVEY + PARTAKERS + SAVE;
//	/survey/{surveyId}/partakers/{partakerId}/delete
	private static final String DELETE_PARTAKER = MAIN_PATH_SURVEY + PARTAKERS + PARTAKER_ID + DELETE;
	private static final String GET_LIST_PARTAKER_RESPONSES = MAIN_PATH_SURVEY + PARTAKERS + LIST_OBJECT;
	private static final String GET_LIST_PARTAKER_SURVEY_PAGINATOR = MAIN_PATH_SURVEY + PARTAKERS + LIST_OBJECT + PAGE;
	
	
	
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Salvar los participantes a la encuesta", notes = "se envia la data para salvar los participantes de la encuesta, externo o interno", 
	response = PartakerDTO.class, httpMethod = "POST", responseContainer = "List")
	@PostMapping(POST_PARTAKERS_SAVE)
	public ResponseEntity<SurveyDTO> savePartakerFor(@ApiParam(name="surveyId", value="encuesta Id", required=true) @PathVariable Long surveyId, 
													   @ApiParam(name="surveyPartakers", value="objeto survey que trae los participantes nuevos para guardar", required=true) @RequestBody SurveyDTO surveyPartakers) throws SurveyException{
		try {
			return ResponseEntity.ok(partakerService.savePartakerFor(surveyId, surveyPartakers.getPartakers()));
		} catch (SurveyException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error salvar participacion", e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error salvar participacion", e);
		}
		
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Remover una particpante de la encuesta", notes = "se elimina la participacion, validando si ya tiene respuestas o no.", 
	response = Boolean.class, httpMethod = "DELETE")
	@DeleteMapping(DELETE_PARTAKER)
	public ResponseEntity<Boolean> deletePartakerFor(@ApiParam(name="surveyId", value="encuesta Id", required=true) @PathVariable Long surveyId, 
			   										 @ApiParam(name="partakerId", value="objeto del participante de la encuestas", required=true) @PathVariable Long partakerId) throws SurveyException{
		try {
			return ResponseEntity.ok(partakerService.deletePartakerFrom(surveyId, partakerId));
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error salvar participacion", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error salvar participacion", e);
		}
		
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene la lista de participantes que ya han respondido la encuesta", notes = "Obtiene la lista de participantes que ya han respondido la encuesta segun el id de la encuesta", 
	response = Long.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_LIST_PARTAKER_RESPONSES)
	public ResponseEntity<List<PartakerDTO>> getListPartakerResponse(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId) throws SurveyException{
		try {
			return ResponseEntity.ok(partakerService.getListPartakerResponseService(surveyId));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get results details survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de participantes según el estado de la encuesta", notes = "Recupera los detalles generales de cada uno de los participantes de la encuesta según el estado", 
	response = PartakerDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_LIST_PARTAKER_SURVEY_PAGINATOR)
	public PageableResponse<PartakerDTO> getListSurveysCompanyByOwnerPaginator(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId
															,  @ApiParam(name="statePartaker", value="Estado de la encuesta del participante", required=true) @RequestParam String statePartaker
															,  @ApiParam(name="idPartaker", value="Id de un participante específico que se quiere buscar", required=true) @RequestParam long idPartaker
															, Pageable pageable) throws SurveyException{
		return partakerService.getListPartakersSurveyPaginator(surveyId, statePartaker, idPartaker, pageable);
	}

}
