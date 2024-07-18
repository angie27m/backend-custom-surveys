package com.acsendo.api.survey.climate.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.climate.dto.ClimateConfigurationDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationFilterDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationTemplateContentDTO;
import com.acsendo.api.climate.dto.ClimateModelDTO;
import com.acsendo.api.hcm.exception.ResourceNotFoundException;
import com.acsendo.api.survey.climate.service.ClimateCreationService;
import com.acsendo.api.survey.climate.service.ClimateEvaluationEmployeeService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
@RequestMapping(ClimateEvaluationDetailController.MAIN_PATH)
@Api(value = "Detalles de modelos y evaluacion de clima")
public class ClimateEvaluationDetailController {

	public static final String MAIN_PATH = "/survey/climate/{companyId}";
	public static final String MODELS = "/models";
	public static final String MODEL_DETAIL = "/model/{modelId}/detailCount";
	public static final String FORM_EVALUATIONS_CLIMATE = "/evaluation/form";
	public static final String SAVE_EVALUATION_CONFIGURATION = "/evaluation/configuration";
	public static final String DELETE_EVALUATION = "/evaluation/{evaluationId}";
	public static final String EVALUATIONS_CLIMATE = "/evaluations";
	public static final String EVALUATION_CLIMATE_GENERAL = "/evaluations/resume";
	public static final String EVALUATION_CLIMATE_DETAIL = "/evaluation/{modelId}/resume";
	public static final String DUPLICATE_EVALUATION_CLIMATE = "/evaluation/{evaluationId}/duplicate";
	public static final String SAVE_CLIMATE_CONFIGURATION = "/model/{modelId}/configuration";
	public static final String GET_CLIMATE_CONFIGURATION = "/model/{modelId}/configuration";
	public static final String GET_CLIMATE_LINK = "/model/{modelId}/link";
	public static final String CLIMATE_TYPES = "/model/{modelId}/types";
	public static final String CALCULATION_TYPES = "/calculations/types";
	public static final String GET_CLIMATE_DEPENDENT = "/depedent/{modelId}";

	@Autowired
	ClimateCreationService climateCreationSvc;

	@Autowired
	ClimateEvaluationEmployeeService climateEvaluationSvc;

	/**
	 * Método GET que obtiene los modelos de clima de la empresa
	 * 
	 * @param companyId id de la empresa
	 * @return List<ClimateModelDTO> lista de modelos de clima existentes de la
	 *         empresa
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene los modelos de clima de la empresa", response = ClimateModelDTO.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(MODELS)
	public List<ClimateModelDTO> getClimateModels(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId) {
		try {
			List<ClimateModelDTO> climates = new ArrayList<ClimateModelDTO>();
			climates = climateCreationSvc.getClimateModels(companyId);
			return climates;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error climates model not found", e);
		}
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene los tipos de clima que usa la evaluación", response = List.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(CLIMATE_TYPES)
	public List<String> getClimateTypes(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "Identificador del modelo de lcima", required = true) @PathVariable Long modelId) {
		try {
			List<String> evaluationTypes = new ArrayList<>();
			evaluationTypes = climateCreationSvc.getClimateEvaluationTypes(companyId, modelId);
			return evaluationTypes;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error model not found", e);
		}
	}

	/**
	 * Método GET que obtiene las evaluaciones de clima
	 * 
	 * @param companyId id de la empresa
	 * @return ClimateModelDTO detalle del modelo de clima
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene las evaluaciones de clima", response = ClimateEvaluationDTO.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(EVALUATIONS_CLIMATE)
	public List<ClimateEvaluationDTO> getEvaluationsClimate(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId) {
		try {
			List<ClimateEvaluationDTO> evaluations = new ArrayList<ClimateEvaluationDTO>();
			evaluations = climateCreationSvc.getEvaluationsClimate(companyId);
			return evaluations;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error evaluations not found", e);
		}
	}

	/**
	 * Método GET que obtiene las evaluaciones de clima, su modelo de clima y su
	 * detalle de contadores
	 * 
	 * @param companyId id de la empresa
	 * @return ClimateModelDTO detalle del modelo de clima
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtieneel detalle del modelo de clima", response = ClimateEvaluationDTO.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(EVALUATION_CLIMATE_DETAIL)
	public ClimateEvaluationDTO getEvaluationsClimateDetail(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "Identificador del modelo de clima", required = true) @PathVariable Long modelId) {
		try {
			ClimateEvaluationDTO evaluation = climateCreationSvc.getEvaluationsClimateDetail(modelId);
			return evaluation;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error evaluations not found", e);
		}
	}

	/**
	 * Método GET que obtiene las evaluaciones de clima, su modelo de clima y su
	 * detalle de contadores
	 * 
	 * @param companyId id de la empresa
	 * @return ClimateModelDTO detalle del modelo de clima
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtieneel detalle del modelo de lcima", response = ClimateEvaluationDTO.class, httpMethod = "POST", responseContainer = "DTO")
	@PostMapping(EVALUATION_CLIMATE_GENERAL)
	public ResponseEntity<Page<ClimateEvaluationDTO>> getEvaluationsClimate(Pageable pageable,
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@RequestBody ClimateEvaluationFilterDTO filterDTO) {
		try {
			Page<ClimateEvaluationDTO> evaluations = null;
			evaluations = climateCreationSvc.getEvaluationsClimateGeneral(companyId, pageable, filterDTO);
			return new ResponseEntity<Page<ClimateEvaluationDTO>>(evaluations, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error can't duplicate evaluation", e);
		}
	}

	/**
	 * 5. Eliminar un competence level de una competencia. Los competence Level se
	 * eliminan, al igual que los comportamietnos y labels
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "", notes = "", response = Boolean.class, httpMethod = "DELETE")
	@DeleteMapping(DELETE_EVALUATION)
	public boolean deleteCompetenceFull(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluacion", required = true) @PathVariable Long evaluationId) {
		try {
			return climateCreationSvc.deleteEvaluation(evaluationId);
		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "competence not found for this Id of Model ", e);
		}
	}

	/**
	 * Método GET que obtiene las evaluaciones de clima, su modelo de clima y su
	 * detalle de contadores
	 * 
	 * @param companyId id de la empresa
	 * @return ClimateModelDTO detalle del modelo de clima
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Duplica una evaluacion de clima a a partir de su Id", httpMethod = "PUT")
	@GetMapping(DUPLICATE_EVALUATION_CLIMATE)
	public ClimateEvaluationTemplateContentDTO duplicateEvaluationClimate(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "evaluationId", value = "Identificador de la evaluacion de clima", required = true) @PathVariable Long evaluationId) {
		try {
			return climateCreationSvc.duplicateEvaluationClimateByDTO(companyId, evaluationId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error can't duplicate evaluation", e);
		}
	}

	/**
	 * Método GET que obtiene las evaluaciones de clima, su modelo de clima y su
	 * detalle de contadores
	 * 
	 * @param companyId id de la empresa
	 * @return ClimateModelDTO detalle del modelo de clima
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene el link  anonimo", httpMethod = "GET")
	@GetMapping(GET_CLIMATE_LINK)
	public String generateLinkSurveyClimate(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "Identificador del modelo de clima", required = true) @PathVariable Long modelId) {
		try {
			return climateEvaluationSvc.generateLinkSurveyClimate(modelId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error can't duplicate evaluation", e);
		}
	}

	/**
	 * Método POst que guarda las configuracion de evaluaciones de clima, su modelo
	 * de clima y su detalle de contadores
	 * 
	 * @param companyId id de la empresa
	 * @return ClimateModelDTO detalle del modelo de clima
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Registra la configuracion de una evaluacion de clima", httpMethod = "PUT")
	@PutMapping(SAVE_CLIMATE_CONFIGURATION)
	public ClimateConfigurationDTO saveClimateConfiguration(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "config", value = "configuracion del modelo de clima", required = true) @RequestBody ClimateConfigurationDTO config) {
		try {
			return climateCreationSvc.saveClimateConfiguration(companyId, config);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error can't save configuration", e);
		}
	}

	/**
	 * Método POst que guarda las configuracion de evaluaciones de clima, su modelo
	 * de clima y su detalle de contadores
	 * 
	 * @param companyId id de la empresa
	 * @return ClimateModelDTO detalle del modelo de clima
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Registra la configuracion de una evaluacion de clima", httpMethod = "GET")
	@GetMapping(GET_CLIMATE_CONFIGURATION)
	public ClimateConfigurationDTO getClimateConfiguration(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "configuracion id del modelo de clima", required = true) @PathVariable Long modelId) {
		try {
			return climateCreationSvc.getClimateConfiguration(companyId, modelId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error can't find configuration", e);
		}
	}

	/**
	 * Método que obtiene los tipos de cálculo para resultados de enps
	 * 
	 * @param companyId id de la empresa
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Método que obtiene los tipos de cálculo para resultados de enps", httpMethod = "GET")
	@GetMapping(CALCULATION_TYPES)
	public Map<Long, String> getCalculationTypesEnps(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId) {
		try {
			return climateCreationSvc.getCalculationTypesEnps();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error can't find calculations types", e);
		}
	}
	/**
	 * Método que obtiene los tipos de cálculo para resultados de enps
	 * 
	 * @param companyId id de la empresa
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Método que encuentra si la encuesta tiene preguntas dependientes ", httpMethod = "GET")
	@GetMapping(GET_CLIMATE_DEPENDENT)
	public Boolean getClimateDependent(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "Identificador del modelo de clima de la encuesta", required = true) @PathVariable Long modelId) {
		try {
			return climateCreationSvc.getClimateDependent(companyId, modelId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error can't find survey", e);
		}
	}

}
