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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.survey.dto.ConfigModuleSurveyDTO;
import com.acsendo.api.survey.dto.ConfigSurveyDTO;
import com.acsendo.api.survey.dto.PartakerSurveyDTO;
import com.acsendo.api.survey.dto.QuestionDTO;
import com.acsendo.api.survey.dto.SurveyDTO;
import com.acsendo.api.survey.dto.SurveyResponseExcelDTO;
import com.acsendo.api.survey.dto.SurveySummaryDTO;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.acsendo.api.survey.service.SurveyCoreService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
@Api(value = "Core de Encuestas", description = "Servicios para la administracion de las encuestas y sus detalles")
public class SurveyCoreController {
	
	private static final String MAIN_PATH_SURVEY = "/survey";
	private static final String LIST_OBJECT = "/list";
	private static final String LIST_SECURE_OBJECT = "/listSecureCode";
	private static final String SURVEY_ID = "/{surveyId}";
	private static final String TEMPLATES = "/templates";
	private static final String TEMPLATES_BASE = "/templatesBase";
	private static final String SURVEY_OWNDER = "/owner/{surveyOwner}";
	private static final String OWNDER = "/owner/page/{surveyOwner}";
	private static final String BOSS_ID = "/boss/{bossId}";
	private static final String BOSS = "/boss/page/{bossId}";
	private static final String PARTAKER = "/partaker/{employeeId}";
	private static final String PARTAKERS = "/partaker/page/{employeeId}";
	private static final String SAVE = "/save";
	private static final String CLONE = "/clone";
	private static final String SETTINGS = "/settings";
	private static final String DELETE = "/delete";
	private static final String QUESTIONS = "/questions";
	private static final String CLOSE = "/close";
	private static final String CONFIGURATION_MODULE = "/configurationModule";
	private static final String COMPANY_ID = "/{companyId}";
	
	
	@Autowired
	private SurveyCoreService surveyCoreService;
	
	// Servicios que se van a implementar
	private static final String GET_LIST_SURVEYS = MAIN_PATH_SURVEY + LIST_OBJECT;
	private static final String GET_LIST_SURVEYS_OWNER = MAIN_PATH_SURVEY + LIST_OBJECT + SURVEY_OWNDER;
	private static final String GET_SURVEYS_OWNER = MAIN_PATH_SURVEY + LIST_OBJECT + OWNDER;
	private static final String GET_SURVEY = MAIN_PATH_SURVEY + LIST_OBJECT + SURVEY_ID;
	private static final String GET_SURVEY_CODE = MAIN_PATH_SURVEY + LIST_SECURE_OBJECT + SURVEY_ID;
	private static final String GET_TEMPLATES_COMPANY = MAIN_PATH_SURVEY + TEMPLATES;
	private static final String GET_TEMPLATES_BASE_ACSENDO = MAIN_PATH_SURVEY + TEMPLATES_BASE + LIST_OBJECT;
	private static final String PUT_SAVE_SURVEY = MAIN_PATH_SURVEY + SAVE;
	private static final String GET_CLONE_SURVEY = MAIN_PATH_SURVEY + SURVEY_ID + CLONE;
	private static final String DELETE_SURVEY = MAIN_PATH_SURVEY + SURVEY_ID + DELETE;
	private static final String CLOSE_SURVEY = MAIN_PATH_SURVEY + SURVEY_ID + CLOSE;
	
	
	private static final String GET_LIST_SURVEYS_BOSS = MAIN_PATH_SURVEY + BOSS_ID + LIST_OBJECT;
	private static final String GET_SURVEYS_BOSS = MAIN_PATH_SURVEY + BOSS + LIST_OBJECT;
	private static final String GET_LIST_SURVEYS_PARTAKER = MAIN_PATH_SURVEY + PARTAKER +  LIST_OBJECT;
	private static final String GET_SURVEYS_PARTAKER = MAIN_PATH_SURVEY + PARTAKERS + LIST_OBJECT;
	private static final String PUT_CONFIG_SURVEY = MAIN_PATH_SURVEY + SURVEY_ID + SETTINGS + SAVE;
	private static final String GET_QUESTIONS = MAIN_PATH_SURVEY + SURVEY_ID + QUESTIONS;
	private static final String GET_CONFIGURATION_MODULE = MAIN_PATH_SURVEY + CONFIGURATION_MODULE + COMPANY_ID;
	private static final String POST_CONFIGURATION_MODULE = MAIN_PATH_SURVEY + CONFIGURATION_MODULE + COMPANY_ID;
	public static final String GET_EXCEL_TEMPLATE = MAIN_PATH_SURVEY + "/exceltemplate";
	public static final String GET_RESPONSE_SURVEY_EXCEL = MAIN_PATH_SURVEY + "/surveyresponse";
	public static final String GET_ADD_PARTAKERS_EXCEL = MAIN_PATH_SURVEY + "/addpartakers";
	public static final String GET_PING_HEALTH_CHECK = MAIN_PATH_SURVEY + "/pingHealthCheck";

	
			
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de encuestas que tiene una compania", notes = "Recupera los detalles generales de cada una de las encuestas de una compañia", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_LIST_SURVEYS)
	public List<SurveySummaryDTO> getListSurveysCompany(@ApiParam(name="companyId", value="Id de la compañía", required=true) @RequestParam long companyId) throws SurveyException{
		return surveyCoreService.getListSurveysCompanyService(companyId);
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de encuestas que se tiene por ADMIN o BOSS", notes = "Recupera los detalles generales de cada una de las encuestas de una compañia", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_LIST_SURVEYS_OWNER)
	public List<SurveySummaryDTO> getListSurveysCompanyByOwner(@ApiParam(name="companyId", value="Id de la compania", required=true) @RequestParam long companyId
															,  @ApiParam(name="surveyOwner", value="tipo de propietario", required=true) @PathVariable String surveyOwner ) throws SurveyException{
		return surveyCoreService.getListSurveysCompanyServiceByOwner(companyId, surveyOwner);
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de encuestas que se tiene por ADMIN o BOSS Paginator", notes = "Recupera los detalles generales de cada una de las encuestas de una compañia", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_SURVEYS_OWNER)
	public PageableResponse<SurveySummaryDTO> getListSurveysCompanyByOwnerPaginator(@ApiParam(name="companyId", value="Id de la compania", required=true) @RequestParam long companyId
															,  @ApiParam(name="surveyOwner", value="tipo de propietario", required=true) @PathVariable String surveyOwner, Pageable pageable) throws SurveyException{
		return surveyCoreService.getListSurveysCompanyServiceByOwnerPaginator(companyId, surveyOwner, pageable);
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de encuestas que yo como jefe he creado", notes = "Trae la lista de encuetas que el jefe creo, no se pueden ver en el admin. solo en el perfil del jefe.", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_LIST_SURVEYS_BOSS)
	public List<SurveySummaryDTO> getListSurveysCompanyByBossId(@ApiParam(name="companyId", value="Id de la compania", required=true) @RequestParam long companyId
															,  @ApiParam(name="bossId", value="id del jefe", required=true) @PathVariable long bossId ) throws SurveyException{
		return surveyCoreService.getListSurveysCompanyServiceByBoss(companyId, bossId);
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de encuestas que yo como jefe he creado con paginador", notes = "Trae la lista de encuetas que el jefe creo, no se pueden ver en el admin. solo en el perfil del jefe.", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_SURVEYS_BOSS)
	public PageableResponse<SurveySummaryDTO> getListSurveysCompanyByBossPaginator(@ApiParam(name="companyId", value="Id de la compania", required=true) @RequestParam long companyId
															,  @ApiParam(name="bossId", value="id del jefe", required=true) @PathVariable long bossId, Pageable pageable) throws SurveyException{
		return surveyCoreService.getListSurveysCompanyServiceByBossPaginator(companyId, bossId, pageable);
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de encuestas donde el empleado esta participando", notes = "Recupera la lista de enceustas donde se tiene o tuvo participacion", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_LIST_SURVEYS_PARTAKER)
	public ResponseEntity<List<PartakerSurveyDTO>> getListSurveysCompanyByPartakerADMIN_MODUL_URL_BASE(@ApiParam(name="companyId", value="Id de la compania", required=true) @RequestParam long companyId
																,  @ApiParam(name="employeeId", value="Empleado", required=true) @PathVariable long employeeId ) throws SurveyException{
		try {
			return ResponseEntity.ok().body(surveyCoreService.getListSurveysCompanyServiceByPartakerId(companyId, employeeId));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error list partakers", e);
		} 
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener los detalles de una encuesta", notes = "Recupera una encuesta específica según el id del survey enviado como parámetro", 
	response = SurveyDTO.class, httpMethod = "GET")
	@GetMapping(GET_SURVEY)
	public SurveyDTO getSurveyById(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId) throws SurveyException{
		return surveyCoreService.getSurveyByIdService(surveyId);
	}
	

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener detalle de encesta por Secure Code (sid)", notes = "Recupera una encuesta específica según el secure Code (sid) del survey enviado como parámetro", 
	response = SurveyDTO.class, httpMethod = "GET")
	@GetMapping(GET_SURVEY_CODE)
	public SurveyDTO getSurveyBySecureCode(@ApiParam(name="surveyId", value="Secure Code de la encuesta", required=true) @PathVariable String surveyId, 
										   @ApiParam(name="partakerId", value="Id del partaker", required = false) @RequestParam long partakerId) throws SurveyException{
		try {
			return surveyCoreService.getSurveyBySecureCoreService(surveyId, partakerId);
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get survey", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get survey", e);
		}
		
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de plantillas generales de una compañía", notes = "Recupera las plantillas generales que tiene una compañía", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_TEMPLATES_COMPANY)
	public List<SurveySummaryDTO> getListTemplatesSurveysByCompany(@ApiParam(name="companyId", value="Id de la compañía", required=true) @RequestParam long companyId) throws SurveyException{
		return surveyCoreService.getListTemplatesSurveysByCompanyService(companyId);
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtener la lista de plantillas base de acsendo", notes = "Recupera las plantillas base que tiene acsendo", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_TEMPLATES_BASE_ACSENDO)
	public List<SurveySummaryDTO> getListTemplatesSurveysBaseAcsendo() throws SurveyException{
		return surveyCoreService.getListTemplatesSurveysBaseAcsendoService();
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Crear o editar una encuesta", notes = "Crea una nueva o edita una encuesta", 
	response = SurveyDTO.class, httpMethod = "PUT")
	@PutMapping(PUT_SAVE_SURVEY)
	public SurveyDTO putSaveSurvey(@ApiParam(name="companyId", value="Id de la compañía", required=true) @RequestParam long companyId, 
			@ApiParam(name = "userId", value = "Identificador del usuario creador de la encuesta", required = true) @RequestParam(required = true) Long userId,
			@ApiParam(name = "employeeId", value = "Identificador del empleado creador de la encuesta", required = false) @RequestParam(required = false) Long employeeId,
			@RequestBody SurveyDTO surveyObject) throws SurveyException{
		return surveyCoreService.putSaveSurveyService(companyId, surveyObject, userId, employeeId);
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Salvar configuracion de encuesta", notes = "Se salva unicamente la configuracion, no se salva ningun otro campo de la encuesta.", 
	response = SurveyDTO.class, httpMethod = "PUT")
	@PutMapping(PUT_CONFIG_SURVEY)
	public ResponseEntity<ConfigSurveyDTO> putConfigSurvey(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId, @RequestBody ConfigSurveyDTO configSurveyObject) throws SurveyException{
		try {
			return ResponseEntity.ok(surveyCoreService.putSaveConfigSurvey(surveyId, configSurveyObject));
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save config survey", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save config survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Clonar Encuesta", notes = "Clona una encuesta a partir de otra pasando por parámetro el id de la encuesta a clonar", 
	response = SurveyDTO.class, httpMethod = "GET")
	@GetMapping(GET_CLONE_SURVEY)
	public SurveyDTO getSurveyClone(@ApiParam(name="surveyId", value="Id de la encuesta a clonar", required=true) @PathVariable long surveyId) throws SurveyException, CloneNotSupportedException{
		return surveyCoreService.getSurveyCloneService(surveyId);
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Eliminar una encuesta según el id pasado por parámetro", notes = "Realiza una eliminación lógica de una encuesta cambiando el state a DELETE", response = Boolean.class, httpMethod = "DELETE")
	@DeleteMapping(DELETE_SURVEY)
	public ResponseEntity<Boolean> deleteSurveyById(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId){
		try {
			return ResponseEntity.ok(surveyCoreService.deleteSurveyByIdService(surveyId));
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save delete survey", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save delete survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Paginar preguntas de encuesta", notes = "se listan las preguntas de la encuesta segun los index enviados.", 
	httpMethod = "GET")
	@GetMapping(GET_QUESTIONS)
	public ResponseEntity<PageableResponse<QuestionDTO>> getQuestionsPaginator(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) 
	@PathVariable long surveyId, Pageable pageable) throws SurveyException{
		try {
			return ResponseEntity.ok(surveyCoreService.getQuestionsPaginator(surveyId, pageable));
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get questions", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error get questions", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Paginar encuestas partaker", notes = "se listan las encuestas segun los index enviados.", 
	response = SurveySummaryDTO.class, httpMethod = "GET", responseContainer = "List")
	@GetMapping(GET_SURVEYS_PARTAKER)
	public ResponseEntity<PageableResponse<PartakerSurveyDTO>> getSurveysByPartakerPaginator(@ApiParam(name="companyId", value="Id de la compania", required=true)  
	@RequestParam long companyId,  @ApiParam(name="employeeId", value="Empleado", required=true) @PathVariable long employeeId, Pageable pageable) throws SurveyException{
		
		try {
			return ResponseEntity.ok().body(surveyCoreService.getSurveysByPartakerPaginator(companyId, employeeId, pageable));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error list partakers", e);
		} 
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Cierra la encuesta enviada por parámetro", notes = "Cambia el estado de la encuesta a estado 'FINISHED'", response = Boolean.class, httpMethod = "DELETE")
	@DeleteMapping(CLOSE_SURVEY)
	public ResponseEntity<Boolean> closeSurvey(@ApiParam(name="surveyId", value="Id de la encuesta", required=true) @PathVariable long surveyId){
		try {
			return ResponseEntity.ok(surveyCoreService.closeSurveyService(surveyId));
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save delete survey", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save delete survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Configuración del módulo de la empresa", notes = "Obtiene las configuraciones generales del modulo por compañía", 
	response = ConfigModuleSurveyDTO.class, httpMethod = "GET")
	@GetMapping(GET_CONFIGURATION_MODULE)
	public ResponseEntity<ConfigModuleSurveyDTO> getConfigurationModuleSurveyByCompany(@ApiParam(name="companyId", value="Id de la compania", required=true) @PathVariable long companyId) throws SurveyException{
		
		try {
			return ResponseEntity.ok().body(surveyCoreService.getConfigurationModuleSurveyByCompanyService(companyId));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error configuration module company", e);
		} 
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Guardar configuración del módulo de la empresa", notes = "Guarda las configuraciones generales del modulo por compañía", 
	response = ConfigModuleSurveyDTO.class, httpMethod = "POST")
	@PostMapping(POST_CONFIGURATION_MODULE)
	public ResponseEntity<ConfigModuleSurveyDTO> postConfigurationModuleSurveyByCompany(@ApiParam(name="companyId", value="Id de la compania", required=true) @PathVariable long companyId, @RequestBody ConfigModuleSurveyDTO configSurveyObject) throws SurveyException{
		try {
			return ResponseEntity.ok(surveyCoreService.postConfigurationModuleSurveyByCompanyService(companyId, configSurveyObject));
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save configuration module company", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error save configuration module company", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Ping Validator Health Check", notes = "Get and validate that the container its good and execute healthy", response = String.class, httpMethod = "GET")
	@GetMapping(GET_PING_HEALTH_CHECK)
	public ResponseEntity<String> getPingHealthCheck() {
		
		try {
			return ResponseEntity.ok().body("The module is response good");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Health Check Service", e);
		}
	}
	
	/**
	 * Retorna plantilla de excel para la creación de una encuesta
	 * 
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(GET_EXCEL_TEMPLATE)
	public ResponseEntity<byte[]>  getChargeExcelTemplate(@RequestParam(value = "language", required = false) String language){
		try {
			byte[] excelFile =  surveyCoreService.getSurveyExcelTemplateUpload(language);
			return new ResponseEntity<byte[]>(excelFile, HttpStatus.OK);				
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
	}
	
	/**
	 * Retorna plantilla de excel con resultados de una encuesta
	 * 
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(GET_RESPONSE_SURVEY_EXCEL)
	public ResponseEntity<byte[]>  getResponseSurveyExcel(@RequestBody SurveyResponseExcelDTO filter){
		try {
			byte[] excelFile =  surveyCoreService.getSurveyResponseExcel(filter);
			return new ResponseEntity<byte[]>(excelFile, HttpStatus.OK);				
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
	}
	
	/**
	 * Retorna plantilla de excel para agregar participantes a una encuesta
	 * 
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(GET_ADD_PARTAKERS_EXCEL)
	public ResponseEntity<byte[]>  getAddPartakersExcel(@RequestParam(value = "language", required = false) String language){
		try {
			byte[] excelFile =  surveyCoreService.getAddPartakersExcel(language);
			return new ResponseEntity<byte[]>(excelFile, HttpStatus.OK);				
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
	}
	


}