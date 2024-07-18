package com.acsendo.api.survey.climate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.climate.dto.ClimatePartakerDTO;
import com.acsendo.api.climate.dto.ClimatePartakerFilterDTO;
import com.acsendo.api.hcm.dto.EmployeeDTO;
import com.acsendo.api.survey.climate.service.ClimatePartakerService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
@RequestMapping(ClimatePartakerController.MAIN_PATH)
@Api(value = "Tabla de detalles de evaluados de clima")
public class ClimatePartakerController {
	public static final String MAIN_PATH = "/survey/climate/{companyId}";
	public static final String PARTAKERS_LIST = "/partakers/{modelId}";
	public static final String PARTAKERS_DETAILS = PARTAKERS_LIST + "/details";
	public static final String PARTAKERS_JOBS = PARTAKERS_LIST + "/jobs";
	public static final String PARTAKERS_REBOOT = PARTAKERS_LIST + "/reboot/{partakerId}";
	public static final String GET_EXCEL_REPORT = PARTAKERS_LIST + "/report";

	@Autowired
	ClimatePartakerService climatePartakerSvc;

	@ApiOperation(value = "Obtiene los detalles de los evaluados de una encuesta de clima", response = String[].class, responseContainer = "List")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(PARTAKERS_DETAILS)
	public Page<ClimatePartakerDTO> getPartakersDetail(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "Identificador del modelo de clima ", required = true) @PathVariable Long modelId,
			@RequestBody(required = false) ClimatePartakerFilterDTO filters,
			@ApiParam(name = "page", value = "Paginación", required = true) Pageable page) {
		try {
			return climatePartakerSvc.getPartakersDetail(companyId, modelId, filters, page);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "partakers not found", e);
		}
	}
	
	@ApiOperation(value = "Obtiene los cargos de los evaluados de una encuesta de clima", response = String[].class, responseContainer = "List")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(PARTAKERS_JOBS)
	public List<ClimatePartakerFilterDTO> getPartakersJobs(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "Identificador del modelo de clima ", required = true) @PathVariable Long modelId) {
		try {
			return climatePartakerSvc.getPartakersJobs(companyId, modelId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "partakers jobs not found", e);
		}
	}
	
	@ApiOperation(value = "Obtiene los evaluados de una encuesta de clima", response = String[].class, responseContainer = "List")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(PARTAKERS_LIST)
	public List<EmployeeDTO> getAvailablePartakers(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "Identificador del modelo de clima ", required = true) @PathVariable Long modelId) {
		try {
			return climatePartakerSvc.getAvailablePartakers(companyId, modelId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "partakers jobs not found", e);
		}
	}
	
	@ApiOperation(value = "reinicia la encuesta de clima de un participante", response = String[].class, responseContainer = "Boolean")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PutMapping(PARTAKERS_REBOOT)
	public Boolean rebootPartakerSurvey(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "Identificador del modelo de clima ", required = true) @PathVariable Long modelId,
			@ApiParam(name = "partakerId", value = "Identificador del empleado", required = true) @PathVariable Long partakerId) {
		try {
			return climatePartakerSvc.rebootPartakerSurvey(companyId, partakerId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "partakers or survey not found", e);
		}
	}
	
	/**
	 * Obtiene reporte excel de los participantes de una evaluación de clima con su detalle
	 */
	@ApiOperation(value = "Obtiene reporte excel con los detalles de los evaluados de una encuesta de clima", response = String[].class, responseContainer = "List")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(GET_EXCEL_REPORT)
	public ResponseEntity<byte[]> getClimatePartakersExcelReport(
			@ApiParam(name = "companyId", value = "Identificador de la empresa", required = true) @PathVariable Long companyId,
			@ApiParam(name = "modelId", value = "Identificador del modelo de clima ", required = true) @PathVariable Long modelId,
			@RequestBody(required = false) ClimatePartakerFilterDTO filters){
		try {
			byte[] excelReport = climatePartakerSvc.getClimatePartakersExcelReport(companyId, modelId, filters);			
			return new ResponseEntity<byte[]>(excelReport, HttpStatus.OK);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	} 
}
