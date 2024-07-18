package com.acsendo.api.survey.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.acsendo.api.company.repository.CompanyRepository;
import com.acsendo.api.hcm.dto.BatchProcessResult;
import com.acsendo.api.hcm.dto.EmailDTO;
import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.hcm.enumerations.EntityState;
import com.acsendo.api.hcm.enumerations.Environment;
import com.acsendo.api.hcm.model.Employee;
import com.acsendo.api.hcm.model.LabelFlex;
import com.acsendo.api.hcm.model.MailTemplate;
import com.acsendo.api.hcm.model.ThemeColor;
import com.acsendo.api.hcm.repository.EmployeeRepository;
import com.acsendo.api.hcm.repository.LabelFlexRepository;
import com.acsendo.api.hcm.repository.ThemeColorRepository;
import com.acsendo.api.hcm.service.EmailService;
import com.acsendo.api.survey.dto.ConfigModuleSurveyDTO;
import com.acsendo.api.survey.dto.ConfigSurveyDTO;
import com.acsendo.api.survey.dto.PartakerDTO;
import com.acsendo.api.survey.dto.PartakerSurveyDTO;
import com.acsendo.api.survey.dto.QuestionDTO;
import com.acsendo.api.survey.dto.ResponseOptionsDTO;
import com.acsendo.api.survey.dto.SurveyDTO;
import com.acsendo.api.survey.dto.SurveyResponseExcelDTO;
import com.acsendo.api.survey.dto.SurveySummaryDTO;
import com.acsendo.api.survey.enumerations.SurveyOwner;
import com.acsendo.api.survey.enumerations.SurveyParticipanState;
import com.acsendo.api.survey.enumerations.SurveyState;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.acsendo.api.survey.factory.ConfigSurveyDTOFactory;
import com.acsendo.api.survey.factory.ModuleConfigurationSurveyFactory;
import com.acsendo.api.survey.factory.PartakerDTOFactory;
import com.acsendo.api.survey.factory.QuestionDTOFactory;
import com.acsendo.api.survey.factory.ResponseOptionsDTOFactory;
import com.acsendo.api.survey.factory.SurveyDTOFactory;
import com.acsendo.api.survey.factory.SurveySummaryDTOFactory;
import com.acsendo.api.survey.model.ConfigSurvey;
import com.acsendo.api.survey.model.ModuleConfigurationSurvey;
import com.acsendo.api.survey.model.Partaker;
import com.acsendo.api.survey.model.Questions;
import com.acsendo.api.survey.model.ResponseOptions;
import com.acsendo.api.survey.model.Survey;
import com.acsendo.api.survey.repository.ConfigSurveyRepository;
import com.acsendo.api.survey.repository.ModuleConfigurationSurveyRepository;
import com.acsendo.api.survey.repository.PartakerRepository;
import com.acsendo.api.survey.repository.QuestionsRepository;
import com.acsendo.api.survey.repository.ReponseOptionsRepository;
import com.acsendo.api.survey.repository.SurveyRepository;
import com.acsendo.api.survey.util.SurveyExcelTemplateHandler;

/**
 * @author scastiblanco
 *
 */
@Service
public class SurveyCoreService {
	
	public  static final String MIME_TYPE_HTML = "text/html; charset=UTF-8";

	@Autowired
	private SurveyRepository surveyRepository;
	
	@Autowired
	private QuestionsRepository questionsRepository;
	
	@Autowired
	private PartakerRepository partakerRepository;
	
	@Autowired
	private SurveySummaryDTOFactory surveySummaryDTOFactory;

	@Autowired
	private SurveyDTOFactory surveyDTOFactory;
	
	@Autowired
	private QuestionDTOFactory questionDTOFactory;
	
	@Autowired
	private ConfigSurveyRepository configSurveyRepository;
	
	@Autowired
	private ConfigSurveyDTOFactory configSurveyDTOFactory;
	
	@Autowired
	private PartakerDTOFactory partakerDTOFactory;
	
	@Autowired
	private CompanyRepository companyRepository;
	
	@Autowired
	private ReponseOptionsRepository reponseOptionsRepository;
	
	@Autowired
	private ResponseOptionsDTOFactory responseOptionsDTOFactory;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private ThemeColorRepository colorRepository;
	
	@Autowired
	private ModuleConfigurationSurveyRepository moduleConfigurationSurveyRepository;
	
	@Autowired
	private ModuleConfigurationSurveyFactory moduleConfigurationSurveyFactory;
	
	@Autowired
	private SurveyExcelTemplateHandler surveyExcelTemplateHandler;
	
	@Autowired
	private LabelFlexRepository labelFlexRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;

	/**
	 * Listar las encuestas de una compania en especifico
	 * @param companyId
	 * @return
	 */
	public List<SurveySummaryDTO> getListSurveysCompanyService(long companyId) {
		
		List<SurveySummaryDTO> listSurveys = new ArrayList<SurveySummaryDTO>();
		
		//Consulta para traer las propiedades principales de cada una de las encuestas de una empresa
		List<Survey> listSurveysRepository = surveyRepository.findSurveysCompany(companyId);
		
		
		if (listSurveysRepository != null && !listSurveysRepository.isEmpty()) {
			for (Survey surv : listSurveysRepository) {
				
				//Realizamos la busqueda de la configuracion de cada encuesta para validar la fecha de finalización
				Optional<ConfigSurvey> confSurveyRepository = configSurveyRepository.getconfigSurveyBySurveyId(surv.getId());
				if (confSurveyRepository.isPresent()) {
					ConfigSurvey confSurvey = confSurveyRepository.get();
					Date dateToday = new Date();
					if (confSurvey.getEndDate() != null && !surv.getSurveyState().equals(SurveyState.FINISHED) && dateToday.compareTo(confSurvey.getEndDate()) > 0) {
						surv.setSurveyState(SurveyState.FINISHED);
						surv = surveyRepository.save(surv);
					}						
				}
				
				//Validamos que la encuesta tiene ya un link para responderla, si no lo generamos
				if (surv.getLinkSurveyAnonymous() == null || surv.getLinkSurveyAnonymous().equals("")) {
					surv.setLinkSurveyAnonymous(generateLinkSurvey(new SurveyDTO()));
					surv = surveyRepository.save(surv);
				}
				
				//Consulta para traer el conteo de preguntas que tiene una encuesta
				long countQuestionsSurvey = questionsRepository.countQuestionBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes de una encuesta
				long countParticipantsSurvey = partakerRepository.countParticipantsBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes que ya terminaron una encuesta
				long countParticipantsFinishedSurvey = partakerRepository.countParticipantsFinishedSurveyBySurveyId(surv.getId());
				
				listSurveys.add(surveySummaryDTOFactory.newSurveySummary(surv, countQuestionsSurvey, countParticipantsSurvey, countParticipantsFinishedSurvey));
			}			
		}
		
		return listSurveys;
		
	}
	
	
	/**
	 * 	Listar las encuestas de una compania en especifco segun el tipo de encuesta creada.
	 * @param companyId
	 * @param surveyOwner (ADMIN, BOSS)
	 * @return
	 */
	public List<SurveySummaryDTO> getListSurveysCompanyServiceByOwner(long companyId, String surveyOwner) {
		
		List<SurveySummaryDTO> listSurveys = new ArrayList<SurveySummaryDTO>();
		// BUSCAR EL ENUM CORREPONDIENTE 
		SurveyOwner owner = Arrays.stream(SurveyOwner.values())
								        .filter(e -> e.toString().equals(surveyOwner))
								        .findFirst()
								        .orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", surveyOwner)));
		//Consulta para traer las propiedades principales de cada una de las encuestas de una empresa
		List<Survey> listSurveysRepository = surveyRepository.findSurveysCompanyBySurveyOwner(companyId, owner);
		
		
		if (listSurveysRepository != null && !listSurveysRepository.isEmpty()) {
			for (Survey surv : listSurveysRepository) {
				
				//Realizamos la busqueda de la configuracion de cada encuesta para validar la fecha de finalización
				Optional<ConfigSurvey> confSurveyRepository = configSurveyRepository.getconfigSurveyBySurveyId(surv.getId());
				if (confSurveyRepository.isPresent()) {
					ConfigSurvey confSurvey = confSurveyRepository.get();
					Date dateToday = new Date();
					if (confSurvey.getEndDate() != null && !surv.getSurveyState().equals(SurveyState.FINISHED) && dateToday.compareTo(confSurvey.getEndDate()) > 0) {
						surv.setSurveyState(SurveyState.FINISHED);
						surv = surveyRepository.save(surv);
					}						
				}
				//Validamos que la encuesta tiene ya un link para responderla, si no lo generamos
				if (surv.getLinkSurveyAnonymous() == null || surv.getLinkSurveyAnonymous().equals("")) {
					surv.setLinkSurveyAnonymous(generateLinkSurvey(new SurveyDTO()));
					surv = surveyRepository.save(surv);
				}
				
				//Consulta para traer el conteo de preguntas que tiene una encuesta
				long countQuestionsSurvey = questionsRepository.countQuestionBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes de una encuesta
				long countParticipantsSurvey = partakerRepository.countParticipantsBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes que ya terminaron una encuesta
				long countParticipantsFinishedSurvey = partakerRepository.countParticipantsFinishedSurveyBySurveyId(surv.getId());
				
				listSurveys.add(surveySummaryDTOFactory.newSurveySummary(surv, countQuestionsSurvey, countParticipantsSurvey, countParticipantsFinishedSurvey));
			}			
		}
		
		return listSurveys;
		
	}
	
	/**
	 * 	Listar las encuestas de una compania en especifco segun el tipo de encuesta creada.
	 * @param companyId
	 * @param surveyOwner (ADMIN, BOSS)
	 * @return
	 */
	public PageableResponse<SurveySummaryDTO> getListSurveysCompanyServiceByOwnerPaginator(long companyId, String surveyOwner, Pageable pageable) {
		
		PageableResponse<SurveySummaryDTO> response = new PageableResponse<SurveySummaryDTO>();
		List<SurveySummaryDTO> listSurveys = new ArrayList<SurveySummaryDTO>();
		// BUSCAR EL ENUM CORREPONDIENTE 
		SurveyOwner owner = Arrays.stream(SurveyOwner.values())
								        .filter(e -> e.toString().equals(surveyOwner))
								        .findFirst()
								        .orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", surveyOwner)));
		//Consulta para traer las propiedades principales de cada una de las encuestas de una empresa
		Optional<Page<Survey>> listSurveysRepository = surveyRepository.findSurveysCompanyBySurveyOwner(companyId, owner, pageable);
		
		if (listSurveysRepository.isPresent() && !listSurveysRepository.get().isEmpty()) {
			List<Survey> listSurveysEntity = listSurveysRepository.get().getContent();
			for (Survey surv : listSurveysEntity) {
				
				//Realizamos la busqueda de la configuracion de cada encuesta para validar la fecha de finalización
				Optional<ConfigSurvey> confSurveyRepository = configSurveyRepository.getconfigSurveyBySurveyId(surv.getId());
				if (confSurveyRepository.isPresent()) {
					ConfigSurvey confSurvey = confSurveyRepository.get();
					Date dateToday = new Date();
					if (confSurvey.getEndDate() != null && !surv.getSurveyState().equals(SurveyState.FINISHED) && dateToday.compareTo(confSurvey.getEndDate()) > 0) {
						surv.setSurveyState(SurveyState.FINISHED);
						surv = surveyRepository.save(surv);
					}						
				}
				//Validamos que la encuesta tiene ya un link para responderla, si no lo generamos
				if (surv.getLinkSurveyAnonymous() == null || surv.getLinkSurveyAnonymous().equals("")) {
					surv.setLinkSurveyAnonymous(generateLinkSurvey(new SurveyDTO()));	
					surv = surveyRepository.save(surv);
				}
				
				//Consulta para traer el conteo de preguntas que tiene una encuesta
				long countQuestionsSurvey = questionsRepository.countQuestionBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes de una encuesta
				long countParticipantsSurvey = partakerRepository.countParticipantsBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes que ya terminaron una encuesta
				long countParticipantsFinishedSurvey = partakerRepository.countParticipantsFinishedSurveyBySurveyId(surv.getId());
				
				listSurveys.add(surveySummaryDTOFactory.newSurveySummary(surv, countQuestionsSurvey, countParticipantsSurvey, countParticipantsFinishedSurvey));
			}	
			response.setTotal((int)(listSurveysRepository.get().getTotalElements()));
			response.setElements(listSurveys);
		}
		
		return response;
		
	}
		
	
	/**
	 * En la vista de Empleado (Jefe) en la seccion de encuetas, se muestra la inforamcion de la lista de encuestas que yo he creado como jefe.
	 * a diferencia de las encuestas de tipo ADMIN, estas en cuetas la crea el jefe y en la tabla 
	 * survey.ownerType = BOSS
	 * survey.owner = (employeeId) es el id del jefe que creo esa encuesta.
	 * 
	 * @param companyId
	 * @param bossId
	 * @return
	 */
	public List<SurveySummaryDTO> getListSurveysCompanyServiceByBoss(long companyId, long bossId) {
		
		List<SurveySummaryDTO> listSurveys = new ArrayList<SurveySummaryDTO>();
		//Consulta para traer las propiedades principales de cada una de las encuestas de una empresa
		List<Survey> listSurveysRepository = surveyRepository.findSurveysCompanyByBossId(companyId, SurveyOwner.BOSS, bossId);
		
		
		if (listSurveysRepository != null && !listSurveysRepository.isEmpty()) {
			for (Survey surv : listSurveysRepository) {
				
				//Realizamos la busqueda de la configuracion de cada encuesta para validar la fecha de finalización
				Optional<ConfigSurvey> confSurveyRepository = configSurveyRepository.getconfigSurveyBySurveyId(surv.getId());
				if (confSurveyRepository.isPresent()) {
					ConfigSurvey confSurvey = confSurveyRepository.get();
					Date dateToday = new Date();
					if (confSurvey.getEndDate() != null && !surv.getSurveyState().equals(SurveyState.FINISHED) && dateToday.compareTo(confSurvey.getEndDate()) > 0) {
						surv.setSurveyState(SurveyState.FINISHED);
						surv = surveyRepository.save(surv);
					}						
				}
				//Validamos que la encuesta tiene ya un link para responderla, si no lo generamos
				if (surv.getLinkSurveyAnonymous() == null || surv.getLinkSurveyAnonymous().equals("")) {
					surv.setLinkSurveyAnonymous(generateLinkSurvey(new SurveyDTO()));	
					surv = surveyRepository.save(surv);
				}
				
				//Consulta para traer el conteo de preguntas que tiene una encuesta
				long countQuestionsSurvey = questionsRepository.countQuestionBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes de una encuesta
				long countParticipantsSurvey = partakerRepository.countParticipantsBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes que ya terminaron una encuesta
				long countParticipantsFinishedSurvey = partakerRepository.countParticipantsFinishedSurveyBySurveyId(surv.getId());
				
				listSurveys.add(surveySummaryDTOFactory.newSurveySummary(surv, countQuestionsSurvey, countParticipantsSurvey, countParticipantsFinishedSurvey));
			}			
		}
		
		return listSurveys;
		
	}
	
	/**
	 * En la vista de Empleado (Jefe) en la seccion de encuetas, se muestra la inforamcion de la lista de encuestas que yo he creado como jefe.
	 * a diferencia de las encuestas de tipo ADMIN, estas en cuetas la crea el jefe y en la tabla 
	 * survey.ownerType = BOSS
	 * survey.owner = (employeeId) es el id del jefe que creo esa encuesta.
	 * 
	 * @param companyId
	 * @param bossId
	 * @return
	 */
	public PageableResponse<SurveySummaryDTO> getListSurveysCompanyServiceByBossPaginator(long companyId, long bossId, Pageable pageable) {
		
		PageableResponse<SurveySummaryDTO> response = new PageableResponse<SurveySummaryDTO>();
		List<SurveySummaryDTO> listSurveys = new ArrayList<SurveySummaryDTO>();
		//Consulta para traer las propiedades principales de cada una de las encuestas de una empresa
		Optional<Page<Survey>> listSurveysRepository = surveyRepository.findSurveysCompanyByBossId(companyId, SurveyOwner.BOSS, bossId, pageable);
		if (listSurveysRepository.isPresent() && !listSurveysRepository.get().isEmpty()) {
			List<Survey> listSurveysEntity = listSurveysRepository.get().getContent();
			
			for (Survey surv : listSurveysEntity) {
				
				//Realizamos la busqueda de la configuracion de cada encuesta para validar la fecha de finalización
				Optional<ConfigSurvey> confSurveyRepository = configSurveyRepository.getconfigSurveyBySurveyId(surv.getId());
				if (confSurveyRepository.isPresent()) {
					ConfigSurvey confSurvey = confSurveyRepository.get();
					Date dateToday = new Date();
					if (confSurvey.getEndDate() != null && !surv.getSurveyState().equals(SurveyState.FINISHED) && dateToday.compareTo(confSurvey.getEndDate()) > 0) {
						surv.setSurveyState(SurveyState.FINISHED);
						surv = surveyRepository.save(surv);
					}						
				}
				//Validamos que la encuesta tiene ya un link para responderla, si no lo generamos
				if (surv.getLinkSurveyAnonymous() == null || surv.getLinkSurveyAnonymous().equals("")) {
					surv.setLinkSurveyAnonymous(generateLinkSurvey(new SurveyDTO()));
					surv = surveyRepository.save(surv);
				}
				
				//Consulta para traer el conteo de preguntas que tiene una encuesta
				long countQuestionsSurvey = questionsRepository.countQuestionBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes de una encuesta
				long countParticipantsSurvey = partakerRepository.countParticipantsBySurveyId(surv.getId());
				
				//Consulta para traer el conteo de los participantes que ya terminaron una encuesta
				long countParticipantsFinishedSurvey = partakerRepository.countParticipantsFinishedSurveyBySurveyId(surv.getId());
				
				listSurveys.add(surveySummaryDTOFactory.newSurveySummary(surv, countQuestionsSurvey, countParticipantsSurvey, countParticipantsFinishedSurvey));
			}	
			response.setTotal((int)(listSurveysRepository.get().getTotalElements()));
			response.setElements(listSurveys);
		}
		
		return response;
				
	}

	
	/**
	 * Listar las encuestas donde yo tengo particiacion. 
	 * @param companyId
	 * @param employeeId
	 * @return
	 */
	public List<PartakerSurveyDTO> getListSurveysCompanyServiceByPartakerId(long companyId, long employeeId) {
		
		List<PartakerSurveyDTO> listPartaker = new ArrayList<PartakerSurveyDTO>();
		//Consulta para traer las propiedades principales de cada una de las encuestas de una empresa
		Optional<List<String[]>> listSurveysRepository = surveyRepository.findSurveysCompanyByPartakerId(companyId, employeeId);
		
		if (listSurveysRepository.isPresent() && !listSurveysRepository.get().isEmpty()) {
			listPartaker = listSurveysRepository.get()
								.stream().map(p -> partakerDTOFactory.newPartakerAndSurvey(p))
								.collect(Collectors.toList());			
		}
		
		return listPartaker;
		
	}
	
	
	/**
	 * Obtiene y recupera todos los detalles que tiene una encuesta segun el Id de encuesta pasado por parámetro
	 * @param surveyId
	 * @return
	 */
	public SurveyDTO getSurveyByIdService(long surveyId) {
		
		SurveyDTO surveyObject = new SurveyDTO();
		
		//Consulta para traer los detalles generales de la encuesta
		Optional<Survey> surveyObjectRepository = surveyRepository.findById(surveyId);
		
		if (surveyObjectRepository.isPresent()) {
			
			Survey surv = surveyObjectRepository.get();
			surveyObject = surveyDTOFactory.newSurvey(surv);
			
			//Metodo: Consulta las preguntas que tiene la encuesta
			List<QuestionDTO> listQuestions = listQuestionsBySurvey(surv.getId());
			surveyObject.setQuestions(listQuestions);
			
			//Consulta para obtener la configuración de la encuesta
			ConfigSurveyDTO configSurvey = getSettingBySurveyId(surv.getId());
			surveyObject.setSettings(configSurvey);
			
			//Consulta los participantes que tiene la encuesta
//			List<PartakerDTO> listParticipants = listPartakerBySurveyId(surv.getId());
//			surveyObject.setPartakers(listParticipants);
			
			//Debemos traer solo el conteo de participantes que hacen parte de la encuesta
			long countParticipantsSurvey = partakerRepository.countParticipantsSurveyBySurveyId(surveyId);
			surveyObject.setCountPartakers(countParticipantsSurvey);
			
			
			//Consulta extra para obtener el número de participantes que ya terminaron de contestar la encuesta
			long countParticipantsFinishedSurvey = partakerRepository.countParticipantsFinishedSurveyBySurveyId(surveyId);
			surveyObject.setCountResponses(countParticipantsFinishedSurvey);
			
		}
		
		return surveyObject;
		
	}
	/**
	 * Obtiene y recupera todos los detalles que tiene una encuesta segun el Id de encuesta pasado por parámetro
	 * Ejemplo busca por /survey?sid=7a7EOzVw1BkdLuikH3p4RTK9oF9ihV8vx8gWqpfLEifXsS9gbtfW2FFCnMkm7aiuQXM1aM
	 * @param surveyId
	 * @return
	 * @throws SurveyException 
	 */
	public SurveyDTO getSurveyBySecureCoreService(String surveyCode, long partakerId) throws SurveyException {
		
		SurveyDTO surveyObject = new SurveyDTO();
		
		//Consulta la encuesta con el secure ID 
		Optional<Survey> surveyObjectRepository = surveyRepository.findBySecureCode("/survey?sid=".concat(surveyCode));
		
		if (surveyObjectRepository.isPresent()) {
			
			Survey surv = surveyObjectRepository.get();
			surveyObject = surveyDTOFactory.newSurvey(surv);
			
//			//Metodo: Consulta las preguntas que tiene la encuesta
//			List<QuestionDTO> listQuestions = listQuestionsBySurvey(surv.getId());
//			surveyObject.setQuestions(listQuestions);
			
			//Consulta para obtener la configuración de la encuesta
			ConfigSurveyDTO configSurvey = getSettingBySurveyId(surv.getId());
			surveyObject.setSettings(configSurvey);
			
			//Consulta los participantes que tiene la encuesta
//			List<PartakerDTO> listParticipants = listPartakerBySurveyId(surv.getId());
			if(partakerId != 0) {
				List<PartakerDTO> listParticipants = getPartakerBySurveyId(surv.getId(), partakerId);
				surveyObject.setPartakers(listParticipants);
			}
			
			
		}
		return surveyObject;
		
	}
	
	/**
	 * Listar los participantes de una encuestas 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<PartakerDTO> listPartakerBySurveyId(long id) {
		List<PartakerDTO> listParticipants = new ArrayList<PartakerDTO>();
		Optional<List<Partaker>> listParticipantsInRepository = partakerRepository.getListPartakerBySurveyId(id);
		if (listParticipantsInRepository.isPresent()) {
			listParticipants = listParticipantsInRepository.get().
					stream().
					map(partakerDTOFactory::newPartaker).
					collect(Collectors.toList());
			
		}
		return listParticipants;
	}
	
	/**
	 * Listar los participantes de una encuestas 
	 * @param id
	 * @return
	 */
	private List<PartakerDTO> getPartakerBySurveyId(long surveyId, long partakerId) {
		List<PartakerDTO> listParticipants = new ArrayList<PartakerDTO>();
		Optional<List<Partaker>> listParticipantsInRepository = partakerRepository.getPartakerBySurveyId(surveyId, partakerId);
		if (listParticipantsInRepository.isPresent()) {
			listParticipants = listParticipantsInRepository.get().
					stream().
					map(partakerDTOFactory::newPartaker).
					collect(Collectors.toList());
			
		}
		return listParticipants;
	}


	/**
	 * Recuperar la configuracion de encuestas 
	 * @param id
	 * @return
	 */
	private ConfigSurveyDTO getSettingBySurveyId(long id) {
		ConfigSurveyDTO configSurvey = new ConfigSurveyDTO();
		Optional<ConfigSurvey> confSurveyRepository = configSurveyRepository.getconfigSurveyBySurveyId(id);
		if (confSurveyRepository.isPresent()) {
			ConfigSurvey confSurvey = confSurveyRepository.get();
			configSurvey = configSurveyDTOFactory.newConfigSurveyDTO(confSurvey);
		}
		return configSurvey;
	}


	/**
	 * Listar las preguntas de una encuesta especifica
	 * @param id
	 * @return
	 */
	private List<QuestionDTO> listQuestionsBySurvey(long id) {
		List<QuestionDTO> listQuestions = new ArrayList<QuestionDTO>();
		List<ResponseOptionsDTO> listResponseOptions = new ArrayList<ResponseOptionsDTO>();
		Optional<List<Questions>> listQuestionsRepository = questionsRepository.findQuestionsBySurveyId(id);
		if (listQuestionsRepository.isPresent()) {
			List<Questions> listQuestionsEntity = listQuestionsRepository.get();
			for (Questions ques : listQuestionsEntity) {
				//Buscamos la lista de las opciones de respuesta de cada pregunta
				Optional<List<ResponseOptions>> responseOptionsEntity = reponseOptionsRepository.findListResponseOptionsByQuestionId(ques.getId());
				if (responseOptionsEntity.isPresent()) {
					listResponseOptions = responseOptionsEntity.get().
							stream().
							map(responseOptionsDTOFactory::newResponseOptions).
							collect(Collectors.toList());
				}
				listQuestions.add(questionDTOFactory.newQuestion(ques, listResponseOptions));
				
			}
		}
		return listQuestions;
	}

	/**
	 * Recuperar la lista de preguntas segun la paginacion indicada
	 * Trae las preguntas junto con las opciones de respuesta indicadas.
	 * @param id
	 * @param pagable
	 * @return
	 */
	private PageableResponse<QuestionDTO> listQuestionsBySurvey(long id, Pageable pageable) {
		PageableResponse<QuestionDTO> response = new PageableResponse<QuestionDTO>();
		List<QuestionDTO> listQuestions = new ArrayList<QuestionDTO>();
		List<ResponseOptionsDTO> listResponseOptions = new ArrayList<ResponseOptionsDTO>();
		Optional<Page<Questions>> listQuestionsRepository = questionsRepository.findQuestionsBySurveyId(id, pageable);
		if (listQuestionsRepository.isPresent()) {
			List<Questions> listQuestionsEntity = listQuestionsRepository.get().getContent();
			for (Questions ques : listQuestionsEntity) {
				//Buscamos la lista de las opciones de respuesta de cada pregunta
				Optional<List<ResponseOptions>> responseOptionsEntity = reponseOptionsRepository.findListResponseOptionsByQuestionId(ques.getId());
				if (responseOptionsEntity.isPresent()) {
					listResponseOptions = responseOptionsEntity.get().
							stream().
							map(responseOptionsDTOFactory::newResponseOptions).
							collect(Collectors.toList());
				}
				listQuestions.add(questionDTOFactory.newQuestion(ques, listResponseOptions));
			}
			response.setTotal((int)(listQuestionsRepository.get().getTotalElements()));
			response.setElements(listQuestions);
		}
		return response;
	}

	/**
	 * Listar las encuestas donde yo tengo particiacion. 
	 * @param companyId
	 * @param employeeId
	 * @return
	 */
	public PageableResponse<PartakerSurveyDTO> listSurveysByPartaker(long companyId, long employeeId,  Pageable pageable) {
		PageableResponse<PartakerSurveyDTO> response = new PageableResponse<PartakerSurveyDTO>();
		List<PartakerSurveyDTO> listPartaker = new ArrayList<PartakerSurveyDTO>();
		//Consulta para traer las propiedades principales de cada una de las encuestas de una empresa
		Optional<Page<Object[]>> listSurveysRepository = surveyRepository.findSurveysCompanyByPartakerId(companyId, employeeId, pageable);
		
		if (listSurveysRepository.isPresent() && !listSurveysRepository.get().isEmpty()) {
			listPartaker = listSurveysRepository.get()
								.stream().map(p -> partakerDTOFactory.newPartakerAndSurvey(p))
								.collect(Collectors.toList());	
		}
		response.setTotal((int)(listSurveysRepository.get().getTotalElements()));
		response.setElements(listPartaker);
		return response;
		
	}
	
	/**
	 * Recupera la lista de templates que tiene una compañía
	 * @param companyId
	 * @return
	 */
	public List<SurveySummaryDTO> getListTemplatesSurveysByCompanyService(long companyId) {
		
		List<SurveySummaryDTO> listSurveys = new ArrayList<SurveySummaryDTO>();
		
		//Consulta para traer las propiedades principales de cada una de los templates que tiene la compañía
		List<Survey> listSurveysRepository = surveyRepository.findSurveysTemplateByCompany(companyId);
		
		
		if (listSurveysRepository != null && !listSurveysRepository.isEmpty()) {
			for (Survey surv : listSurveysRepository) {
				
				//Consulta para traer el conteo de preguntas que tiene cada template
				long countQuestionsSurvey = questionsRepository.countQuestionBySurveyId(surv.getId());
				
				listSurveys.add(surveySummaryDTOFactory.newSurveySummary(surv, countQuestionsSurvey));
			}			
		}
		
		return listSurveys;
		
	}
	
	
	/**
	 * Servicio que recupera la lista de plantillas base de acsendo
	 * @return
	 */
	public List<SurveySummaryDTO> getListTemplatesSurveysBaseAcsendoService() {
		
		List<SurveySummaryDTO> listSurveys = new ArrayList<SurveySummaryDTO>();
		
		//Consulta para traer las propiedades principales de cada una de los templates base de acsendo
		List<Survey> listSurveysRepository = surveyRepository.findSurveysTemplateBaseAcsendo();
		
		if (listSurveysRepository != null && !listSurveysRepository.isEmpty()) {
			for (Survey surv : listSurveysRepository) {
				
				//Consulta para traer el conteo de preguntas que tiene cada template base
				long countQuestionsSurvey = questionsRepository.countQuestionBySurveyId(surv.getId());
				
				listSurveys.add(surveySummaryDTOFactory.newSurveySummary(surv, countQuestionsSurvey));
			}			
		}
		
		return listSurveys;
		
	}
	
	/**
	 * Guarda la encuesta 
	 * @param companyId
	 * @param surveyObject
	 * @return
	 */
	public SurveyDTO putSaveSurveyService(long companyId, SurveyDTO surveyObject, Long userId, Long employeeId) {
		
		
		//Actualiza los campos de la entidad para persistirlos en la BD y devuelve la entidad actualizada
		Survey surveySave = saveAndGetSurveyEntity(companyId, surveyObject, userId, employeeId);
		surveyObject.setId(surveySave.getId());
		
		if (surveySave != null && !surveySave.equals("")) {
			if (surveyObject.getSettings() != null && !surveyObject.getSettings().equals("")) {
				if(surveyObject.getQuestions() != null) {
					
				boolean configTimeAndLogic = false;
				// Validacion para configuracion de la encuestas en tiempos y con lógica
				for (QuestionDTO questionDTO : surveyObject.getQuestions()) {
					if(questionDTO.getTimeSeconds() > 0) {
						configTimeAndLogic = true; break;
					}
					if(questionDTO.getListResponseOptions().size() > 0) {
						for (ResponseOptionsDTO responseOptionDto : questionDTO.getListResponseOptions()) {
							if(responseOptionDto.getLinkQuestion() != 0) {
								configTimeAndLogic = true; break;
							}
						}
					}
				}
				// Si es true, se coloca por defecto 1 pregunta por pagina y sin el boton de regresar.
				if(configTimeAndLogic) {
					surveyObject.getSettings().setNumberQuestionByPage(1);
					surveyObject.getSettings().setShowBackButton(false);
				}
				}
//				Actualiza la configuracion de la encuesta para persistirla en BD
				ConfigSurvey confSurvSave = saveAndGetConfigSurveyEntity(surveyObject.getSettings(), surveySave);
				surveyObject.getSettings().setId(confSurvSave.getId());
			}
			if (surveyObject.getQuestions() != null) {
//				Actualiza el orden de las preguntas de la encuesta para persistirla en BD
				for (QuestionDTO questionDTO : surveyObject.getQuestions()) {
					Optional<Questions> optQuestion = questionsRepository.findById(questionDTO.getId());
					// actualizar unicamente el priority de las preguntas
					if(optQuestion.isPresent()) {
						Questions questionsSave = optQuestion.get();
						questionsSave.setPriority(questionDTO.getPriority());
						questionsRepository.save(questionsSave);
					}
				}
				
			}
			
		}else {
			return null;
		}
		
		return surveyObject;
	}

	public ConfigSurveyDTO putSaveConfigSurvey(long surveyId, ConfigSurveyDTO configSurvey) throws SurveyException {
		
		//Actualiza los campos de la entidad para persistirlos en la BD y devuelve la entidad actualizada
		Optional<Survey> surveySaveOpt = surveyRepository.findById(surveyId);
		if(surveySaveOpt.isPresent()) {
			ConfigSurvey confSurv = saveAndGetConfigSurveyEntity(configSurvey, surveySaveOpt.get());
			ConfigSurveyDTO configSurveyDto = configSurveyDTOFactory.newConfigSurveyDTO(confSurv);
			return configSurveyDto;
		}else {
			throw new SurveyException(SurveyException.SURVEY_NOT_EXISTS);
		}

	}
	
	
	/**
	 * Actualiza los campos de la entidad Survey para persistirlos en la BD y devuelve la entidad actualizada
	 * @param companyId
	 * @param surveyObject
	 * @return
	 */
	public Survey saveAndGetSurveyEntity(long companyId, SurveyDTO surveyObject, Long userId, Long employeeId) {
		
		Survey survey = new Survey();
		
		// Identificar si es una edicion
		if(surveyObject.getId() > 0L) {
			Optional<Survey> optSurveyReporitory = surveyRepository.findById(surveyObject.getId());
			if(optSurveyReporitory.isPresent()) {
				survey = optSurveyReporitory.get();				
			}
		} else {
			survey.setId(-1);
			survey.setOwner(surveyObject.getOwner());
			survey.setState(EntityState.ACTIVE);
			// Generar el link de la encuesta
			survey.setLinkSurveyAnonymous(generateLinkSurvey(surveyObject));	
			survey.setUserId(userId);
			survey.setCreatedBy(employeeId);
			if (employeeId == null) {
				Employee employee = employeeRepository.findEmployeeByUserId(userId);
				if (employee != null) {
					survey.setCreatedBy(employee.getId());					
				}			
			} else {
				survey.setUserId(employeeId);
			}
		}
		survey.setCompany(companyRepository.findById(companyId).get());
		survey.setTitle(surveyObject.getTitle());
		survey.setDescription(surveyObject.getDescription());
		
		survey.setSurveyState(SurveyState.valueOf(surveyObject.getStateSurvey()));
		survey.setAttachment(surveyObject.getAttachment());
		survey.setCountdown(surveyObject.getCountdown());
		survey.setOwnerType(SurveyOwner.valueOf(surveyObject.getOwnerType()));
		survey = surveyRepository.save(survey);
		
		return survey;
	}
	
	private String generateLinkSurvey(SurveyDTO surveyObject) {
		try {
			SecureRandom sha1Random = SecureRandom.getInstance("SHA1PRNG");
			int leftLimit = 48; // numeral '0'
		    int rightLimit = 122; // letter 'z'
		    int targetStringLength = 70;
		    String generatedString = sha1Random.ints(leftLimit, rightLimit + 1)
		      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
		      .limit(targetStringLength)
		      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
		      .toString();
		    return "/survey?sid=".concat(generatedString) ;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} // assuming Unix

		return null;
	}


	/**
	 * Actualiza los campos de la entidad ConfigSurvey para persistirlos en la BD y devuelve la entidad actualizada
	 * @param configSurveyObject
	 * @param survey
	 * @return
	 */
	public ConfigSurvey saveAndGetConfigSurveyEntity(ConfigSurveyDTO configSurveyObject, Survey survey) {
		// Crear un objeto para salvar en la base de datos
		ConfigSurvey configSurvey = configSurveyDTOFactory.newConfigSurvey(configSurveyObject);
		configSurvey.setSurvey(survey);
		
		// identificar si es una edicion
		if(survey.getId() > 0L) {
			Optional<ConfigSurvey> optSurveyReporitory = configSurveyRepository.getconfigSurveyBySurveyId(survey.getId());
			if(optSurveyReporitory.isPresent()) {
				configSurvey.setId(optSurveyReporitory.get().getId());
			}
		}else {
			configSurvey.setId(-1);
			configSurvey.setNumberQuestionByPage(5L);
		}
		
		configSurvey = configSurveyRepository.save(configSurvey);
		
		Optional<Survey> survRepository = surveyRepository.findById(configSurvey.getSurvey().getId());
		if (survRepository.isPresent()) {
			Survey surv = survRepository.get();
			if (surv.getSurveyState().equals(SurveyState.FINISHED) && configSurvey.getEndDate() != null) {
				surv.setSurveyState(SurveyState.CREATED);
				surveyRepository.save(surv);
				
			}
		}
		
		return configSurvey;
	}
	
	public Partaker saveAndGetPartakerEntity(PartakerDTO partakerObject, Survey survey) {
		
		Partaker partaker = new Partaker();
		
		// identificar si es una edicion
		if(partakerObject.getId() > 0L) {
			Optional<Partaker> partkRepository = partakerRepository.findById(partakerObject.getId());
			if(partkRepository.isPresent()) {
				partaker = partkRepository.get();				
			}
		}else {
			partaker.setId(-1);
			partaker.setSurvey(survey);
		}
		partaker.setName(partakerObject.getName());
		partaker.setEmail(partakerObject.getEmail());
		partaker.setParticipantState(SurveyParticipanState.valueOf(partakerObject.getParticipantState()));
		partaker.setDateResponseInitial(partakerObject.getDateResponseInitial());
		partaker.setDateResponseFinal(partakerObject.getDateResponseFinal());
		partaker.setLinkSurvey(partakerObject.getLinkSurvey());
		partaker.setAnonymous(partakerObject.isAnonymous());
		partaker.setParticipantEvaluator(partakerObject.getParticipantEvaluator());
		partaker.setParticipantEvaluated(partakerObject.getParticipantEvaluated());
		
		partaker = partakerRepository.save(partaker);
		
		return partaker;
	}
	
	/**
	 * Clona la encuesta con sus configuraciones y preguntas
	 * @param surveyId
	 * @return
	 * @throws CloneNotSupportedException 
	 */
	public SurveyDTO getSurveyCloneService(long surveyId) throws CloneNotSupportedException {
		
		SurveyDTO surveyObject = new SurveyDTO();
		
		//Consulta para traer la entidad encuesta a duplicar
		Optional<Survey> surveyObjectRepository = surveyRepository.findById(surveyId);
		if (surveyObjectRepository.isPresent()) {
			Survey surv = surveyObjectRepository.get();
			
			//Duplicamos survey y realizamos la persistencia en BD
			Survey surveyClone = new Survey();
			surveyClone = (Survey) surv.clone();
			surveyClone.setId(-1);
			surveyClone.setTitle(surv.getTitle() + " Copia");
			surveyClone.setAuditable(null);
			surveyClone.setLinkSurveyAnonymous(generateLinkSurvey(surveyObject));
			surveyClone.setSurveyState(SurveyState.DRAFT);
			surveyClone = surveyRepository.save(surveyClone);
			surveyObject = surveyDTOFactory.newSurvey(surveyClone);
			
			//Consulta para obtener la configuración de la encuesta a clonar
			ConfigSurveyDTO configSurvey = new ConfigSurveyDTO();
			Optional<ConfigSurvey> confSurveyRepository = configSurveyRepository.getconfigSurveyBySurveyId(surveyId);
			if (confSurveyRepository.isPresent()) {
				ConfigSurvey confSurvey = confSurveyRepository.get();
				//Duplicamos ConfigSurvey y realizamos la persistencia en BD
				ConfigSurvey confSurveyClone = new ConfigSurvey();
				confSurveyClone = (ConfigSurvey) confSurvey.clone();
				confSurveyClone.setId(-1);
				confSurveyClone.setSurvey(surveyClone);
				confSurveyClone.setAuditable(null);
				confSurveyClone = configSurveyRepository.save(confSurveyClone);
				configSurvey = configSurveyDTOFactory.newConfigSurveyDTO(confSurveyClone);
			}
			surveyObject.setSettings(configSurvey);
			
			//Consulta las preguntas que tiene la encuesta a clonar
			List<QuestionDTO> listQuestions = new ArrayList<QuestionDTO>();
			List<ResponseOptionsDTO> listResponseOptions = new ArrayList<ResponseOptionsDTO>();
			Optional<List<Questions>> listQuestionsRepository = questionsRepository.findQuestionsBySurveyId(surveyId);
			if (listQuestionsRepository.isPresent()) {
				//Creamos un map para actualizar las preguntas dependientes
				Map<Long,List<ResponseOptions>> mapDependencies = new HashMap<Long, List<ResponseOptions>>();
				List<Questions> listQuestionsEntity = listQuestionsRepository.get();
				for (Questions ques : listQuestionsEntity) {
					//Duplicamos Questions y realizamos la persistencia en BD
					Questions questionClone = new Questions();
					questionClone = (Questions) ques.clone();
					questionClone.setId(-1);
					questionClone.setSurvey(surveyClone);
					questionClone.setAuditable(null);
					questionClone = questionsRepository.save(questionClone);
					
					Optional<List<ResponseOptions>> responseOptionsEntity = reponseOptionsRepository.findListResponseOptionsByQuestionId(ques.getId());
					//Buscamos en el map si la pregunta origina se encuentra como dependencia de alguna de las opciones de respuesta de otra pregunta clonada
					if (mapDependencies.size() > 0) {
						List<ResponseOptions> getResponsesClones = mapDependencies.get(ques.getId());
						if (getResponsesClones != null) {
							for (ResponseOptions responseClone : getResponsesClones) {
								responseClone.setLinkQuestion(questionClone.getId());
							}
							reponseOptionsRepository.saveAll(getResponsesClones);
						}
					}
					if (responseOptionsEntity.isPresent()) {
						List<ResponseOptions> options = responseOptionsEntity.get();
						for (ResponseOptions resOpt : options) {
							//Duplicamos ResponseOptions de cada pregunta y realizamos la persistencia en BD
							ResponseOptions optionClone = new ResponseOptions();
							optionClone = (ResponseOptions) resOpt.clone();
							optionClone.setId(-1);
							optionClone.setQuestion(questionClone);
							optionClone.setAuditable(null);
							optionClone.setLinkQuestion(0L);
							optionClone = reponseOptionsRepository.save(optionClone);
							
							//Ingresamos las preguntas dependientes de las preguntas clonadas
							if (resOpt.getLinkQuestion() > 0) {
								//Buscamos el link question para saber si ya existe en el mapa
								List<ResponseOptions> findListResponses = mapDependencies.get(resOpt.getLinkQuestion());
								if (findListResponses == null) {
									mapDependencies.put(resOpt.getLinkQuestion(), new ArrayList<ResponseOptions>());
								}
								mapDependencies.get(resOpt.getLinkQuestion()).add(optionClone);
							}
							
							listResponseOptions.add(responseOptionsDTOFactory.newResponseOptions(optionClone));
						}
					}
					
					listQuestions.add(questionDTOFactory.newQuestion(questionClone, listResponseOptions));
					listResponseOptions = new ArrayList<ResponseOptionsDTO>();
					
				}
			}
			surveyObject.setQuestions(listQuestions);
		}
		
		return surveyObject;
		
	}
	
	public boolean deleteSurveyByIdService(long surveyId) throws SurveyException {
		
		boolean response = false;
		Optional<Survey> optSurveyReporitory = surveyRepository.findById(surveyId);
		if(optSurveyReporitory.isPresent()) {
			Survey survey = optSurveyReporitory.get();
			survey.setState(EntityState.DELETED);
			survey = surveyRepository.save(survey);
			if (survey.getState().equals(EntityState.DELETED))
				response = true;
			
		}else {
			throw new SurveyException(SurveyException.SURVEY_NOT_EXISTS);
		}		
		
		return response;
	}

/**
 * REecuperar la lista de preguntas de una encuesta especifca, aqui se puede paginar
 * y en caso no encontrar preguntas, saca un error.
 * @param surveyId
 * @param pageable
 * @return
 * @throws SurveyException
 */
	public PageableResponse<QuestionDTO> getQuestionsPaginator(long surveyId, Pageable pageable) throws SurveyException {
		//Metodo: Consulta las preguntas que tiene la encuesta
		PageableResponse<QuestionDTO> listQuestions = listQuestionsBySurvey(surveyId, pageable);
		if(listQuestions.getElements()!=null && !listQuestions.getElements().isEmpty()) {
			return listQuestions;
		}else {
			throw new SurveyException(SurveyException.SURVEY_QUESTIONS_EMPTY);
		}
	}
	
	/**
	 * REecuperar la lista de encuestas de partaker, aqui se puede paginar
	 * y en caso no encontrar participaciones, saca un error.
	 * @param companyId
	 * @param employeeId
	 * @param pageable
	 * @return
	 * @throws SurveyException
	 */
		public PageableResponse<PartakerSurveyDTO> getSurveysByPartakerPaginator(long companyId, long employeeId, Pageable pageable) throws SurveyException {
			//Metodo: Consulta las preguntas que tiene la encuesta
			PageableResponse<PartakerSurveyDTO> listPartakers = listSurveysByPartaker(companyId, employeeId, pageable);
//			if(listPartakers.getElements()!=null && !listPartakers.getElements().isEmpty()) {
				return listPartakers;
//			}else {
//				throw new SurveyException(SurveyException.SURVEY_PARTAKERS_EMPTY);
//			}
		}
		
	
	/**
	 * Metodo que permite recuperar los participantes de una encuesta y luego poder realizar el envio de los correos
	 * a cada uno de ellos. 
	 * no importa, si hace parte o no de la platforma flex, el sistema envia el correo 
	 * 
	 * @param surveyId
	 * @return
	 */
	public BatchProcessResult startSendEmailToPartakers(long surveyId) {
		
		Optional<List<Partaker>> listParticipantsInRepository = partakerRepository.getListPartakerBySurveyIdForSendEmail(surveyId);
		if (listParticipantsInRepository.isPresent()) {
			// Enviar de forma asyncrona los correos de participación
//			Thread newThread = new Thread(() -> {
				AtomicLong affectedObjectsCount = new AtomicLong(0);
				//Lista de participantes para cambiar el estado a SENDED al enviar los correos masivos
				List<Partaker> partakersUpdateState = new ArrayList<Partaker>();
				//Lista de participantes para enviar los correos masivos
				List<EmailDTO> partakersToSendEmails = new ArrayList<EmailDTO>();
			    try {
						// Iterar los participantes y enviar el correo de participación.
						listParticipantsInRepository.get().stream().forEach(partaker -> {
							   //se debe enviar a los estados diferentes de FINISHED
							// Cambiar estado a enviado.
								try {
									EmailDTO partakerEmail = sendEmailForPartaker(partaker, surveyId, "welcomePartakerSurvey",  false, true);
									if (partakerEmail != null) {
										partakersToSendEmails.add(partakerEmail);										
										partaker.setParticipantState(SurveyParticipanState.SENDED);
										partakersUpdateState.add(partaker);
									}
								} catch (SurveyException e) {
									BatchProcessResult.newERRORBatchProcessResult(e);
								}
								affectedObjectsCount.incrementAndGet();					
							
						});
						//Enviamos los correos masivos y actualizamos los estados de cada uno de los participantes
						this.emailService.sendBulkEmials(partakersToSendEmails);	
						//Guardamos el estado actualizado de los participantes
						partakerRepository.saveAll(partakersUpdateState);
						
			    } catch (Exception e) {
					e.printStackTrace();
				}
			    
//			});
//			newThread.start();
			
			
		}
		
		return BatchProcessResult.newOKBatchProcessResult(0L);
	}

	/**
	 * Enviar correo haciendo uso del servicio de envio de emails
	 * con la platilla, reemplazo de parametros.
	 * @param surveyId 
	 * @param horequest
	 * @param templateName
	 * @param sendToBoss
	 * @param sendToCollaborator
	 * @throws SurveyException 
	 */
	public EmailDTO sendEmailForPartaker(Partaker partaker, long surveyId, String templateName, boolean sendToBoss,boolean sendToCollaborator) throws SurveyException {
		
		Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
		Optional<ConfigSurvey> confSurveyOpt = configSurveyRepository.getconfigSurveyBySurveyId(surveyId);
		
		if(surveyOpt.isPresent() && confSurveyOpt.isPresent()) {
			Survey survey = surveyOpt.get();
			ConfigSurvey config = confSurveyOpt.get();
			
			Optional<MailTemplate> templateOpt = emailService.getEmailTemplate(templateName, survey.getCompany().getLanguageCode());
			if(templateOpt.isPresent()) {
				EmailDTO partakerEmail = new EmailDTO();
				HashMap<String, String>  params = buildParamsForPartaker(partaker, survey, config);
				String contentEmail = emailService.buildTemplateWithData(params, templateOpt.get().getContent());
				String subject = templateOpt.get().getSubject().replaceAll("#subjectWelcomeSurvey#", config.getTemplateSubjectSendMail());	
				LabelFlex supportEmail=labelFlexRepository.findByLanguageCodeAndCode("es", "SUPPORT_EMAIL" );
				LabelFlex fromNameEmail=labelFlexRepository.findByLanguageCodeAndCode("es", "FROM_NAME_EMAIL");
				
				//crea el EmailDTO
				partakerEmail.setContent(contentEmail);
				partakerEmail.setSubject(subject);
				partakerEmail.setEmailSender(survey.getCompany().getFromaddress() != null ? survey.getCompany().getFromaddress() : supportEmail.getLabel());
				partakerEmail.setCompanyId(survey.getCompany().getId());
				partakerEmail.setEmailReceiver(partaker.getEmail());
				partakerEmail.setMimeType(MIME_TYPE_HTML);
				partakerEmail.setSenderName(survey.getCompany().getFromname() != null ? survey.getCompany().getFromname() : fromNameEmail.getLabel());		
				
				return partakerEmail;
				
			} else {
				new Throwable("Template "+templateName+" is not present");
			}
			
		}else {
			throw new SurveyException(SurveyException.SURVEY_NOT_EXISTS);
		}
		return null;
		
	}

	/**
	 * Construir el mapa de parametros a enviar en el correo y la plantilla
	 * 
	 * @param partaker
	 * @param survey
	 * @param config 
	 * @return
	 */
	private HashMap<String, String> buildParamsForPartaker(Partaker partaker, Survey survey, ConfigSurvey config) {
		 
		HashMap<String, String>  params = new HashMap<String, String>();
		//String languageCode = survey.getCompany().getLanguageCode();
		params.put("#templateMessageSendMail#", config.getTemplateMessageSendMail());
		params.put("#name#", partaker.getName());
		params.put("#titleSurvey#", survey.getTitle());
		params.put("#descriptionSurvey#", survey.getDescription()!=null ? survey.getDescription() : "");
		Optional<List<ThemeColor>> colorsOpt = colorRepository.findAllByThemeId(survey.getCompany().getTheme());
		if(colorsOpt.isPresent() && colorsOpt.get().size()>0) {
			params.put("#color#", colorsOpt.get().get(0).getColorValue());
		}
		
		String systemEnvironment = System.getenv("STAGE");
		if(systemEnvironment == null || systemEnvironment.isEmpty()) {
			systemEnvironment = "local";
		}
		Environment environment = Environment.valueOf(systemEnvironment);
		systemEnvironment = environment.getSystemEnvironment();
		String urlAction = partaker.getLinkSurvey();
		String apiUrl = systemEnvironment + urlAction +"&prod=true";
		params.put("#urlApp#", apiUrl);
		
		return params;
	}
	
	public boolean closeSurveyService(long surveyId) throws SurveyException {
		
		boolean response = false;
		Optional<Survey> optSurveyReporitory = surveyRepository.findById(surveyId);
		if(optSurveyReporitory.isPresent()) {
			Survey survey = optSurveyReporitory.get();
			survey.setSurveyState(SurveyState.FINISHED);
			survey = surveyRepository.save(survey);
			if (survey.getSurveyState().equals(SurveyState.FINISHED)) {
				response = true;
				Optional<ConfigSurvey> confSurveyRepository = configSurveyRepository.getconfigSurveyBySurveyId(surveyId);
				if(confSurveyRepository.isPresent()) {
					ConfigSurvey confSurvey = confSurveyRepository.get();
					confSurvey.setEndDate(null);
					configSurveyRepository.save(confSurvey);
					
				}
				
				
			}
			
		}else {
			throw new SurveyException(SurveyException.SURVEY_NOT_EXISTS);
		}		
		
		return response;
	}
	
	/**
	 * Envía una notificación de correo a un participante en específico
	 * @param surveyId
	 * @param partakerId
	 * @return
	 * @throws SurveyException 
	 */
	public BatchProcessResult sendEmailToPartakerSpecificById(long surveyId, long partakerId) throws SurveyException {
		AtomicLong affectedObjectsCount = new AtomicLong(0);
		
		Optional<Partaker> partakerFindRepository = partakerRepository.findById(partakerId);
		if (partakerFindRepository.isPresent()) {
			Partaker partaker = partakerFindRepository.get();
			//se debe enviar a los estados diferentes de FINISHED
			if(!partaker.getParticipantState().equals(SurveyParticipanState.FINISHED)) {
				EmailDTO partakerEmail = sendEmailForPartaker(partaker, surveyId, "welcomePartakerSurvey",  false, true);
				if (partakerEmail != null) {
					List<EmailDTO> partakersListToSend = new ArrayList<EmailDTO>();
					partakersListToSend.add(partakerEmail);										
					//Enviamos los correos masivos y actualizamos los estados de cada uno de los participantes
					this.emailService.sendBulkEmials(partakersListToSend);	
					partaker.setParticipantState(SurveyParticipanState.SENDED);
					partakerRepository.save(partaker);
				}
			}
		}
		return BatchProcessResult.newOKBatchProcessResult(affectedObjectsCount.get());
	}

	
	/**
	 * Servicio que se encarga de traer las configuraciones generales del modulo de encuestas de una compañía
	 * Si no existe registro de las configuraciones, se encarga de crear uno con la información por defecto
	 * @param companyId
	 * @return
	 * @throws SurveyException
	 */
	public ConfigModuleSurveyDTO getConfigurationModuleSurveyByCompanyService(long companyId) throws SurveyException {
		ConfigModuleSurveyDTO responseConfiguration = new ConfigModuleSurveyDTO();
		
		Optional<ModuleConfigurationSurvey> confSurveyRepo = moduleConfigurationSurveyRepository.findConfigurationModuleSurveyByCompanyId(companyId);
		ModuleConfigurationSurvey confSurveyCompany = new ModuleConfigurationSurvey();
		if (confSurveyRepo.isPresent()) {
			confSurveyCompany = confSurveyRepo.get();
			responseConfiguration = moduleConfigurationSurveyFactory.newModuleConfigurationSurveyFactory(confSurveyCompany);
		}else {
			//Procedemos a crear el registro de la configuración en la BD
			confSurveyCompany.setId(-1);
			confSurveyCompany.setCompany(companyRepository.findById(companyId).get());
			confSurveyCompany.setCreateSurveyLeader(true);
			confSurveyCompany = moduleConfigurationSurveyRepository.save(confSurveyCompany);
			responseConfiguration = moduleConfigurationSurveyFactory.newModuleConfigurationSurveyFactory(confSurveyCompany);
		}
		
		return responseConfiguration;
	}
	
	/**
	 * Guarda las configuraciones del modulo de encuestas de una compañía
	 * @param companyId
	 * @param configModuleSurveyObject
	 * @return
	 * @throws SurveyException
	 */
	public ConfigModuleSurveyDTO postConfigurationModuleSurveyByCompanyService(long companyId, ConfigModuleSurveyDTO configModuleSurveyObject) throws SurveyException {
		
		Optional<ModuleConfigurationSurvey> confSurveyRepo = moduleConfigurationSurveyRepository.findById(configModuleSurveyObject.getId());
		ModuleConfigurationSurvey confSurveyCompany = new ModuleConfigurationSurvey();
		if (confSurveyRepo.isPresent()) {
			confSurveyCompany = confSurveyRepo.get();
			confSurveyCompany.setCreateSurveyLeader(configModuleSurveyObject.isCreateSurveyLeader());
			confSurveyCompany = moduleConfigurationSurveyRepository.save(confSurveyCompany);
			configModuleSurveyObject = moduleConfigurationSurveyFactory.newModuleConfigurationSurveyFactory(confSurveyCompany);
		}else {
			return null;
		}
		
		return configModuleSurveyObject;
	}
	
	/**
	 * Retorna el template de excel para la creación de una encuesta
	 * @param idCompany
	 * @return
	 */
	public byte[] getSurveyExcelTemplateUpload(String language) {			
		return this.surveyExcelTemplateHandler.generateSurveyExcelTemplate(language);		
	}

	/**
	 * Retorna el template de excel para la creación de una encuesta
	 * @param idCompany
	 * @return
	 */
	public byte[] getSurveyResponseExcel(SurveyResponseExcelDTO filter) {			
		return this.surveyExcelTemplateHandler.generateSurveyResponseExcel(filter);		
	}
	
	/**
	 * Retorna el template de excel para la creación de una encuesta
	 * @param idCompany
	 * @return
	 */
	public byte[] getAddPartakersExcel(String  language) {			
		return this.surveyExcelTemplateHandler.generateAddPartakersExcelTemplate(language);		
	}
	
}
