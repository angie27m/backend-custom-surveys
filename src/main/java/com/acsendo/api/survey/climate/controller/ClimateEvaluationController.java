package com.acsendo.api.survey.climate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.climate.dto.ClimateEvaluationDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationTemplateContentDTO;
import com.acsendo.api.hcm.exception.ResourceNotFoundException;
import com.acsendo.api.survey.climate.service.ClimateEvaluationEditService;
import com.acsendo.api.survey.climate.service.ClimateEvaluationService;
import com.acsendo.api.survey.climate.service.ClimateMigrationDemoService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
@RequestMapping(ClimateEvaluationController.MAIN_PATH)
@Api(value = "Creacion de evaluación de clima")
public class ClimateEvaluationController {
	public static final String MAIN_PATH = "/survey/laboralclimate/{companyId}";
	public static final String QUESTIONS_SAVE = "/questions/save";
	public static final String EVALUATION_SAVE = "/evaluation/save";
	public static final String EDIT_EVALUATION_QUESTIONS = "/evaluation/questions/edit";
	public static final String EVALUATION_UPDATE_DATE = "/evaluation/save/date";
	public static final String CLONE_DEMO = "/evaluations/clone";

	
	@Autowired
	ClimateEvaluationService climateEvaluationService;
	@Autowired
	ClimateEvaluationEditService climateEvaluationEditService;
	
	@Autowired
	ClimateMigrationDemoService climateMigrationDemoService;
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Guarda las preguntas de una evaluación de clima", response = ClimateEvaluationTemplateContentDTO.class)
	@PostMapping(QUESTIONS_SAVE)
	public ClimateEvaluationTemplateContentDTO saveClimateQuestions(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@RequestBody ClimateEvaluationTemplateContentDTO climateEvaluationContentDTO) {
		try {
			return this.climateEvaluationService.saveClimateQuestions(companyId, climateEvaluationContentDTO);
		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Response question climate can't save", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Guarda titulos, descripciones de la evaluacion de clima", response = ClimateEvaluationDTO.class)
	@PostMapping(EVALUATION_SAVE)
	public ClimateEvaluationDTO saveClimateEvaluation(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "userId", value = "Identificador del usuario creador de la evaluación", required = true) @RequestParam(required = true) Long userId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado creador de la evaluación", required = false) @RequestParam(required = false) Long employeeId,
			@RequestBody ClimateEvaluationDTO climateEvaluationDTO) {
		try {
			return this.climateEvaluationService.saveClimateEvaluation(climateEvaluationDTO, companyId, userId, employeeId);
		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Response question climate can't save", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Actualiza la fecha de la evaluacion", response = ClimateEvaluationDTO.class)
	@PostMapping(EVALUATION_UPDATE_DATE)
	public ClimateEvaluationDTO updateClimateDate(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@RequestBody ClimateEvaluationDTO climateEvaluationDTO) {
		try {
			return this.climateEvaluationService.saveClimateDate( climateEvaluationDTO, companyId);
		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Response question climate can't save", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Edita las preguntas de una evaluación de clima", response = ClimateEvaluationTemplateContentDTO.class, consumes = MediaType.APPLICATION_JSON_VALUE)
	@PostMapping(EDIT_EVALUATION_QUESTIONS)
	public ClimateEvaluationTemplateContentDTO editClimateQuestions(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@RequestPart ClimateEvaluationTemplateContentDTO previousContentDTO, 
			@RequestPart ClimateEvaluationTemplateContentDTO newContentDTO) {
		try {
			return this.climateEvaluationEditService.editClimateQuestions(companyId, previousContentDTO, newContentDTO);
		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Response question climate can't save", e);
		}
	}
	
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Clona el modelo de clima de una evaluación")
	@GetMapping(CLONE_DEMO)
	public void cloneDemoClimateEvaluations(
			@ApiParam(name = "companyId", value = "Identificador de la empresa base", required = true) @PathVariable Long companyId,
			@ApiParam(name = "newCompanyId", value = "Identificador de la nueva empresa", required = true) @RequestParam(required = true) Long newCompanyId,
			@ApiParam(name = "userId", value = "Identificador del usuario creador de la evaluación", required = true) @RequestParam(required = true) Long userId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado creador de la evaluación", required = false) @RequestParam(required = false) Long employeeId
		) {
		try {
			 climateMigrationDemoService.cloneEvaluationClimateAndGenerateResults(companyId, newCompanyId, userId, employeeId);
		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error al migrar evaluación de clima"+ newCompanyId, e);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error al migrar evaluación de clima"+ newCompanyId, e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error al migrar evaluación de clima "+ newCompanyId, e);
		}
	}
	
	
}
