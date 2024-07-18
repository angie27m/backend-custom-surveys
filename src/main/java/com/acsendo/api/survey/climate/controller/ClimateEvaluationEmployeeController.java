package com.acsendo.api.survey.climate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.climate.dto.ClimatePartakerDTO;
import com.acsendo.api.hcm.dto.BatchProcessResult;
import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.hcm.exception.ResourceNotFoundException;
import com.acsendo.api.survey.climate.dto.ClimateEvaluationConfigurationDTO;
import com.acsendo.api.survey.climate.dto.ClimateOptionTypeDTO;
import com.acsendo.api.survey.climate.dto.ClimateQuestionDTO;
import com.acsendo.api.survey.climate.dto.ClimateResumeDTO;
import com.acsendo.api.survey.climate.dto.InfoEmailClimateDTO;
import com.acsendo.api.survey.climate.dto.ResponseClimateDTO;
import com.acsendo.api.survey.climate.service.ClimateEvaluationEmployeeService;
import com.acsendo.api.survey.climate.service.ClimateTemplateService;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@XRayEnabled
@RequestMapping(ClimateEvaluationEmployeeController.MAIN_PATH)
@Api(value = "Evaluación de clima por parte de un participante")
public class ClimateEvaluationEmployeeController {

	public static final String MAIN_PATH = "/survey/climate";

	public static final String OPTIONS = "/options";

	public static final String CONFIGURATION = "/configuration";

	public static final String QUESTIONS = "/questions";

	public static final String RESUME = "/resume";
	
	public static final String FINISH = "/finish";
	
	public static final String COOKIE = "/cookie";
	
	public static final String EMAIL = "/email";
	
	public static final String SEND_EMAIL = EMAIL + "/send";	
	
	public static final String PARTAKERS = "/partakers";	
	
	public static final String PARTAKERS_EXCEL = PARTAKERS + "/excel";	
	
	public static final String BATCH = EMAIL + "/batch";	
	
	public static final String EVALUATIONS = "/evaluations";
	
	public static final String BATCH_TASK = "/batch";
	
	public static final String KEYWORD = "/keyword";
	
	public static final String EMPLOYEES = "/employees";	

	public static final String TEST_EMAIL = EMAIL + "/test";



	@Autowired
	ClimateEvaluationEmployeeService climateEvaluationSvc;
	
	
	@Autowired
	private ClimateTemplateService climateTemplateSvc;	


	/**
	 * Método GET que obtiene las opciones de respuesta para los diferentes tipos de
	 * preguntas (Cuantitativas y demográficas)
	 * 
	 * @param modelId Identificador de un modelo de clima
	 * @return ClimateOptionTypeDTO Opciones de respuestas
	 **/
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene las opciones de respuesta cuantivativas y demográficas de un modelo de clima", response = ClimateOptionTypeDTO.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(OPTIONS)
	public ClimateOptionTypeDTO getOptionsResponsesByModel(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId) {
		try {
			return climateEvaluationSvc.getOptionsResponseFromClimate(modelId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Error  options responses climate model not found", e);
		}
	}

	/**
	 * Obtiene las opciones configurables de un modelo de clima
	 * 
	 * @param modelId Identificador de un modelo de clima
	 * @return ClimateEvaluationConfigurationDTO Configuración del modelo
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene las opciones configurables de un modelo de clima", response = ClimateEvaluationConfigurationDTO.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(CONFIGURATION)
	public ClimateEvaluationConfigurationDTO getClimateEvaluationConfiguration(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId) {
		try {
			return climateEvaluationSvc.getClimateEvaluationConfiguration(modelId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Error get climate evaluation configuration", e);
		}
	}

	/**
	 * Método GET que obtiene todas las preguntas de un modelo (Cuantitativas,
	 * abiertas y demográficas), según el orden dado con las respuestas que ha
	 * registrado
	 * 
	 * @param modelId    Identificador de un modelo de clima
	 * @param partakerId Identificador del participante de la encuesta
	 * @return List<ClimateQuestionDTO> lista de preguntas
	 **/
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene las preguntas cuantivativas, abiertas y demográficas de una evaluación con las respuestas que ha registrado", response = ClimateQuestionDTO.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(QUESTIONS)
	public List<ClimateQuestionDTO> getQuestionsByModelAndPartakerId(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId,
			@ApiParam(name = "partakerId", value = "Identificador del participante de la encuesta", required = true) @RequestParam(required = true) Long partakerId) {
		try {
			return climateEvaluationSvc.getQuestionsByModelAndPartaker(modelId, partakerId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Error questions climate model not found", e);
		}
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Guarda las respuestas de una evaluación de clima", response = Boolean.class)
	@PostMapping()
	public boolean saveClimateResponse(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId,
			@ApiParam(name = "partakerId", value = "Identificador del participante de la encuesta", required = true) @RequestParam(required = true) Long partakerId,
			@RequestBody ResponseClimateDTO responseClimateDTO) {
		try {
			climateEvaluationSvc.saveClimateResponse(responseClimateDTO, modelId, partakerId);
			return true;

		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Response question climate can't save", e);
		}
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene información de compañía y encuesta de clima según su link", response = ClimateResumeDTO.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(RESUME)
	public ClimateResumeDTO getClimateResumeCompany(
			@ApiParam(name = "cid", value = "Identificador de la encuesta de clima", required = true) @RequestParam(required = true) String cid,
			@ApiParam(name = "uid", value = "Identificador del usuario de la encuesta de clima", required = true) @RequestParam(required = false) String uid) {
		try {
			return climateEvaluationSvc.getClimateResumeCompany(cid, uid);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error get resume company and climate", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Finalizar encuesta de clima", response = ClimateResumeDTO.class, httpMethod = "PUT", responseContainer = "DTO")
	@PutMapping(FINISH)
	public boolean finishClimateSurvey(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId,
			@ApiParam(name = "partakerId", value = "Identificador del participante de la encuesta", required = true) @RequestParam(required = true) Long partakerId) {
		try {
			climateEvaluationSvc.finishClimateSurvey(modelId, partakerId);
			return true;
		} catch (SurveyException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finish climate survey", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Consulta si existe un participante con una cookie, si no, crea un nuevo registro", response = ClimateResumeDTO.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(COOKIE)
	public ClimateResumeDTO getPartakerByCookie(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId,
			@ApiParam(name = "cookie", value = "Cadena con el valor de la cookie", required = true) @RequestParam(required = true) String cookie) {
		try {
			 return climateEvaluationSvc.validatePartakerByCookie(modelId, cookie);
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Get partaker", e);
		}
	}	
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene información del correo de invitación a la encuesta de clima", response = InfoEmailClimateDTO.class, httpMethod = "GET", responseContainer = "DTO")
	@GetMapping(EMAIL)
	public InfoEmailClimateDTO getEmailInformation(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId,
			@ApiParam(name = "isReminder", value = "Indica si es para recordatorio de clima", required = true) @RequestParam(required = true) Boolean isReminder) {
		try {
			return climateEvaluationSvc.getEmailInformation(modelId, isReminder);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error get email information", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Guarda o actualiza información del correo de invitación a la encuesta de clima", response = Boolean.class, httpMethod = "PUT")
	@PostMapping(EMAIL)
	public boolean updateEmailInformation(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId,
			@RequestBody InfoEmailClimateDTO infoEmailClimateDTO) {
		try {
			climateEvaluationSvc.updateEmailInformation(modelId, infoEmailClimateDTO);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error update email information", e);
		}
	}

	
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Guarda los participantes de una encuesta de clima")
	@PostMapping(PARTAKERS)
	public void saveClimatePartakers(
			@ApiParam(name = "modelId", value = "Identificador del modelo", required = true) @RequestParam(required = true) Long modelId,
			@RequestBody List<ClimatePartakerDTO> partakers) {
		try {
			 climateEvaluationSvc.savePartakers(partakers,modelId);
		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Response question climate can't save", e);
		}
	}
	
	
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Envia correo de recordatorio a la encuesta de clima", response = Boolean.class, httpMethod = "GET")
	@PostMapping(SEND_EMAIL)
	public boolean sendReminderEmail(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId,
			@RequestBody InfoEmailClimateDTO infoEmailClimateDTO) {
		try {
			climateEvaluationSvc.forwardEmail(modelId, infoEmailClimateDTO.getSubject(), infoEmailClimateDTO.getContent());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending reminder email climate", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Genera plantilla excel para participantes", response = Boolean.class, httpMethod = "GET")
	@GetMapping(PARTAKERS_EXCEL)
	public ResponseEntity<byte[]> getPartakerTemplate(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId) {
		try {
			byte[] excelFile = climateTemplateSvc.getPartakerTemplate(modelId);
			return new ResponseEntity<byte[]>(excelFile, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error get template excel partakers climate", e);
		}
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Se ejecuta tarea programada para envio de correo a evaluaciones de clima que van a comenzar")
	@GetMapping(BATCH)
	public BatchProcessResult sendReminderToEvaluationsToStart() {
		BatchProcessResult result = climateEvaluationSvc.sendReminderToEvaluationsToStart();
		return result;
	
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene los participantes de una encuesta de clima")
	@GetMapping(PARTAKERS)
	public List<ClimatePartakerDTO> getClimatePartakers(
			@ApiParam(name = "modelId", value = "Identificador del modelo", required = true) @RequestParam(required = true) Long modelId) {
		try {
			 return climateEvaluationSvc.getClimatePartakers(modelId);
		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error get climate partakers", e);
		}
	}
	
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene las evaluaciones de clima asignadas a un colaborador", response = Boolean.class, httpMethod = "GET")
	@GetMapping(EVALUATIONS)
	public <T>PageableResponse<T>  getAllSurveyClimateEvaluations(
			@ApiParam(name = "employeeId", value = "Identificador de un empleado", required = true) @RequestParam(required = true) Long employeeId,
			@ApiParam(name = "pageable", value = "Paginación", required = true) Pageable pageable) {
		try {
			return climateEvaluationSvc.getAllSurveyClimateEvaluations(employeeId, pageable);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error in get all survey climate evaluations", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Se ejecuta tarea programada para actualización de estado de evaluaciones según fecha de inicio y cierre")
	@GetMapping(BATCH_TASK)
	public BatchProcessResult updateClimateEvaluationState() {
		BatchProcessResult result = climateEvaluationSvc.updateClimateEvaluationState();
		return result;	
	}
	

	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Obtiene los empleados de una empresa, según filtros", response = Boolean.class, httpMethod = "GET")
	@GetMapping(EMPLOYEES)
	public List<ClimatePartakerDTO> getEmployeesByCompany(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId,
			@ApiParam(name = "name", value = "Nombre del empleado", required = false) @RequestParam(required = false) String name,
			@ApiParam(name = "divisionsId", value = "Ids de departamentos", required = false) @RequestParam(required = false) String divisionsId,
			@ApiParam(name = "jobsId", value = "Identificador del cargo", required = false) @RequestParam(required = false) String jobsId) {
		try {
			return climateEvaluationSvc.getEmployeesByCompany(modelId, name, divisionsId, jobsId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getEmployeesByCompany", e);
		}
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Envia correo de prueba de la encuesta de clima", response = Boolean.class, httpMethod = "GET")
	@PostMapping(TEST_EMAIL)
	public boolean sendTestEmail(
			@ApiParam(name = "modelId", value = "Identificador de un modelo de clima", required = true) @RequestParam(required = true) Long modelId,
			@RequestBody InfoEmailClimateDTO infoEmailClimateDTO) {
		try {
			climateEvaluationSvc.sendTestEmail(modelId, infoEmailClimateDTO);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending test email climate", e);

		}
	}
}
