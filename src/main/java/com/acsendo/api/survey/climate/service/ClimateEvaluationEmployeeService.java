package com.acsendo.api.survey.climate.service;

import static com.acsendo.api.util.DataObjectUtil.getLong;
import static com.acsendo.api.util.DataObjectUtil.getString;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.acsendo.api.climate.dao.ClimateModelDAO;
import com.acsendo.api.climate.dto.ClimatePartakerDTO;
import com.acsendo.api.climate.enumerations.EClimateQuestionType;
import com.acsendo.api.climate.enumerations.SentimentType;
import com.acsendo.api.climate.model.ClimateConfiguration;
import com.acsendo.api.climate.model.ClimateDemographicField;
import com.acsendo.api.climate.model.ClimateDemographicOption;
import com.acsendo.api.climate.model.ClimateDemographicResponse;
import com.acsendo.api.climate.model.ClimateDependenceQuestion;
import com.acsendo.api.climate.model.ClimateDimension;
import com.acsendo.api.climate.model.ClimateDimensionQuestion;
import com.acsendo.api.climate.model.ClimateDimensionQuestionResponse;
import com.acsendo.api.climate.model.ClimateEvaluationKeyword;
import com.acsendo.api.climate.model.ClimateModel;
import com.acsendo.api.climate.model.ClimateModelQuestion;
import com.acsendo.api.climate.model.ClimateModelQuestionResponse;
import com.acsendo.api.climate.model.ClimatePartaker;
import com.acsendo.api.climate.model.ClimateResponseOption;
import com.acsendo.api.climate.repository.ClimateConfigurationRepository;
import com.acsendo.api.climate.repository.ClimateDemographicFieldRepository;
import com.acsendo.api.climate.repository.ClimateDemographicOptionRepository;
import com.acsendo.api.climate.repository.ClimateDemographicResponseRepository;
import com.acsendo.api.climate.repository.ClimateDependenceQuestionRepository;
import com.acsendo.api.climate.repository.ClimateDimensionQuestionRepository;
import com.acsendo.api.climate.repository.ClimateDimensionQuestionResponseRepository;
import com.acsendo.api.climate.repository.ClimateDimensionRepository;
import com.acsendo.api.climate.repository.ClimateEvaluationKeywordRepository;
import com.acsendo.api.climate.repository.ClimateEvaluationRepository;
import com.acsendo.api.climate.repository.ClimateModelQuestionRepository;
import com.acsendo.api.climate.repository.ClimateModelQuestionResponseRepository;
import com.acsendo.api.climate.repository.ClimateModelRepository;
import com.acsendo.api.climate.repository.ClimatePartakerRepository;
import com.acsendo.api.climate.repository.ClimateResponseOptionRespository;
import com.acsendo.api.competences.model.Questionnaire;
import com.acsendo.api.competences.repository.QuestionnaireRepository;
import com.acsendo.api.comprehend.ComprehendUtil;
import com.acsendo.api.customReports.util.LambdaRedshiftUtil;
import com.acsendo.api.evaluation.model.Climate;
import com.acsendo.api.hcm.dto.BatchProcessResult;
import com.acsendo.api.hcm.dto.KeyWordDTO;
import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.hcm.enumerations.EntityState;
import com.acsendo.api.hcm.enumerations.Environment;
import com.acsendo.api.hcm.enumerations.EvaluationState2;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Employee;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.model.LabelFlex;
import com.acsendo.api.hcm.model.MailTemplate;
import com.acsendo.api.hcm.model.User;
import com.acsendo.api.hcm.repository.EmployeeRepository;
import com.acsendo.api.hcm.repository.LabelFlexRepository;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.hcm.repository.MailTemplateRepository;
import com.acsendo.api.hcm.repository.UserRepository;
import com.acsendo.api.hcm.service.EmailService;
import com.acsendo.api.survey.climate.dto.ClimateEvaluationConfigurationDTO;
import com.acsendo.api.survey.climate.dto.ClimateEvaluationDTO;
import com.acsendo.api.survey.climate.dto.ClimateOptionTypeDTO;
import com.acsendo.api.survey.climate.dto.ClimateQuestionDTO;
import com.acsendo.api.survey.climate.dto.ClimateQuestionOptionDependenceDTO;
import com.acsendo.api.survey.climate.dto.ClimateResponseOptionDTO;
import com.acsendo.api.survey.climate.dto.ClimateResumeDTO;
import com.acsendo.api.survey.climate.dto.InfoEmailClimateDTO;
import com.acsendo.api.survey.climate.dto.ResponseClimateDTO;
import com.acsendo.api.survey.enumerations.SurveyParticipanState;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.amazonaws.services.comprehend.model.SentimentScore;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;

/**
 * Clase para los servicios del modelo, preguntas, opciones de respuesta y respuestas de la evaluación de un colaborador
 * */
@Service
public class ClimateEvaluationEmployeeService {

	@Autowired
	private ClimateResponseOptionRespository climateResponseOptionRespository;
	
	@Autowired
	private ClimateDemographicOptionRepository climateDemographicOptionRepository;
		
	@Autowired
	private ClimateConfigurationRepository climateConfigurationRepository;
	
	@Autowired
	private ClimateDimensionQuestionResponseRepository climateDimensionQuestionResponseRepository;
	
	@Autowired
	private ClimateDimensionQuestionRepository climateDimensionQuestionRepository;
	
	@Autowired
	private ClimateModelRepository climateModelRepository;
	
	@Autowired
	private ClimateDimensionRepository climateDimensionRepository;
		
	@Autowired
	private ClimateModelQuestionResponseRepository climateModelQuestionResponseRepository;
	
	@Autowired
	private ClimateModelQuestionRepository climateModelQuestionRepository;
	
	@Autowired
	private ClimateDemographicResponseRepository climateDemographicResponseRepository;
	
	@Autowired
	private ClimateDemographicFieldRepository climateDemographicFieldRepository;
	
	@Autowired
	private ClimatePartakerRepository climatePartakerRepository;
	
	@Autowired
	private QuestionnaireRepository questionnaireRepository;
	
	@Autowired
	private ClimateEvaluationKeywordRepository climateEvaluationKeywordRepository;
		
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private LambdaRedshiftUtil lambdaRedshiftUtil;

	@Autowired
	private ClimateModelDAO modelDao;

	private static final String INITIAL_LINK = "/climate-survey?cid=";
	
	@Autowired
	private EmailService emailService;	

	/**
	* Url para reemplazar el logo de la empresa
	* */
	private static final String URL_LOGO="/logo?companyId=";
	
	@Autowired
	private MailTemplateRepository mailRepository;
	
	@Autowired
	private ClimateDependenceQuestionRepository climateDependenceRepository;
	
	@Autowired
	private  ClimateEvaluationRepository climateEvaluationRepository; 
	
    @Autowired
	private LabelRepository labelRepository;

    @Autowired
	private LabelFlexRepository labelFlexRepository;
    
    @Autowired
   	private EmployeeRepository employeeRepository;
	
    /**
     *  Método que obtiene las opciones de respuesta de las preguntas cuantitativas y demográficas
     *  @param modelId Identificador de un modelo de clima
     *  @return ClimateOptionTypeDTO Dto con la información de las opciones de respuesta
     * */
	public ClimateOptionTypeDTO getOptionsResponseFromClimate(Long modelId){
		

		ClimateOptionTypeDTO optionType = new ClimateOptionTypeDTO();
		Optional<ClimateModel> climateModel = climateModelRepository.findById(modelId);
		
		if(climateModel.isPresent()) {
			Climate evaluation = this.climateEvaluationRepository.findEvaluationByModelId(climateModel.get().getId());
			
			if(evaluation!=null && evaluation.getModel()!=null) {
				
				List<ClimateResponseOption> climate=climateResponseOptionRespository.findAllByModelIdAndKeyIsNotNullOrderById(evaluation.getModel().getId());
				List<ClimateResponseOptionDTO> climateDto=climate.stream().map(c-> new ClimateResponseOptionDTO(c.getId(), c.getKey(), c.getValue().toString(),null)).collect(Collectors.toList());
				optionType.setOptionsClimate(climateDto);			

				List<ClimateResponseOption> enps=climateResponseOptionRespository.findAllByModelIdAndKeyIsNullOrderByValue(evaluation.getModel().getId());
				List<ClimateResponseOptionDTO> enpsDto=enps.stream().map(c-> new ClimateResponseOptionDTO(c.getId(), c.getValue().toString(), c.getValue().toString(),null)).collect(Collectors.toList());
				optionType.setOptionsEnps(enpsDto);
		
				// En las opciones sociodemográficas se envía en el value el id de la opción, ya que es lo que se guarda en la respuesta
				List<ClimateDemographicOption> demographics=climateDemographicOptionRepository.findByFieldModelId(evaluation.getModel().getId());
				List<ClimateResponseOptionDTO> demographicsDto=demographics.stream().map(c-> new ClimateResponseOptionDTO(c.getId(), c.getKey(), String.valueOf(c.getId()), c.getField().getId())).collect(Collectors.toList());
                optionType.setOptionsDemographics(demographicsDto);
			}
		}
			
		return optionType;
		
	}
	
	
	/**
	 *  Método que obtiene las preguntas cuantitativas, abiertas y demográficas de un modelo de clima, según el orden dado.
	 *  @param modelId Identificador de un modelo de clima
	 *  @param partakerId Identificador del participante de la encuesta
	 *  @return List<ClimateQuestionDTO> Listado de preguntas
	 * */
	public List<ClimateQuestionDTO> getQuestionsByModelAndPartaker(Long modelId, Long partakerId) {
		
		Optional<ClimateModel> climateModel = climateModelRepository.findById(modelId);
	   
	    List<ClimateQuestionDTO> questions=new ArrayList<ClimateQuestionDTO>();
		
	    if(climateModel.isPresent()) {
	    
	    	Climate evaluation = this.climateEvaluationRepository.findEvaluationByModelId(climateModel.get().getId());			
			if(evaluation!=null && evaluation.getModel()!=null) {				
				questions=getQuestionsByModel(evaluation.getModel().getId(), partakerId);
			}
	 	}
	   
	    return questions;
		
	}
	
	
	
	/**
	 * Método que obtiene todas las preguntas cuantitativas, abiertas y demogŕaficas de un modelo
	 * Adicional devuelve el arreglo según el orden de las preguntas, o el orden de las dependencias
	 * @param modelId Identificador del modelo 
	 * @param partakerId Identificador del participante de la encuesta
	 * */
	public List<ClimateQuestionDTO> getQuestionsByModel(Long modelId, Long partakerId){
		
		List<Object[]> list=modelDao.getQuestionsByModelIdAndPartakerId(modelId, partakerId);
		
		Map<String, Boolean> questionsMap = new HashMap<String, Boolean>();
		   
		List<ClimateQuestionDTO> questions=new ArrayList<ClimateQuestionDTO>();
		
		List<ClimateDependenceQuestion> dependences=climateDependenceRepository.findQuestionByModelId(modelId);
		
        list.forEach(data->{

            //Valida si la pregunta ya fue agregada
        	Boolean added=questionsMap.get(getLong(data[0])+"-"+(Integer)data[4]);
        	if(added==null) {
        		ClimateQuestionDTO dto=createClimateQuestion(data, dependences);
			    questions.add(dto);
				questionsMap.put(dto.getId()+"-"+dto.getOrder(), true);
				//Si tiene preguntas dependientes las agrega a la lista, para que quede ordenada en orden de dependencias
			    if(dto.getQuestionsDependence() != null && dto.getQuestionsDependence().size()>0) {
			     recursiveQuestions(dependences, dto.getQuestionsDependence(), questions, questionsMap, list);
			    }

        	}
        });
		
		
		return questions;
	}
	
	
	
	/***
	 * Método que recorre las dependencias de una pregunta, para agregarlas en ese mismo orden a la lista a retornar al front
	 * 
	 * */
	private void recursiveQuestions(List<ClimateDependenceQuestion> allDependences, List<ClimateQuestionOptionDependenceDTO> dependencesByQuestion, List<ClimateQuestionDTO> questions, Map<String, Boolean> questionsMap,List<Object[]> list) {
		
		for(ClimateQuestionOptionDependenceDTO dependence:dependencesByQuestion) {
			//Pregunta si ya está agregada la pregunta en el map 
		 	Boolean added=questionsMap.get(dependence.getQuestionId()+"-"+dependence.getOrder());
        	if(added==null) {
        		// Busca la pregunta dependiente en las lista general, para obtener el objeto y agregarlo a la lista a retornar
				Optional<Object[]> dataOpt=list.stream().filter(d-> ((getLong(d[0]).equals(dependence.getQuestionId())) && (((Integer)d[4]).equals(dependence.getOrder())))).findFirst();
				
				if(dataOpt.isPresent()) {
					Object[] data=dataOpt.get();
					ClimateQuestionDTO dto=createClimateQuestion(data, allDependences);
					questions.add(dto);
					questionsMap.put(dependence.getQuestionId()+"-"+dependence.getOrder(), true);
					if(dto.getQuestionsDependence() != null && dto.getQuestionsDependence().size()>0) {
						 recursiveQuestions(allDependences, dto.getQuestionsDependence(), questions, questionsMap, list);
					}
			
				}
           }
		}
		
	}
	
	
	/**
	 * Método que crea el dto de la pregunta de clima junto a sus dependencias(Si las tiene)
	 * */
	private ClimateQuestionDTO createClimateQuestion(Object[] data,List<ClimateDependenceQuestion> allDependences) {
		
		ClimateQuestionDTO dto=new ClimateQuestionDTO();
		dto.setId(getLong(data[0]));
		dto.setQuestion((String)(data[1]));
		dto.setDescription((String)(data[5]));
		dto.setType(EClimateQuestionType.valueOf((String) data[2]));
		dto.setRequired((Boolean)data[3]);
		dto.setOrder((Integer) (data[4]!=null? data[4]:0));
		dto.setResponse((String) data[6]);
		if(allDependences.size()>0) {
			List<ClimateDependenceQuestion> depByQuestion=allDependences.stream().filter(p-> (p.getParentENPSClimateQuestion()!=null && p.getParentENPSClimateQuestion().getId()==dto.getId()) || (p.getParentDemographicQuestion()!=null && p.getParentDemographicQuestion().getId()==dto.getId())).collect(Collectors.toList());
		    dto.setQuestionsDependence(fillNextQuestionDependence(depByQuestion));
		    dto.setFather(fillFatherQuestion(allDependences, dto.getId()));
		}	
	    return dto;
	}
	
	
	
	
	/**
	 * Método que crea las lista con las preguntas dependientes de otra pregunta y de una opción de respuesta
	 * 
	 * */
	private List<ClimateQuestionOptionDependenceDTO>  fillNextQuestionDependence(List<ClimateDependenceQuestion> depByQuestion) {
		
		
		List<ClimateQuestionOptionDependenceDTO> list=new ArrayList<ClimateQuestionOptionDependenceDTO>();
		
		 depByQuestion.forEach(dep->{
			
			ClimateQuestionOptionDependenceDTO dto=new ClimateQuestionOptionDependenceDTO();
			Long questionId=0L;
			Long optionId=0L;
			Integer order=0;
			
			if(dep.getChildENPSClimateQuestion()!=null) {
				questionId=dep.getChildENPSClimateQuestion().getId();
				order=dep.getChildENPSClimateQuestion().getFieldOrder();
				
			}else if(dep.getChildDemographicQuestion()!=null){
				questionId=dep.getChildDemographicQuestion().getId();
				order=dep.getChildDemographicQuestion().getFieldOrder();
			}else if(dep.getChildOpenQuestion()!=null) {
				questionId=dep.getChildOpenQuestion().getId();
				order=dep.getChildOpenQuestion().getFieldOrder();
			}
			dto.setQuestionId(questionId);
			dto.setOrder(order);
			
			if(dep.getParentENPSClimateOption()!=null) {
				optionId=dep.getParentENPSClimateOption().getId();
			}else if(dep.getParentDemographicOption()!=null) {
				optionId=dep.getParentDemographicOption().getId();
			}
			dto.setOptionId(optionId);

			list.add(dto);
		 });
		
		return list;
	}
	
	
	
	
	/**
	 * Método que busca La pregunta padre de una pregunta especifica, para que en el front se puede saber a cual devolverse y a cual seguir
	 * */
	private ClimateQuestionOptionDependenceDTO fillFatherQuestion(List<ClimateDependenceQuestion> depByQuestion, Long questionId) {
		
		ClimateQuestionOptionDependenceDTO fatherDto=null;
		
		Optional<ClimateDependenceQuestion> question=depByQuestion.stream().filter(q-> ( (q.getChildDemographicQuestion()!=null && q.getChildDemographicQuestion().getId()==questionId.longValue()) 
				|| (q.getChildENPSClimateQuestion()!=null && q.getChildENPSClimateQuestion().getId()==questionId.longValue()) 
				|| (q.getChildOpenQuestion()!=null && q.getChildOpenQuestion().getId()==questionId.longValue()))).findFirst();
		
		if(question.isPresent()) {
			fatherDto=new ClimateQuestionOptionDependenceDTO();
			if(question.get().getParentENPSClimateQuestion()!=null) {
				fatherDto.setQuestionId(question.get().getParentENPSClimateQuestion().getId());
				fatherDto.setOrder(question.get().getParentENPSClimateQuestion().getFieldOrder());
		
			} else if(question.get().getParentDemographicQuestion()!=null) {
				fatherDto.setQuestionId(question.get().getParentDemographicQuestion().getId());
				fatherDto.setOrder(question.get().getParentDemographicQuestion().getFieldOrder());
			}
		}
		
		return fatherDto;
	}
	


	/**
	 * Obtiene las opciones configurables para mostrar evaluación de clima
	 * 
	 * @param modelId Identificador de un modelo de clima
	 */
	public ClimateEvaluationConfigurationDTO getClimateEvaluationConfiguration(Long modelId) {

		ClimateEvaluationConfigurationDTO configDTO = new ClimateEvaluationConfigurationDTO();
		Optional<ClimateModel> climateModel = climateModelRepository.findById(modelId);

		if (climateModel.isPresent()) {
			Climate evaluation = this.climateEvaluationRepository.findEvaluationByModelId(climateModel.get().getId());
			if (evaluation != null && evaluation.getModel() != null) {
				Optional<ClimateConfiguration> configurationOp = climateConfigurationRepository.findByModelId(evaluation.getModel().getId());
				if(!configurationOp.isPresent())
					return configDTO;
				ClimateConfiguration configuration = climateConfigurationRepository.findByModelId(evaluation.getModel().getId()).get();
				configDTO.setTitle(evaluation.getName());
				configDTO.setDescription(evaluation.getDescription());
				configDTO.setBackButton(configuration.getBackButton());
				configDTO.setProgressBar(configuration.getProgressBar());
				configDTO.setQuestionsPage(configuration.getQuestionsPage());
				configDTO.setWelcomeImage(evaluation.getWelcomeImage());
				configDTO.setWelcomeText(evaluation.getWelcomeText());
				configDTO.setAnonymous(configuration.getAnonymous());
				configDTO.setStartDate(Date.from(configuration.getStartDate().toInstant()));
				configDTO.setEndDate(Date.from(configuration.getEndDate().toInstant()));
				configDTO.setEndText(evaluation.getEndText());
				configDTO.setWithCookie(configuration.getWithCookie());
				Optional<List<ClimateDependenceQuestion>> dependences = climateDependenceRepository.findAllByModelIdAndState(modelId, EntityState.ACTIVE);
				configDTO.setHasDependenceQuestions(!dependences.isPresent()? false: dependences.get().size() > 0 ? true: false );
			}
		}

		return configDTO;
	}

	/**
	 * Se guardan las respuestas de una evaluación de clima
	 * 
	 * @param responseClimateDTO Objeto que contiene la respuesta de la pregunta
	 * @param modelId Identificador del modelo 
	 * @param partakerId Identificador del participante de la encuesta
	 */
	public void saveClimateResponse(ResponseClimateDTO responseClimateDTO, Long modelId, Long partakerId) {

		Optional<ClimatePartaker> climatePartakerOpt = climatePartakerRepository.findById(partakerId);

		if (climatePartakerOpt.isPresent()) {
			
			// Se actualiza el estado en progreso
			if (climatePartakerOpt.get().getPartakerState().equals(SurveyParticipanState.CREATED) || climatePartakerOpt.get().getPartakerState().equals(SurveyParticipanState.SENDED)) {
				climatePartakerOpt.get().setPartakerState(SurveyParticipanState.IN_PROGRESS);
				climatePartakerRepository.save(climatePartakerOpt.get());
			}			
			
			// Se valida las diferentes opciones de guardado
			if (responseClimateDTO.getType().equals(EClimateQuestionType.CLIMATE_LABEL)
					|| responseClimateDTO.getType().equals(EClimateQuestionType.ENPS)) {
				saveQuantitativeQuestions(responseClimateDTO, climatePartakerOpt.get());
			} else if (responseClimateDTO.getType().equals(EClimateQuestionType.OPEN)) {
				saveOpenQuestions(responseClimateDTO, climatePartakerOpt.get());
			} else if (responseClimateDTO.getType().equals(EClimateQuestionType.SOCIODEMOGRAPHIC)) {
				saveSociodemographicQuestions(responseClimateDTO, climatePartakerOpt.get());
			}
			// Se agrega evento de contestar evaluación de clima en redshift
			try {
				Climate climate = climateEvaluationRepository.findEvaluationByModelId(modelId);
				if (climatePartakerOpt.get().getEmployee() != null) {
					User user = userRepository.getUserByEmployeeId(climatePartakerOpt.get().getEmployee().getId());
					lambdaRedshiftUtil.sendInformationForLambdaAndRedshift(climate.getCompany().getId(), user.getId(), modelId, false, 
							"CLIMATE", "GUARDAR RESPUESTA DE EVALUACIÓN DE CLIMA");					
				} else {
					lambdaRedshiftUtil.sendInformationForLambdaAndRedshift(climate.getCompany().getId(), partakerId, modelId, false, 
							"CLIMATE", "GUARDAR RESPUESTA DE EVALUACIÓN DE CLIMA");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	/**
	 * Guarda o actualiza respuestas cuantitativas de una evaluación de clima
	 * 
	 * @param responseClimateDTO Objeto que contiene la respuesta de la pregunta
	 * @param partaker Entidad que contiene el participante de una evaluación de clima
	 */
	private void saveQuantitativeQuestions(ResponseClimateDTO responseClimateDTO, ClimatePartaker partaker) {

		Optional<ClimateDimensionQuestion> questionOpt = climateDimensionQuestionRepository
				.findById(responseClimateDTO.getQuestionId());

		Optional<ClimateModel> modelOpt = climateModelRepository.findById(questionOpt.get().getModel().getId());

		Optional<ClimateDimension> dimensionOpt = climateDimensionRepository
				.findById(questionOpt.get().getDimension().getId());

		ClimateDimensionQuestionResponse climateRes = climateDimensionQuestionResponseRepository
				.findByPartakerIdAndQuestionId(partaker.getId(), responseClimateDTO.getQuestionId());

		if (climateRes != null) {
			climateRes.setResponse(Integer.valueOf(responseClimateDTO.getResponse()));
			climateDimensionQuestionResponseRepository.save(climateRes);
		} else {
			ClimateDimensionQuestionResponse responseDimension = new ClimateDimensionQuestionResponse();
			responseDimension.setModel(modelOpt.get());
			responseDimension.setDimension(dimensionOpt.get());
			responseDimension.setQuestion(questionOpt.get());
			responseDimension.setPartaker(partaker);
			responseDimension.setResponse(Integer.valueOf(responseClimateDTO.getResponse()));
			climateDimensionQuestionResponseRepository.save(responseDimension);
		}

	}
	

	/**
	 * Guarda o actualiza respuestas abiertas de una evaluación de clima
	 * 
	 * @param responseClimateDTO Objeto que contiene la respuesta de la pregunta
	 * @param partaker Entidad que contiene el participante de una evaluación de clima
	 */
	private void saveOpenQuestions(ResponseClimateDTO responseClimateDTO, ClimatePartaker partaker) {
		
		Optional<ClimateModelQuestion> questionOpt = climateModelQuestionRepository
				.findById(responseClimateDTO.getQuestionId());

		Optional<ClimateModel> modelOpt = climateModelRepository.findById(questionOpt.get().getModel().getId());
		
		ClimateModelQuestionResponse climateRes = climateModelQuestionResponseRepository
				.findByPartakerIdAndQuestionId(partaker.getId(), responseClimateDTO.getQuestionId());

		Climate evaluation = this.climateEvaluationRepository.findEvaluationByModelId(modelOpt.get().getId());
		
		if (climateRes != null) {
			climateRes.setResponse(responseClimateDTO.getResponse());
			climateRes = this.calculateSentiments(climateRes, partaker);
			this.calculateWordCloud(evaluation, responseClimateDTO.getResponse());
			climateModelQuestionResponseRepository.save(climateRes);
		} else {
			ClimateModelQuestionResponse responseDimension = new ClimateModelQuestionResponse();
			responseDimension.setModel(modelOpt.get());
			responseDimension.setQuestion(questionOpt.get());
			responseDimension.setPartaker(partaker);
			responseDimension.setResponse(responseClimateDTO.getResponse());
			//Análisis de los sentimientos de la respuesta y palabras claves de la respuesta
			if(responseDimension.getResponse() != null) {
				responseDimension = this.calculateSentiments(responseDimension, partaker);
				this.calculateWordCloud(evaluation, responseClimateDTO.getResponse());
			}
			climateModelQuestionResponseRepository.save(responseDimension);
		}

	}
		
	/**
	 * Análisis de los sentimientos de la respuesta
	 * @param responseDimension Entidad que contiene la respuesta
	 */
	private ClimateModelQuestionResponse calculateSentiments(ClimateModelQuestionResponse responseDimension, ClimatePartaker partaker){
		
		try {
			AWSXRay.setGlobalRecorder(AWSXRayRecorderBuilder.defaultRecorder());
		    AWSXRay.beginSegment("AmazonComprehend");
		    // your AWS call
		  	SentimentScore ss = ComprehendUtil.getSentiment(responseDimension.getResponse().trim(), partaker.getModel().getCompany().getLanguageCode());
     		responseDimension.setPositiveSentimentScore(ss.getPositive().doubleValue()); 
     		responseDimension.setNegativeSentimentScore(ss.getNegative().doubleValue()); 
     		responseDimension.setNeutralSentimentScore(ss.getNeutral().doubleValue()); 
     		responseDimension.setMixedSentimentScore(ss.getMixed().doubleValue()); 
     		
			SentimentType sentimentType = SentimentType.MIXED;
			if (responseDimension.getPositiveSentimentScore() >= responseDimension.getNegativeSentimentScore()
					&& responseDimension.getPositiveSentimentScore() >= responseDimension.getNeutralSentimentScore()
					&& responseDimension.getPositiveSentimentScore() >= responseDimension.getMixedSentimentScore()) {
				sentimentType = SentimentType.POSITIVE;
			} else if (responseDimension.getNegativeSentimentScore() >= responseDimension.getPositiveSentimentScore()
					&& responseDimension.getNegativeSentimentScore() >= responseDimension.getNeutralSentimentScore()
					&& responseDimension.getNegativeSentimentScore() >= responseDimension.getMixedSentimentScore()) {
				sentimentType = SentimentType.NEGATIVE;
			} else if (responseDimension.getNeutralSentimentScore() >= responseDimension.getPositiveSentimentScore()
					&& responseDimension.getNeutralSentimentScore() >= responseDimension.getNegativeSentimentScore()
					&& responseDimension.getNeutralSentimentScore() >= responseDimension.getMixedSentimentScore()) {
				sentimentType = SentimentType.NEUTRAL;
			}
			responseDimension.setSentimenType(sentimentType);
		} catch (Exception e) {
		    throw e;
		} finally {
		    AWSXRay.endSegment();
		}
		
		
		return responseDimension;
	}
	
	
	/**
	 * Guarda o actualiza respuestas sociodemográficas de una evaluación de clima
	 * 
	 * @param responseClimateDTO Objeto que contiene la respuesta de la pregunta
	 * @param partaker Entidad que contiene el participante de una evaluación de clima
	 */
	private void saveSociodemographicQuestions(ResponseClimateDTO responseClimateDTO, ClimatePartaker partaker) {
		
		Optional<ClimateDemographicField> fieldOpt = climateDemographicFieldRepository.findById(responseClimateDTO.getQuestionId());
		
		Optional<ClimateDemographicOption> optionOpt = climateDemographicOptionRepository.findById(Long.valueOf(responseClimateDTO.getResponse()));
		
		ClimateDemographicResponse climateRes = climateDemographicResponseRepository
				.findByPartakerIdAndFieldId(partaker.getId(), responseClimateDTO.getQuestionId());

		if (climateRes != null) {
			climateRes.setOption(optionOpt.get());
			climateDemographicResponseRepository.save(climateRes);
		} else {
			ClimateDemographicResponse responseField = new ClimateDemographicResponse();
			responseField.setField(fieldOpt.get());
			responseField.setOption(optionOpt.get());
			responseField.setPartaker(partaker);
			climateDemographicResponseRepository.save(responseField);
		}

	}

	/**
	 * Obtiene información de compañía y encuesta de clima según su link
	 * 
	 * @param cid Identificador de la encuesta
	 * @param uid Identificador del usuario de la encuesta
	 */
	public ClimateResumeDTO getClimateResumeCompany(String cid, String uid) {

		//Se construye link de encuesta según sus parámetros
		String link = INITIAL_LINK + cid;
		if (uid != null) {
			link = link + "&uid=" + uid;
		}
		
		ClimateResumeDTO climateResume = new ClimateResumeDTO();
		// Consultar existencia del link en tabla de configuración
		Optional<ClimateConfiguration> optConfiguration = climateConfigurationRepository.findByLinkAnonymous(link);

		// Si existe el link anónimo, se crea un registro en la tabla de participantes
		if (optConfiguration.isPresent()) {

			climateResume.setCompanyId(optConfiguration.get().getModel().getCompany().getId());
			climateResume.setModelId(optConfiguration.get().getModel().getId());
			
			if(!optConfiguration.get().getWithCookie()) {
			   ClimatePartaker partaker = new ClimatePartaker();
			   partaker.setModel(optConfiguration.get().getModel());			   
			   partaker.setPartakerState(SurveyParticipanState.CREATED);
			   ClimatePartaker newPartaker = climatePartakerRepository.save(partaker);
			   climateResume.setState(newPartaker.getState());
			   climateResume.setPartakerState(newPartaker.getPartakerState());
			   climateResume.setPartakerId(newPartaker.getId());			  
			 }
		} else {
			// Si no existe, busca en la tabla de participantes
			Optional<ClimatePartaker> optPartaker = climatePartakerRepository.findByLink(link);
			if (optPartaker.isPresent()) {
				// Validar que el participante sea un empleado activo
				Optional<Employee> employeeOpt = employeeRepository.findById(optPartaker.get().getEmployee().getId());
				if (employeeOpt.isPresent() && employeeOpt.get().getState().toString().equals(EntityState.RETIRED.toString())) {
					optPartaker.get().setState(EntityState.DELETED);
					climatePartakerRepository.save(optPartaker.get());					
				}				
				climateResume.setCompanyId(optPartaker.get().getModel().getCompany().getId());
				climateResume.setModelId(optPartaker.get().getModel().getId());
				climateResume.setState(optPartaker.get().getState());
				climateResume.setPartakerState(optPartaker.get().getPartakerState());
				climateResume.setPartakerId(optPartaker.get().getId());	
			}
		}
		return climateResume;
	}

	/**
	 * Método que finaliza encuesta de clima
	 * @param modelId Identificador del modelo 
	 * @param partakerId Identificador del participante de la encuesta
	 * @throws Exception 
	 */
	public void finishClimateSurvey(Long modelId, Long partakerId) throws SurveyException {
		
		Optional<ClimatePartaker> climatePartakerOpt = climatePartakerRepository.findById(partakerId);
		
		// Valida preguntas respondidas antes de cambiar estado a Finalizado		
		if (climatePartakerOpt.isPresent()) {
			if (validateFinishedQuestions(climatePartakerOpt.get())) {
				//Se actualiza estado de la encuesta a finalizada
				climatePartakerOpt.get().setPartakerState(SurveyParticipanState.FINISHED);
				climatePartakerRepository.save(climatePartakerOpt.get());				
			} else {
				throw new SurveyException(SurveyException.EVALUATION_CANT_FINISH);
			}
		}
	}
	
	/**
	 * Método para validar si se contestaron todas las preguntas de la evaluación
	 * (NO valida evaluaciones con preguntas dependientes)
	 */
	private boolean validateFinishedQuestions(ClimatePartaker partaker) {

		// Este query se encarga de validar que una evaluación de clima tenga las
		// respuestas a todas sus preguntas requeridas o en el caso de no haber
		// preguntas requeridas que tenga al menos una respuesta en la evaluación
		List<Object[]> answeredQuestionsInfo = climateEvaluationRepository
				.infoClimateQuestionsAnswered(partaker.getModel().getId(), partaker.getId());

		if (answeredQuestionsInfo != null && !answeredQuestionsInfo.isEmpty()) {
			Object[] results = answeredQuestionsInfo.get(0);
			if (results.length > 6 && results[7] != null) {
				return Boolean.TRUE.equals(results[7]);
			}
		}
		return false;
	}
	
	/**
	 * Método que consulta si ya existe un registro de un participante con cookie. Si existe, devuelve el id, si no, crea el registro.
	 * @param modelId Identificador del modelo
	 * @param cookie Cadena con la información de la cookie del navegador generada
	 * @return Long con el Identificador del participante
	 * */
	public ClimateResumeDTO validatePartakerByCookie(Long modelId, String cookie) {
		Optional<ClimatePartaker> climatePartakerOpt = climatePartakerRepository.findByModelIdAndCookie(modelId, cookie);
		
		ClimateResumeDTO dto=new ClimateResumeDTO();
		
		Optional<ClimateModel> modelOpt = climateModelRepository.findById(modelId);
		
		if(!climatePartakerOpt.isPresent() & modelOpt.isPresent()) {
			ClimatePartaker partaker = new ClimatePartaker();
			partaker.setModel(modelOpt.get());
			partaker.setPartakerState(SurveyParticipanState.IN_PROGRESS);
			partaker.setCookie(cookie);
			ClimatePartaker newPartaker = climatePartakerRepository.save(partaker);
			dto.setPartakerId(newPartaker.getId());
			dto.setPartakerState(newPartaker.getPartakerState());
			dto.setState(newPartaker.getState());
			dto.setModelId(modelId);
	
		}else {
			dto.setPartakerId(climatePartakerOpt.get().getId());
			dto.setPartakerState(climatePartakerOpt.get().getPartakerState());
			dto.setState(climatePartakerOpt.get().getState());
			dto.setModelId(modelId);
		}
		
		return dto;
	}

	/**
	 * Obtiene información del correo de invitación a la encuesta de clima
	 * @param modelId Identificador del modelo de clima
	 */
	public InfoEmailClimateDTO getEmailInformation(Long modelId, Boolean isReminder) {
		InfoEmailClimateDTO infoEmailDTO = new InfoEmailClimateDTO();
		Optional<ClimateModel> climateModel = climateModelRepository.findById(modelId);
		
		if (climateModel.isPresent()) {
			Climate evaluation = this.climateEvaluationRepository.findEvaluationByModelId(climateModel.get().getId());

			if (evaluation != null && evaluation.getModel() != null) {
				ClimateConfiguration configuration = climateConfigurationRepository.findByModelId(evaluation.getModel().getId()).get();
				String subject = isReminder ? configuration.getSubjectReminderEmail() : configuration.getSubjectEmail();
				String content = isReminder ? configuration.getContentReminderEmail() : configuration.getContentEmail();
				infoEmailDTO.setSubject(subject);
				infoEmailDTO.setContent(content);
			}
		}
		return infoEmailDTO;
	}

	/**
	 * Guarda o actualiza información del correo de invitación a la encuesta de clima
	 * @param modelId Identificador del modelo de clima
	 * @param infoEmailClimateDTO Contiene información del correo
	 */
	public void updateEmailInformation(Long modelId, InfoEmailClimateDTO infoEmailClimateDTO) {
		
		Optional<ClimateModel> climateModel = climateModelRepository.findById(modelId);
		
		if (climateModel.isPresent()) {
			Climate evaluation = this.climateEvaluationRepository.findEvaluationByModelId(climateModel.get().getId());

			if (evaluation != null && evaluation.getModel() != null) {
				ClimateConfiguration configuration = climateConfigurationRepository.findByModelId(evaluation.getModel().getId()).get();
				configuration.setSubjectEmail(infoEmailClimateDTO.getSubject());
				configuration.setContentEmail(infoEmailClimateDTO.getContent());
				climateConfigurationRepository.save(configuration);
				
				// Se cambia estado de evaluación a ACTIVE
				evaluation.setState(EntityState.ACTIVE);
				evaluation.setStartDate(Date.from(configuration.getStartDate().toInstant()));
				evaluation.setEndDate(Date.from(configuration.getEndDate().toInstant()));
				climateEvaluationRepository.save(evaluation);
			}
		}
	
	}



	/**
	 * Envia correo de recordatorio a la encuesta de clima
	 * @param ClimateConfiguration Configuración del modelo de clima
	 * @param subjectEmail Asunto del correo al iniciar la encuesta o al reenviar
	 * @param principalContentEmail Contenido a enviar en el correo
	 */
	public void sendReminderEmail(ClimateConfiguration configuration, String subjectEmail, String principalContentEmail, String recipientEmail,
			boolean withSendedPartakers) {
		
		Optional<ClimateModel> climateModel = climateModelRepository.findById(configuration.getModel().getId());
		if (climateModel.isPresent()) {
			Company company = climateModel.get().getCompany();
			String language = company.getLanguageCode();

			Optional<MailTemplate> mTemplate = mailRepository.getEmailTemplateByType("INVITATION_CLIMATE", language);
			ZoneId zoneTimeCompany = company.getCountry() != null ? ZoneId.of(company.getCountry().getZone()) : ZoneId.systemDefault();
			// Convertir la fecha a la zona horaria de la empresa
	        ZonedDateTime endDateCompany = configuration.getEndDate().withZoneSameInstant(zoneTimeCompany);
	        
			//Se consulta listado de participantes
			List<ClimatePartaker> partakersList = withSendedPartakers ? climatePartakerRepository.findPartakersByModelId(configuration.getModel().getId()) :
				climatePartakerRepository.findPartakersCreatedByModelId(configuration.getModel().getId());
			
			if (!partakersList.isEmpty() && configuration!=null && mTemplate.isPresent()) {
				List<ClimatePartaker> partakersToSend = recipientEmail != null ? partakersList.subList(0, 1) : partakersList;
				partakersToSend.stream().forEach(partaker -> {
					new Thread(() -> {
						try {
							//Asunto
							String subject = subjectEmail;
							String contentEmail = mTemplate.get().getContent();
							String systemEnvironment = getUrlEnviroment();
							HashMap<String, String> mapParams = new HashMap<String, String>();
							//Parámetros correo	
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
							mapParams.put("#companyLogo#", systemEnvironment + URL_LOGO + company.getId());
							mapParams.put("#companyName#", company.getName());
							int year = Calendar.getInstance().get(Calendar.YEAR);
							mapParams.put("#actualYear#", String.valueOf(year));
							mapParams.put("#endDate#", endDateCompany.format(formatter));
							mapParams.put("#userName#", partaker.getName());
							mapParams.put("#emailContent#", principalContentEmail);
							String surveyUrl = configuration.getLogin() ? systemEnvironment : systemEnvironment + partaker.getLink();
							if(configuration.getAnonymous()) {
								surveyUrl = configuration.getLogin() ? systemEnvironment : systemEnvironment + configuration.getLinkAnonymous();
							}
							mapParams.put("#surveyUrl#", surveyUrl);
							String content = emailService.buildTemplateWithData(mapParams, contentEmail);
							LabelFlex supportEmail = labelFlexRepository.findByLanguageCodeAndCode("es", "SUPPORT_EMAIL");
							LabelFlex fromNameEmail = labelFlexRepository.findByLanguageCodeAndCode("es", "FROM_NAME_EMAIL");
							String fromMail = company.getFromaddress() != null ? company.getFromaddress() : supportEmail.getLabel();
							String fromName = company.getFromname() != null ? company.getFromname() : fromNameEmail.getLabel();
							String principalEmail = recipientEmail != null ? recipientEmail : partaker.getEmail();
					        emailService.sendEmail(content, subject, principalEmail, null, null, fromMail, company.getId(), fromName);
					        // Actualiza estado de enviado a correos
					        if(recipientEmail == null && partaker.getPartakerState() != SurveyParticipanState.IN_PROGRESS) {
					          partaker.setPartakerState(SurveyParticipanState.SENDED);
					        }
							climatePartakerRepository.save(partaker);
						} catch (Exception e) {
							System.out.println("Error al enviar correo al participante de clima " + e.getMessage());
							e.printStackTrace();
						}						
					}).start();					
				});
				
			}
		}
	}
	
	/**
	 * Se obtiene la url según el ambiente donde se ejecute la aplicación
	 */
	private String getUrlEnviroment() {
		String systemEnvironment = System.getenv("STAGE");
		if(systemEnvironment == null || systemEnvironment.isEmpty()) {
			systemEnvironment = "local";
		}
		// Url aplicación
		Environment environment = Environment.valueOf(systemEnvironment);
		systemEnvironment = environment.getSystemEnvironment();
		return systemEnvironment;
	}
	
	
	/**
	 * Método para guardar participantes de una encuesta de clima
	 * @param List<ClimatePartakerDTO> Listado con los participantes
	 * @param modelId Identificador del modelo
	 * 
	 * **/
	public void savePartakers(List<ClimatePartakerDTO> partakersAdded, Long modelId) {

		List<ClimatePartaker> listToSave = new ArrayList<ClimatePartaker>();
		Optional<ClimateModel> model = climateModelRepository.findById(modelId);

		if (model.isPresent()) {
			Optional<ClimateConfiguration> configuration = climateConfigurationRepository
					.findByModelId(model.get().getId());
			// Consultamos los registros guardados para esos participantes
			List<ClimatePartaker> partakers = climatePartakerRepository.findActivePartakersByModelId(modelId);
			
			// Si es anómina, obtiene participantes según el correo
			if (configuration.get().getAnonymous()) {
				Map<String, ClimatePartaker> partakersInBD = partakers.stream()
						.collect(Collectors.toMap(ClimatePartaker::getEmail, Function.identity()));
				Map<String, ClimatePartaker> partakersToDelete = new HashMap<String, ClimatePartaker>();
				partakersToDelete.putAll(partakersInBD);

				partakersAdded.stream().forEach(dto -> {
					if (dto.getEmail() != null) {
						ClimatePartaker partakerSaved = partakersInBD.get(dto.getEmail());
						// Si no existe el participante lo creamos
						if (partakerSaved == null) {
							listToSave.add(createClimatePartaker(dto, model.get(), configuration));
						} else {
							// Borramos del Map los participantes que si vienen en la lista nueva, porque al
							// final borramos lo que quede en esta lista
							partakersToDelete.remove(partakerSaved.getEmail());
						}
					}
				});

				if (!partakersToDelete.isEmpty()) {
					// Eliminamos los registros que quedaron en el map (que no vienen en la nueva lista)
					Collection<ClimatePartaker> partTDelete = partakersToDelete.values().stream()
							.filter(p -> p.getPartakerState() != SurveyParticipanState.FINISHED)
							.collect(Collectors.toList());
					partTDelete.stream().forEach(dele -> {
						dele.setState(EntityState.DELETED);
					});
					climatePartakerRepository.saveAll(partTDelete);
				}

				if (!listToSave.isEmpty()) {
					climatePartakerRepository.saveAll(listToSave);
				}
			} else {
				// Obtiene participantes según el empleado asociado
				Map<Object, ClimatePartaker> partakersInBD = partakers.stream()
					    .collect(Collectors.toMap(
					        partaker -> partaker.getEmployee().getId(),  
					        Function.identity(),                         
					        (existing, replacement) -> existing          
					    ));
				
				Map<Object, ClimatePartaker> partakersToDelete = new HashMap<Object, ClimatePartaker>();
				partakersToDelete.putAll(partakersInBD);
				Set<Object> idsToDelete = new HashSet<>();
				partakersAdded.stream().forEach(dto -> {
					if (dto.getEmployeeId() != null) {
						ClimatePartaker partakerSaved = partakersInBD.get(dto.getEmployeeId());
						// Si no existe el participante lo creamos
						if (partakerSaved == null) {
							listToSave.add(createClimatePartaker(dto, model.get(), configuration));							
						} else {
							// Se verifican los participantes que se van a eliminar
							if (partakerSaved.getPartakerState() != SurveyParticipanState.FINISHED) {
								idsToDelete.add(partakerSaved.getEmployee().getId());								
							}
						}
					}
				});
				
				// Eliminar los participantes del mapa
				idsToDelete.forEach(partakersToDelete::remove);
				
				if (!partakersToDelete.isEmpty()) {
					// Eliminamos los registros que quedaron en el map (que no vienen en la nueva lista)
					Collection<ClimatePartaker> partTDelete = partakersToDelete.values().stream()
							.filter(p -> p.getPartakerState() != SurveyParticipanState.FINISHED)
							.collect(Collectors.toList());
					partTDelete.stream().forEach(dele -> {
						dele.setState(EntityState.DELETED);
					});
					climatePartakerRepository.saveAll(partTDelete);
				}
				
				if (!listToSave.isEmpty()) {
					climatePartakerRepository.saveAll(listToSave);
				}
			}
		}
	}

	/**
	 * Se encarga de la creación de un participante de una encuesta de clima
	 */
	private ClimatePartaker createClimatePartaker(ClimatePartakerDTO partakerDto, ClimateModel model, Optional<ClimateConfiguration> configuration) {
		ClimatePartaker partaker = new ClimatePartaker();
		partaker.setName(partakerDto.getName());
		partaker.setEmail(partakerDto.getEmail());
		partaker.setModel(model);
		if (partakerDto.getPartakerState() != null) {
			partaker.setPartakerState(partakerDto.getPartakerState());
		} else {
			partaker.setPartakerState(SurveyParticipanState.CREATED);
		}

		if (partakerDto.getEmployeeId() != null && configuration.isPresent()
				&& configuration.get().getAnonymous() != null
				&& !configuration.get().getAnonymous()) {

			String link = this.generateNewLinkSurveyClimate();
			Employee employee = new Employee();
			employee.setId(partakerDto.getEmployeeId());
			partaker.setEmployee(employee);
			try {
				String linkEmployee = link + "&uid=" + Base64.getEncoder()
						.encodeToString(String.valueOf(employee.getId()).getBytes("utf-8"));
				partaker.setLink(linkEmployee);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return partaker;
	}
	
	/**
	 *  Generar link de encuesta de clima
	 * */
	public String generateLinkSurveyClimate(Long modelId) {
		Optional<ClimateConfiguration> conf = this.climateConfigurationRepository.findByModelId(modelId);
		if(conf.isPresent())
			return conf.get().getLinkAnonymous() != null ? conf.get().getLinkAnonymous() : this.generateNewLinkSurveyClimate();
		return this.generateNewLinkSurveyClimate();
	}

	public String generateNewLinkSurveyClimate() {
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
		    return INITIAL_LINK.concat(generatedString) ;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		} // assuming Unix
	}


	/**
	 * Se ejecuta tarea programada para envio de correo a evaluaciones de clima que van a comenzar
	 */
	public BatchProcessResult sendReminderToEvaluationsToStart() {
		
		AtomicLong affectedObjectsCount = new AtomicLong(0);
		BatchProcessResult result = BatchProcessResult.newOKBatchProcessResult(affectedObjectsCount.get());
		Optional<List<ClimateConfiguration>> configurations = climateConfigurationRepository.getClimateConfigurationToReminderFirstTime();
		if (configurations.isPresent()) {
			
			configurations.get().parallelStream().forEach(configuration -> {
				sendReminderEmail(configuration, configuration.getSubjectEmail(), configuration.getContentEmail(), null, false);
				affectedObjectsCount.getAndIncrement();
			});
		}
		result.setAffectedObjectsCount(affectedObjectsCount.get());
		return result;
	}


	/**
	 * Obtiene los participantes que existen en una encuesta de clima
	 * @param modelId Identificador del modelo de clima
	 */
	public List<ClimatePartakerDTO> getClimatePartakers(Long modelId) {
		
		List<ClimatePartakerDTO> partakersList = new ArrayList<ClimatePartakerDTO>();
		
		Climate evaluation = this.climateEvaluationRepository.findEvaluationByModelId(modelId);
		
		// Consulta participantes de la tabla climate_partaker
		List<ClimatePartaker> partakers = climatePartakerRepository.findByModelIdEmailNotNull(modelId);
		partakers.stream().forEach(p -> {
			ClimatePartakerDTO partakerDTO = new ClimatePartakerDTO();
			if (p.getEmployee() != null) {
				Optional<Employee> employeeOpt = employeeRepository.findById(p.getEmployee().getId());
				p.setEmail(employeeOpt.get().getPerson().getEmail());
				climatePartakerRepository.save(p);				
				partakerDTO.setEmail(employeeOpt.get().getPerson().getEmail());
				partakerDTO.setEmployeeId(p.getEmployee().getId());
			} else {				
				partakerDTO.setEmail(p.getEmail());
			}
			partakerDTO.setName(p.getName());
			partakerDTO.setPartakerState(p.getPartakerState());
			partakersList.add(partakerDTO);
		});
		
		List<Questionnaire> questionnaires = new ArrayList<Questionnaire>();
		
		//Si no trae articipantes del nuevo modelo, busca por el antiguo
		if (partakers.size() == 0) {
			questionnaires = questionnaireRepository.getQuestionnaireByEvaluationAndActives(evaluation.getId());
			questionnaires.forEach(q -> {
				ClimatePartakerDTO partakerDTO = new ClimatePartakerDTO();
				Optional<Employee> employeeOpt = employeeRepository.findById(q.getEvaluated().getId());
				partakerDTO.setEmail(employeeOpt.get().getPerson().getEmail());
				partakerDTO.setName(q.getEvaluated().getPerson().getName());
				partakerDTO.setEmployeeId(q.getEvaluated().getId());
				if(q.getState().toString().equals("ACTIVE") || q.getState().toString().equals("ASSIGNED")) {
					partakerDTO.setPartakerState(SurveyParticipanState.CREATED);
				}else if(q.getState().toString().equals("IN_PROGRESS")) {
					partakerDTO.setPartakerState(SurveyParticipanState.IN_PROGRESS);
				}else {
					partakerDTO.setPartakerState(SurveyParticipanState.FINISHED);
				}
				
				partakersList.add(partakerDTO);
			});
		}	
		
		return partakersList;		
	}


	/**
	 * Evaluaciones de clima asignadas a un colaborador, incluyendo modelo antiguo y nuevo
	 * @param employeeId Identificador de un empleado
	 * @param pageable Paginador
	 */
	@SuppressWarnings("unchecked")
	public <T>PageableResponse<T> getAllSurveyClimateEvaluations(Long employeeId, Pageable pageable) {
		
		List<ClimateEvaluationDTO> results = new ArrayList<ClimateEvaluationDTO>();
		List<Object[]> list = new ArrayList<Object[]>();
		
		PageableResponse<T> pages = new PageableResponse<T>();
		Integer maxResults = null;
		Integer startIndex = null;		
		if (pageable != null) {
			maxResults = pageable.getPageSize();
			startIndex = pageable.getPageSize() * pageable.getPageNumber();
		}
		
		list = modelDao.getAllSurveyClimateEvaluationsByEmployee(employeeId, startIndex, maxResults);
		
		Function<Object[], ClimateEvaluationDTO> mapper = data -> {
			ClimateEvaluationDTO result = new ClimateEvaluationDTO();
			result.setName(getString(data[0]));
			result.setState(getString(data[1]));
			result.setStartDate((Date) data[2]);
			result.setEndDate((Date) data[3]);
			result.setLink(getString(data[4]));
			return result;
		};
		results = list.stream().map(mapper).collect(Collectors.toList());
		
		pages.setElements((List<T>) results);
		pages.setTotal(modelDao.getAllSurveyClimateEvaluationsByEmployee(employeeId, null, null).size());
	
		return pages;
	}
	
	/**
	 * Se ejecuta tarea programada para actualización de estado de evaluaciones según fecha de inicio y cierre
	 */
	public BatchProcessResult updateClimateEvaluationState() {
		
		AtomicLong affectedObjectsCount = new AtomicLong(0);
		BatchProcessResult result = BatchProcessResult.newOKBatchProcessResult(affectedObjectsCount.get());
		Optional<List<ClimateConfiguration>> configurationsToStart = climateConfigurationRepository.getClimateConfigurationAboutToStart();
		if(configurationsToStart.isPresent()) {
			configurationsToStart.get().parallelStream().forEach(configuration -> {
				Climate evaluation = climateEvaluationRepository.findEvaluationByModelId(configuration.getModel().getId());
				evaluation.setEvaluationState2(EvaluationState2.STARTED);
				climateEvaluationRepository.save(evaluation);
				affectedObjectsCount.getAndIncrement();
			});
		}
		
		Optional<List<ClimateConfiguration>> configurationsToFinish = climateConfigurationRepository.getClimateConfigurationAboutToFinish();
		if(configurationsToFinish.isPresent()) {
			configurationsToFinish.get().parallelStream().forEach(configuration -> {
				Climate evaluation = climateEvaluationRepository.findEvaluationByModelId(configuration.getModel().getId());
				evaluation.setEvaluationState2(EvaluationState2.FINISHED);
				climateEvaluationRepository.save(evaluation);
				affectedObjectsCount.getAndIncrement();
			});
		}
		
		result.setAffectedObjectsCount(affectedObjectsCount.get());
		return result;
	}

	/**
	 * Método que reenvía correo de una evaluación de clima a participantes que todavía no han finalizado la encuesta
	 * @param modelId Identificador del modelo
	 * */
	public void forwardEmail(Long modelId, String subject, String content) {

		ClimateModel model = climateModelRepository.getOne(modelId);
		Optional<ClimateConfiguration> configuration = climateConfigurationRepository.findByModelId(modelId);
		if (configuration.isPresent() && model != null) {
			// Actualiza asunto y contenido de correo
			configuration.get().setSubjectReminderEmail(subject);
			configuration.get().setContentReminderEmail(content);
			climateConfigurationRepository.save(configuration.get());
			
			Label labelSubject = this.labelRepository.findByModuleCode("climate", "subject_resend_email");
			String subjectAux = model.getCompany().getLanguageCode().equals("es") ? labelSubject.getSpanish()
					: model.getCompany().getLanguageCode().equals("en") ? labelSubject.getEnglish() : labelSubject.getPortuguese();
			String subjectMail = subject != null ? subject : subjectAux;

			Label labelContent = this.labelRepository.findByModuleCode("climate", "content_resend_email");
			String contentAux = model.getCompany().getLanguageCode().equals("es") ? labelContent.getSpanish()
					: model.getCompany().getLanguageCode().equals("en") ? labelContent.getEnglish() : labelContent.getPortuguese();
			String contentMail = content != null ? content : contentAux;
			
			Company company = model.getCompany();
			ZoneId zoneTimeCompany = company.getCountry() != null ? ZoneId.of(company.getCountry().getZone()) : ZoneId.systemDefault();
			// Convertir la fecha a la zona horaria de la empresa
			ZonedDateTime startDateCompany = configuration.get().getStartDate().withZoneSameInstant(zoneTimeCompany);
			ZonedDateTime endDateCompany = configuration.get().getEndDate().withZoneSameInstant(zoneTimeCompany);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			contentMail = contentMail.replace("#dateStart", startDateCompany.format(formatter));
			contentMail = contentMail.replace("#dateEnd", endDateCompany.format(formatter));
			sendReminderEmail(configuration.get(), subjectMail, contentMail, null, true);

		}
	}
	
	/**
	 * Análisis de los palabras claves de las respuestas a preguntas abiertas
	 */
	private void calculateWordCloud(Climate evaluation, String response) {
		
		String languageCode = evaluation.getCompany().getLanguageCode();
		
		Map<KeyWordDTO, Long> mapCounting=new HashMap<KeyWordDTO, Long>();
	
			try {
				AWSXRay.setGlobalRecorder(AWSXRayRecorderBuilder.defaultRecorder());
			    AWSXRay.beginSegment("AmazonComprehend");
				mapCounting = ComprehendUtil.getSyntax(response, languageCode);

			} catch (Exception e) {
			    throw e;
			} finally {
			    AWSXRay.endSegment();
			}

			List<String> listWords = new ArrayList<String>();
			mapCounting.forEach( (k,v)-> {listWords.add(k.getWord());});
			
			if (listWords.size() > 0) {
				Optional<List<ClimateEvaluationKeyword>> optListCurrentEvaluationKeywords = climateEvaluationKeywordRepository.
						getDuplicateClimateEvaluationKeywordByEvaluationId(evaluation.getId(), listWords);
				
				List<ClimateEvaluationKeyword> wordsToSave=new ArrayList<ClimateEvaluationKeyword>();
				
				mapCounting.forEach( (k,v)-> {
					
					if(optListCurrentEvaluationKeywords.isPresent()) {
						Optional<ClimateEvaluationKeyword> optionalK= optListCurrentEvaluationKeywords.get().stream().filter(word-> word.getWord().equals(k.getWord())).findFirst();
						
						if (optionalK.isPresent()) {
							ClimateEvaluationKeyword keyword=optionalK.get();
							keyword.setCount(keyword.getCount()+v);
							wordsToSave.add(keyword);
						} else {
							
							ClimateEvaluationKeyword keyword=updateOrSaveClimateEvaluationKeyword(evaluation, k.getWord(), k.getType(), v, null);
							wordsToSave.add(keyword);
						}
					} else {
						ClimateEvaluationKeyword keyword=updateOrSaveClimateEvaluationKeyword(evaluation, k.getWord(), k.getType(), v, null);
						wordsToSave.add(keyword);
					}
					
				});
				
				
				// Guarda las nuevas palabras encontradas
				climateEvaluationKeywordRepository.saveAll(wordsToSave);
				
			}
			
		
	}
	
	private ClimateEvaluationKeyword updateOrSaveClimateEvaluationKeyword(Climate evaluation, String text,
			String type, Long count, ClimateEvaluationKeyword cek) {
		
		if(count == null) {
			count = 0l;
		}
		if (cek != null) {
			cek.setCount(cek.getCount() + count);
		} else {
			cek = new ClimateEvaluationKeyword();
			cek.setEvaluation(evaluation);
			cek.setType(type);
			cek.setWord(text);
			cek.setKeyResult(evaluation.getId() + text);
			cek.setCount(count);
		}
		return cek;
	}


	/**
	 * Obtiene los empleados de una empresa, según filtros
	 */
	public List<ClimatePartakerDTO> getEmployeesByCompany(Long modelId, String name, String divisionsId, String jobsId) {
		
		List<ClimatePartakerDTO> employeesList = new ArrayList<ClimatePartakerDTO>();
		Optional<ClimateModel> modelOpt = climateModelRepository.findById(modelId);
		
		if (modelOpt.isPresent()) {
			List<Object[]> employees = new ArrayList<Object[]>();
			employees = modelDao.getEmployeesByCompany(modelOpt.get().getCompany().getId(), name, divisionsId, jobsId);
			employees.stream().forEach(emp -> {
				ClimatePartakerDTO partaker = new ClimatePartakerDTO();
				partaker.setEmployeeId(getLong(emp[0]));				
				partaker.setName(getString(emp[1]));
				partaker.setEmail(getString(emp[2]));
				partaker.setDivisionId(getLong(emp[3]));				
				partaker.setDivisionName(getString(emp[4]));
				partaker.setJobId(getLong(emp[5]));				
				partaker.setJobName(getString(emp[6]));
				employeesList.add(partaker);
			});
		}
		
		return employeesList;
	}	

	
	/**
	 * Método que envía correo de prueba de una evaluación de clima 
	 * 
	 * @param modelId Identificador del modelo
	 */
	public void sendTestEmail(Long modelId, InfoEmailClimateDTO infoEmailClimateDTO) {

		Optional<ClimateModel> climateModel = climateModelRepository.findById(modelId);
		if (climateModel.isPresent()) {
			// Guarda asunto y contenido del correo
			ClimateConfiguration configuration = climateConfigurationRepository.findByModelId(climateModel.get().getId()).get();
			configuration.setSubjectEmail(infoEmailClimateDTO.getSubject());
			configuration.setContentEmail(infoEmailClimateDTO.getContent());
			climateConfigurationRepository.save(configuration);
			
			// Configuración del correo de prueba
			String subject = configuration.getSubjectEmail();
			String content = configuration.getContentEmail();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			
			Company company = climateModel.get().getCompany();
			ZoneId zoneTimeCompany = company.getCountry() != null ? ZoneId.of(company.getCountry().getZone()) : ZoneId.systemDefault();
			// Convertir la fecha a la zona horaria de la empresa
	        ZonedDateTime startDateCompany = configuration.getStartDate().withZoneSameInstant(zoneTimeCompany);
	        ZonedDateTime endDateCompany = configuration.getEndDate().withZoneSameInstant(zoneTimeCompany);
			content = content.replace("#dateStart", startDateCompany.format(formatter));
			content = content.replace("#dateEnd", endDateCompany.format(formatter));
			
			// Realiza envio del correo de prueba a cada uno de los destinatarios
			for (int i = 0; i < infoEmailClimateDTO.getEmailRecipients().length; i++) {				
				sendReminderEmail(configuration, subject, content, infoEmailClimateDTO.getEmailRecipients()[i], false);
			}			
		}
	}

}
