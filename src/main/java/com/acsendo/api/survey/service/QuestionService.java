package com.acsendo.api.survey.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acsendo.api.hcm.enumerations.EntityState;
import com.acsendo.api.survey.dto.QuestionDTO;
import com.acsendo.api.survey.dto.ResponseOptionsDTO;
import com.acsendo.api.survey.dto.TemplateQuestionDTO;
import com.acsendo.api.survey.dto.TemplateTypeResponseOptionsDTO;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.acsendo.api.survey.factory.QuestionDTOFactory;
import com.acsendo.api.survey.factory.ResponseOptionsDTOFactory;
import com.acsendo.api.survey.factory.TemplateQuestionDTOFactory;
import com.acsendo.api.survey.factory.TemplateTypeResponseOptionsDTOFactory;
import com.acsendo.api.survey.model.Questions;
import com.acsendo.api.survey.model.ResponseOptions;
import com.acsendo.api.survey.model.TemplateQuestions;
import com.acsendo.api.survey.model.TemplateTypeResponseOptions;
import com.acsendo.api.survey.repository.QuestionsRepository;
import com.acsendo.api.survey.repository.ReponseOptionsRepository;
import com.acsendo.api.survey.repository.SurveyRepository;
import com.acsendo.api.survey.repository.TemplateQuestionsRepository;
import com.acsendo.api.survey.repository.TemplateTypeResponseOptionsRepository;
import com.acsendo.api.survey.repository.TypeResponseRepository;

@Service
public class QuestionService {

	@Autowired
	private TemplateQuestionsRepository templateQuestionsRepository;
	
	@Autowired
	private TemplateQuestionDTOFactory templateQuestionDTOFactory;
	
	@Autowired
	private TemplateTypeResponseOptionsRepository templateTypeResponseOptionsRepository;
	
	@Autowired
	private TemplateTypeResponseOptionsDTOFactory templateTypeResponseOptionsDTOFactory;
	
	@Autowired
	private QuestionsRepository questionsRepository;
	
	@Autowired
	private SurveyRepository surveyRepository;
	
	@Autowired
	private TypeResponseRepository typeResponseRepository;
	
	@Autowired
	private ReponseOptionsRepository reponseOptionsRepository;
	
	@Autowired
	private ResponseOptionsDTOFactory responseOptionsDTOFactory;
	
	@Autowired
	private QuestionDTOFactory questionDTOFactory;
	
	
	/**
	 * Obtiene y recupera la pregunta base y sus opciones de respuesta según el questionCode
	 * @param questionCode
	 * @return
	 */
	public TemplateQuestionDTO getDetailQuestionBaseService(String questionCode) {
		
		TemplateQuestionDTO templateQuestionObject = new TemplateQuestionDTO();
		
		//Consulta para traer los detalles generales de la pregunta base
		Optional<TemplateQuestions> questionObjectRepository = templateQuestionsRepository.findQuestionByQuestionCode(questionCode);
		
		if (questionObjectRepository.isPresent()) {
			
			TemplateQuestions ques = questionObjectRepository.get();
			templateQuestionObject = templateQuestionDTOFactory.newTemplateQuestion(ques);
			
			List<TemplateTypeResponseOptionsDTO> listTemplateTypeResponseOptions = new ArrayList<TemplateTypeResponseOptionsDTO>();
			//Consulta las opciones de respuesta base que tiene la pregunta base
			Optional<List<TemplateTypeResponseOptions>> listTypeResponseRepository = templateTypeResponseOptionsRepository.findTemplateOptionsResponseByTemplateQuestionId(templateQuestionObject.getId());
			if (listTypeResponseRepository.isPresent()) {
				listTemplateTypeResponseOptions = listTypeResponseRepository.get().
						stream().
						map(templateTypeResponseOptionsDTOFactory::newTemplateResponseOptions).
						collect(Collectors.toList());
			}
			templateQuestionObject.setListTemplateTypeResponse(listTemplateTypeResponseOptions);
			
		}
		
		return templateQuestionObject;
		
	}
	
	/**
	 * Servicio para obtener lista de preguntas base, para pintar el menu
	 * @param competenceId
	 * @return
	 */
	public List<TemplateQuestionDTO> getListTemplateQuestionsService() {
		
		//Consulta para traer lista de preguntas del menu.
		List<TemplateQuestions> listTemplateQuestionRepository = templateQuestionsRepository.findAll();
		
		List<TemplateQuestionDTO> listTemplateTypeResponse = new ArrayList<TemplateQuestionDTO>();
		
		if (listTemplateQuestionRepository != null && !listTemplateQuestionRepository.isEmpty()) {
			for (TemplateQuestions templateOpts : listTemplateQuestionRepository) {
				List<TemplateTypeResponseOptionsDTO> listTemplateTypeResponseOptions = new ArrayList<TemplateTypeResponseOptionsDTO>();
				//Consulta las opciones de respuesta base que tiene la pregunta base
				Optional<List<TemplateTypeResponseOptions>> listTypeResponseRepository = templateTypeResponseOptionsRepository.findTemplateOptionsResponseByTemplateQuestionId(templateOpts.getId());
				if (listTypeResponseRepository.isPresent()) {
					listTemplateTypeResponseOptions = listTypeResponseRepository.get().
							stream().
							map(templateTypeResponseOptionsDTOFactory::newTemplateResponseOptions).
							collect(Collectors.toList());
				}
				listTemplateTypeResponse.add(templateQuestionDTOFactory.newTemplateQuestion(templateOpts, listTemplateTypeResponseOptions));
				
			}			
		}
		
		return listTemplateTypeResponse;
		
	}
	
	
	
	
	/**
	 * Servicio para guardar una pregunta con sus opciones de respuesta
	 * @param questionId
	 * @param questionObject
	 * @return
	 * @throws SurveyException 
	 */
	public QuestionDTO putSaveQuestionService(long surveyId, QuestionDTO questionObject) throws SurveyException {
		
		//Actualiza los campos de la entidad para persistirlos en la BD y devuelve la entidad actualizada
		Questions questionSave = saveAndGetQuestionEntity(surveyId, questionObject);
		questionObject.setId(questionSave.getId());
		
		if (questionSave != null && !questionSave.equals("")) {
			//Procedemos primero a borrar las opciones de respuesta que se encuentran guardadas en bd si hay
			reponseOptionsRepository.deleteListResponseOptionsByQuestionId(questionSave.getId());
			
			//Ahora procedemos a guardar las opciones de respuesta de la pregunta
			if (questionObject.getListResponseOptions() != null && !questionObject.getListResponseOptions().isEmpty()) {
				for (ResponseOptionsDTO optObject : questionObject.getListResponseOptions()) {
					ResponseOptions respOption = saveAndGetResponseOptionEntity(questionSave, optObject);
					if (respOption == null) 
						questionObject.getListResponseOptions().remove(optObject);
					else 
						optObject.setId(respOption.getId());						
					
				}
			}
			
			
		}else {
			throw new SurveyException(SurveyException.QUESTION_NOT_EXISTS);
		}
		
		return questionObject;
		
	}

	/**
	 * Realiza la persistencia de una pregunta en el entity de Question
	 * @param questionObject
	 */
	private Questions saveAndGetQuestionEntity(long surveyId, QuestionDTO questionObject) {
		
		Questions question = new Questions();
		
		// Identificar si es una edicion
		if(questionObject.getId() > 0L) {
			Optional<Questions> optQuestionReporitory = questionsRepository.findById(questionObject.getId());
			if(optQuestionReporitory.isPresent()) {
				question = optQuestionReporitory.get();				
			}
		}else {
			question.setId(-1);
			question.setState(EntityState.ACTIVE);
		}
		question.setSurvey(surveyRepository.findById(surveyId).get());
		question.setTyperesponse(typeResponseRepository.findById(questionObject.getTypeResponseId()).get());
		question.setQuestion(questionObject.getQuestionName());
		question.setQuestionDescription(questionObject.getQuestionDescription());
		question.setQuestionPlaceholder(questionObject.getQuestionPlaceholder() !=null ? questionObject.getQuestionPlaceholder() : "" );
		question.setQuestionCode(questionObject.getQuestionCode());
		question.setRequired(questionObject.isRequired());
		question.setPriority(questionObject.getPriority());
		question.setAttachment(questionObject.getAttachment()!=null? questionObject.getAttachment() : null);
		LocalTime time = LocalTime.MIN;
		question.setTimeQuestions(time.plusSeconds(questionObject.getTimeSeconds()!= 0 ? questionObject.getTimeSeconds() : 0 ));
		question.setTypeSizeImage(questionObject.getTypeSizeImage());
		question = questionsRepository.save(question);
		
		return question;
	}
	
	/**
	 * Realiza la persistencia de la opcion de respuesta de una pregunta
	 * @param questionEntity
	 * @param optObject
	 * @return
	 */
	private ResponseOptions saveAndGetResponseOptionEntity(Questions questionEntity, ResponseOptionsDTO optObject) {
		
		ResponseOptions optionsResponse = new ResponseOptions();
		
		// Identificar si es una edicion
		if(optObject.getId() > 0L) {
			Optional<ResponseOptions> optRespRepository = reponseOptionsRepository.findById(optObject.getId());
			if(optRespRepository.isPresent()) {
				optionsResponse = optRespRepository.get();
				if(optObject.getState().equals("DELETED")) {
					reponseOptionsRepository.deleteById(optObject.getId());
					return null;
				}
			}
		}else {
			optionsResponse.setId(-1);
			optionsResponse.setState(EntityState.ACTIVE);
		}
		optionsResponse.setState(EntityState.valueOf(optObject.getState()));
		optionsResponse.setQuestion(questionEntity);
		optionsResponse.setLabel(optObject.getLabel());
		optionsResponse.setIcon(optObject.getIcon());
		optionsResponse.setValue(optObject.getValue());
		optionsResponse.setAttachment(optObject.getAttachment());
		optionsResponse.setPattern(optObject.getPattern());
		optionsResponse.setPlaceholder(optObject.getPlaceholder());
		optionsResponse.setPriority(optObject.getPriority());
		optionsResponse.setType(optObject.getType());
		optionsResponse.setCorrectAnswer(optObject.isCorrectAnswer());
		optionsResponse.setLinkQuestion(optObject.getLinkQuestion());
		optionsResponse.setTypeSizeImageOption(optObject.getTypeSizeImageOption());
		
		optionsResponse = reponseOptionsRepository.save(optionsResponse);
		
		return optionsResponse;
	}
	
	/**
	 * Realiza el procedimiento de duplicar una pregunta basada en otra
	 * @param questionCloneId
	 * @throws CloneNotSupportedException 
	 * @throws SurveyException 
	 */
	public QuestionDTO getQuestionCloneService(long questionCloneId, String newNameDuplicate, QuestionDTO question) throws CloneNotSupportedException, SurveyException {
		
		//Primero guardamos el question que viene por parámetro para clonarlo correctamente
		putSaveQuestionService(question.getIdSurvey(), question);
		//Consulta la pregunta a clonar
		QuestionDTO questionCloneDto = new QuestionDTO();
		List<ResponseOptionsDTO> listResponseOptions = new ArrayList<ResponseOptionsDTO>();
		Optional<Questions> questionToCloneRepository = questionsRepository.findById(questionCloneId);
		if (questionToCloneRepository.isPresent()) {
			Questions questionToCloneEntity = questionToCloneRepository.get();
			//Duplicamos Questions y realizamos la persistencia en BD
			Questions questionClone = new Questions();
			questionClone = (Questions) questionToCloneEntity.clone();
			questionClone.setId(-1);
			questionClone.setAuditable(null);
			questionClone.setPriority(questionsRepository.countQuestionBySurveyId(questionToCloneEntity.getSurvey().getId())+ 1);
			questionClone.setQuestion(newNameDuplicate);
			questionClone = questionsRepository.save(questionClone);
			Optional<List<ResponseOptions>> responseOptionsEntity = reponseOptionsRepository.findListResponseOptionsByQuestionId(questionToCloneEntity.getId());
			if (responseOptionsEntity.isPresent()) {
				List<ResponseOptions> options = responseOptionsEntity.get();
				for (ResponseOptions resOpt : options) {
					//Duplicamos ResponseOptions de cada pregunta y realizamos la persistencia en BD
					ResponseOptions optionClone = new ResponseOptions();
					optionClone = (ResponseOptions) resOpt.clone();
					optionClone.setId(-1);
					optionClone.setQuestion(questionClone);
					optionClone.setAuditable(null);
					optionClone = reponseOptionsRepository.save(optionClone);
					listResponseOptions.add(responseOptionsDTOFactory.newResponseOptions(optionClone));
				}
			}
			questionCloneDto = questionDTOFactory.newQuestion(questionClone, listResponseOptions);
				
		}
		else {
			throw new SurveyException(SurveyException.QUESTION_NOT_EXISTS);
		}
		return questionCloneDto;
	}
	
	/**
	 * Elimina la pregunta según el id pasado por parámetro
	 * @param questionId
	 * @return
	 * @throws SurveyException
	 */
	public boolean deleteQuestionByIdService(long questionId) throws SurveyException {
		
		boolean response = true;
		Optional<List<ResponseOptions>> resRepository = reponseOptionsRepository.findListResponseOptionsByQuestionIdNotActive(questionId);
		if (resRepository.isPresent()) {
			List<ResponseOptions> resList = resRepository.get();
			for (ResponseOptions res : resList) {
				reponseOptionsRepository.deleteById(res.getId());
			}			
		}
		questionsRepository.deleteById(questionId);		
		
		return response;
	}
	
	/**
	 * Recuperar la pretunta segun el question_id
	 * @return 
	 * @throws SurveyException 
	 */
	public QuestionDTO getQuestionById(long questionId) throws SurveyException {
		Optional<Questions> questionOpt = questionsRepository.findById(questionId);
		List<ResponseOptionsDTO> listResponseOptions = new ArrayList<ResponseOptionsDTO>();
		if (questionOpt.isPresent()) {
			Questions question = questionOpt.get();
			Optional<List<ResponseOptions>> responseOptionsEntity = reponseOptionsRepository.findListResponseOptionsByQuestionId(question.getId());
			if (responseOptionsEntity.isPresent()) {
				listResponseOptions = responseOptionsEntity.get().
															stream().
															map(responseOptionsDTOFactory::newResponseOptions).
															collect(Collectors.toList());
			}
			// retorna la pregunta con sus opciones de respuesta.
			return questionDTOFactory.newQuestion(question, listResponseOptions);
		}else {
			throw new SurveyException(SurveyException.QUESTION_NOT_EXISTS);
		}
	}
	
	/**
	 * Recupera la lista de preguntas con su información general
	 * @param surveyId
	 * @return
	 */
	public List<QuestionDTO> getListQuestionsBySurveyIdService(long surveyId) {
		
		Optional<List<Questions>> listQuestionsRepository = questionsRepository.findQuestionsBySurveyId(surveyId);
		List<QuestionDTO> listQuestions = new ArrayList<QuestionDTO>();
		
		//Guardamos la lista de preguntas en el QuestionDTO
		if (listQuestionsRepository.isPresent()) {
			listQuestions = listQuestionsRepository.get().
					stream().
					map(questionDTOFactory::newQuestion).
					collect(Collectors.toList());
		}
		
		return listQuestions;
		
	}
	
	/**
	 * Recupera la lista de opciones de respuesta de cada una de las preguntas de una encuesta
	 * @param surveyId
	 * @return
	 */
	public List<ResponseOptionsDTO> getListResponseOptionsByQuestionsBySurveyService(long surveyId) {
		
		//Traemos todas las preguntas con sus respectivas opciones de respuesta
		Optional<List<ResponseOptions>> listResponsesOptionsRepository = reponseOptionsRepository.findListQuestionsWithResponseOptions(surveyId);
		
		List<ResponseOptionsDTO> listResponsesOptions = new ArrayList<ResponseOptionsDTO>();
		if (listResponsesOptionsRepository.isPresent()) {
			listResponsesOptions = listResponsesOptionsRepository.get().
					stream().
					map(responseOptionsDTOFactory::newResponseOptions).
					collect(Collectors.toList());					
		}
		return listResponsesOptions;
		
	}
}
