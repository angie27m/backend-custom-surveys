package com.acsendo.api.survey.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.acsendo.api.climate.enumerations.SentimentType;
import com.acsendo.api.comprehend.ComprehendUtil;
import com.acsendo.api.employeeextrafield.dao.EmployeeExtraFieldsDAO;
import com.acsendo.api.hcm.dto.KeyWordDTO;
import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.survey.dto.PartakerDTO;
import com.acsendo.api.survey.dto.QuestionDTO;
import com.acsendo.api.survey.dto.ResponseDTO;
import com.acsendo.api.survey.dto.ResponseOptionsDTO;
import com.acsendo.api.survey.dto.ResultsExtraFieldsDTO;
import com.acsendo.api.survey.dto.ResultsGeneralSurveyDTO;
import com.acsendo.api.survey.dto.SurveyResponseDTO;
import com.acsendo.api.survey.enumerations.SurveyParticipanState;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.acsendo.api.survey.factory.PartakerDTOFactory;
import com.acsendo.api.survey.factory.QuestionDTOFactory;
import com.acsendo.api.survey.factory.ResponseDTOFactory;
import com.acsendo.api.survey.factory.ResponseOptionsDTOFactory;
import com.acsendo.api.survey.model.ConfigSurvey;
import com.acsendo.api.survey.model.Partaker;
import com.acsendo.api.survey.model.Questions;
import com.acsendo.api.survey.model.Response;
import com.acsendo.api.survey.model.ResponseOptions;
import com.acsendo.api.survey.model.Survey;
import com.acsendo.api.survey.model.SurveyKeyword;
import com.acsendo.api.survey.repository.ConfigSurveyRepository;
import com.acsendo.api.survey.repository.PartakerRepository;
import com.acsendo.api.survey.repository.QuestionsRepository;
import com.acsendo.api.survey.repository.ReponseOptionsRepository;
import com.acsendo.api.survey.repository.ResponseRepository;
import com.acsendo.api.survey.repository.SurveyKeywordRepository;
import com.acsendo.api.survey.repository.SurveyRepository;
import com.amazonaws.services.comprehend.model.SentimentScore;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;


@Service
public class ResponseService {

	
	@Autowired
	private ResponseRepository responseRepository;
	
	@Autowired
	private SurveyRepository surveyRepository;
	
	@Autowired
	private ResponseOptionsDTOFactory responseOptionsDTOFactory;
	
	@Autowired
	private ConfigSurveyRepository configSurveyRepository;
	
	@Autowired
	private PartakerRepository partakerRepository;
	
	@Autowired
	private QuestionsRepository questionsRepository;
	
	@Autowired
	private SurveyKeywordRepository surveyKeywordRepository;
	
	@Autowired
	PartakerDTOFactory partakerDTOFactory;
	
	@Autowired
	QuestionDTOFactory questionDTOFactory;
	
	@Autowired
	ResponseDTOFactory responseDTOFactory;
	
	@Autowired
	ReponseOptionsRepository reponseOptionsRepository;
	
	@Autowired
	private EmployeeExtraFieldsDAO employeeExtraFieldsDAO;
	
	
	/**
	 * Salvar las respuestas de una encuesta.
	 * @param questionId
	 * @return
	 * @throws SurveyException
	 */
	public boolean postSaveResponseToSurvey(long surveyId , SurveyResponseDTO surveyResponseDTO) throws SurveyException {
		
		boolean response = true;
		// Validar si existe la encuesta
		Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
		Optional<ConfigSurvey> confSurveyOpt = configSurveyRepository.getconfigSurveyBySurveyId(surveyId);
		
		if(surveyOpt.isPresent() && confSurveyOpt.isPresent()) {
			Survey survey = surveyOpt.get();
			ConfigSurvey config = confSurveyOpt.get();
			
			// Salvar las respuestas en la base de datos 			
			List<Response> listResponseOptions = surveyResponseDTO.getResponses().
															stream().
															map(responseOptionsDTOFactory::newResponseToSurvey).
															collect(Collectors.toList());
			
			//Tener un participante de Temp, para evitar crear más de un participante.
			Partaker partakerTemp = null;
			for (Response response2 : listResponseOptions) {
				if(response2.getPartaker()!=null) {
					// actaulizar el estado del participante a FINISHED
					Partaker partaker =  partakerRepository.getOne(response2.getPartaker().getId());
					partaker.setParticipantState(SurveyParticipanState.FINISHED);
					partaker.setDateResponseInitial(surveyResponseDTO.getDateResponseInitial());
					partaker.setDateResponseFinal(surveyResponseDTO.getDateResponseFinal());
					// si la encuesta es de anonimato, debemos borrar rastro del partaker 
					if(config.isAnonymous()) {
						partaker.setName("");
						partaker.setEmail("");
						partaker.setParticipantEvaluator(0L);
					}
					//Actualizar el participante
					partakerRepository.save(partaker);
				}else {
					if(partakerTemp==null) {
						// solo se ejecuta 1 sola vez.
						// En caso que no se recupere algun participante (El sistema creará uno para poder almacenar la respuesta)
						Partaker newPartaker = partakerDTOFactory.newPartakertoSaveResponses(survey, config, surveyResponseDTO);
						newPartaker = partakerRepository.save(newPartaker);
						partakerTemp = newPartaker;
					}
					// asociar a la respuesta el mismo partaker creado desde la iteración 1
					response2.setPartaker(partakerTemp);
				}
				String questionCode = questionsRepository.getQuestionCodeByQuestionId(response2.getQuestionId());
				if (questionCode.equalsIgnoreCase("TEXTLONG")) {
					// Calcula los sentimientos de las preguntas de parrafo
					Response responseFinal = calculateSentiments(response2, survey.getCompany().getLanguageCode());
					// Salvar la respuesta 
					responseRepository.save(responseFinal);
					// Calcula palabras claves de la respuesta
					this.calculateWordCloud(survey, response2.getResponseOptionValue().trim());
				} else {
					// Salvar la respuesta 
					responseRepository.save(response2);
				}	
				
			}		
		}else {
			throw new SurveyException(SurveyException.SURVEY_NOT_EXISTS);
		}
		
		
		return response;
	}
	
	/**
	 * Realiza las consultas generales para la pagina de resultados de encuestas
	 * @param surveyId
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public ResultsGeneralSurveyDTO getGeneralResultsService(long surveyId) throws InterruptedException, ExecutionException {
		
		ResultsGeneralSurveyDTO objResponse = new ResultsGeneralSurveyDTO();
		Survey survGeneral = surveyRepository.findById(surveyId).get();
		objResponse.setIdSurvey(surveyId);
		objResponse.setNameSurvey(survGeneral.getTitle());
		objResponse.setDateCreationSurvey(survGeneral.getAuditable().getCreatedDate());
		objResponse.setTotalResponse(partakerRepository.countParticipantsFinishedSurveyBySurveyId(surveyId));
		objResponse.setNumQuestions(questionsRepository.countQuestionBySurveyId(surveyId));
		objResponse.setOwnerType(survGeneral.getOwnerType().toString());
		objResponse.setTimeResponse(partakerRepository.promDateInitialAndFinalResponseSurveys(surveyId).get());
		
		return objResponse;
		
	}
	
	/**
	 * Obtiene los resultados de cada pregunta que tiene una encuesta
	 * @param surveyId
	 * @return
	 */
	public List<QuestionDTO> getResultsDetailsByQuestionsOfSurveyService(long surveyId) {
		
		Optional<List<Questions>> listQuestionsRepository = questionsRepository.findQuestionsBySurveyId(surveyId);
		List<QuestionDTO> listQuestions = new ArrayList<QuestionDTO>();
		
		//Primero guardamos la lista de preguntas en el QuestionDTO
		if (listQuestionsRepository.isPresent()) {
			listQuestions = listQuestionsRepository.get().
					stream().
					map(questionDTOFactory::newQuestion).
					collect(Collectors.toList());
			
			//Traemos todas las preguntas con sus respectivas opciones de respuesta
			Optional<List<ResponseOptions>> listResponsesOptionsRepository = reponseOptionsRepository.findListQuestionsWithResponseOptions(surveyId);
			
			List<ResponseOptionsDTO> listResponsesOptions = new ArrayList<ResponseOptionsDTO>();
			if (listResponsesOptionsRepository.isPresent()) {
				listResponsesOptions = listResponsesOptionsRepository.get().
						stream().
						map(responseOptionsDTOFactory::newResponseOptions).
						collect(Collectors.toList());					
			}
			
			//Traemos todas las preguntas con las respuestas que se han dado
			Optional<List<Response>> listResponsesQuestionRepository = responseRepository.findListQuestionsWithResponseOptions(surveyId);
			List<ResponseDTO> listResponsesQuestion = new ArrayList<ResponseDTO>();
			if (listResponsesQuestionRepository.isPresent()) {
				listResponsesQuestion = listResponsesQuestionRepository.get().
						stream().
						map(responseDTOFactory::newResponse).
						collect(Collectors.toList());					
				
			}
			
			//Procedemos a consultar las respuestas por cada pregunta para guardarlas en la lista de responseResultsList de cada QuestionDTO
			List<ResponseOptionsDTO> lisResOptionDuplicate = new ArrayList<ResponseOptionsDTO>(listResponsesOptions);
			for (QuestionDTO question : listQuestions) {
				List<ResponseOptionsDTO> listResponsesOptionsIterate = new ArrayList<ResponseOptionsDTO>();
				List<ResponseDTO> listResponsesQuestionIterate = new ArrayList<ResponseDTO>();
				
				//Iteramos las opciones de respuesta de cada pregunta
				listResponsesOptions.stream().forEach((resOpt) -> {
					if(resOpt.getQuestionId() == question.getId()) {
						listResponsesOptionsIterate.add(resOpt);
					}
				});
				
				//Iteramos las respuestas de cada pregunta
				listResponsesQuestion.stream().forEach((resOpt) -> {
					if(resOpt.getQuestionId() == question.getId()) {
						listResponsesQuestionIterate.add(resOpt);
						if (resOpt.getResponseOptionsId() > 0 ) {
							ResponseOptionsDTO dtoObj = lisResOptionDuplicate.stream()
									  .filter(option -> option.getId() == resOpt.getResponseOptionsId())
									  .findAny()
									  .orElse(null);
							resOpt.setResponseOption(dtoObj);						
						}
					}
				});
				
				//Vamos borrando las opciones de respuesta que ya hemos capturado anteriormente
				listResponsesOptions.removeIf(resOpt -> resOpt.getQuestionId() == question.getId());
				
				//Vamos borrando las respuestas que ya hemos capturado anteriormente
				listResponsesQuestion.removeIf(resOpt -> resOpt.getQuestionId() == question.getId());
				
				question.setListResponseOptions(listResponsesOptionsIterate);
				question.setResponseResultsList(listResponsesQuestionIterate);
			}
			
		}
		
		return listQuestions;
		
	}
	
	
	/**
	 * Realiza la eliminación de las respuestas de una encuestas y asimismo actualiza el estado de los participantes a "SENDED"
	 * @param surveyId
	 * @return
	 */
	public boolean deleteResponsesService(long surveyId) {
		boolean res = false;
		//Elimina las respuestas que tiene la encuesta
		responseRepository.deleteResponsesBySurveyId(surveyId);
		//Ahora traemos los participantes de la encuesta y procedemos a borrar los que estan en anónimo y los que no son les cambiamos el estado a SENDED
		Optional<List<Partaker>> partakersRepository = partakerRepository.getListPartakerBySurveyId(surveyId);
		if (partakersRepository.isPresent()) {
			List<Partaker> partakers = partakersRepository.get();
			for (Partaker partaker : partakers) {
				if (partaker.getName() == null && partaker.getEmail() == null) {
					partakerRepository.delete(partaker);
				}else {
					partaker.setParticipantState(SurveyParticipanState.SENDED);
					partakerRepository.save(partaker);
				}				
			}
			res = true;
		}		
		return res;
	}
	
	/**
	 * Obtiene los resultados de cada pregunta que tiene una encuesta por paginador
	 * @param surveyId
	 * @return
	 */
	public PageableResponse<QuestionDTO> getResultsDetailsByQuestionsOfSurveyServicePaginator(long surveyId, Pageable pageable) {
		
		PageableResponse<QuestionDTO> response = new PageableResponse<QuestionDTO>();
		
		Optional<Page<Questions>> listQuestionsRepository = questionsRepository.findQuestionsBySurveyId(surveyId, pageable);
		List<QuestionDTO> listQuestions = new ArrayList<QuestionDTO>();
		
		//Primero guardamos la lista de preguntas en el QuestionDTO
		if (listQuestionsRepository.isPresent()) {
			listQuestions = listQuestionsRepository.get().getContent().
					stream().
					map(questionDTOFactory::newQuestion).
					collect(Collectors.toList());
			
			//Traemos todas las preguntas con sus respectivas opciones de respuesta
			Optional<List<ResponseOptions>> listResponsesOptionsRepository = reponseOptionsRepository.findListQuestionsWithResponseOptions(surveyId);
			
			List<ResponseOptionsDTO> listResponsesOptions = new ArrayList<ResponseOptionsDTO>();
			if (listResponsesOptionsRepository.isPresent()) {
				listResponsesOptions = listResponsesOptionsRepository.get().
						stream().
						map(responseOptionsDTOFactory::newResponseOptions).
						collect(Collectors.toList());					
			}
			
			//Traemos todas las preguntas con las respuestas que se han dado
			Optional<List<Response>> listResponsesQuestionRepository = responseRepository.findListQuestionsWithResponseOptions(surveyId);
			List<ResponseDTO> listResponsesQuestion = new ArrayList<ResponseDTO>();
			if (listResponsesQuestionRepository.isPresent()) {
				listResponsesQuestion = listResponsesQuestionRepository.get().
						stream().
						map(responseDTOFactory::newResponse).
						collect(Collectors.toList());					
				
			}
			
			//Procedemos a consultar las respuestas por cada pregunta para guardarlas en la lista de responseResultsList de cada QuestionDTO
			List<ResponseOptionsDTO> lisResOptionDuplicate = new ArrayList<ResponseOptionsDTO>(listResponsesOptions);
			for (QuestionDTO question : listQuestions) {
				List<ResponseOptionsDTO> listResponsesOptionsIterate = new ArrayList<ResponseOptionsDTO>();
				List<ResponseDTO> listResponsesQuestionIterate = new ArrayList<ResponseDTO>();
				
				//Iteramos las opciones de respuesta de cada pregunta
				listResponsesOptions.stream().forEach((resOpt) -> {
					if(resOpt.getQuestionId() == question.getId()) {
						listResponsesOptionsIterate.add(resOpt);
					}
				});
				
				//Iteramos las respuestas de cada pregunta
				listResponsesQuestion.stream().forEach((resOpt) -> {
					if(resOpt.getQuestionId() == question.getId()) {
						listResponsesQuestionIterate.add(resOpt);
						if (resOpt.getResponseOptionsId() > 0 ) {
							ResponseOptionsDTO dtoObj = lisResOptionDuplicate.stream()
									  .filter(option -> option.getId() == resOpt.getResponseOptionsId())
									  .findAny()
									  .orElse(null);
							resOpt.setResponseOption(dtoObj);						
						}
					}
				});
				
				//Vamos borrando las opciones de respuesta que ya hemos capturado anteriormente
				listResponsesOptions.removeIf(resOpt -> resOpt.getQuestionId() == question.getId());
				
				//Vamos borrando las respuestas que ya hemos capturado anteriormente
				listResponsesQuestion.removeIf(resOpt -> resOpt.getQuestionId() == question.getId());
				
				question.setListResponseOptions(listResponsesOptionsIterate);
				question.setResponseResultsList(listResponsesQuestionIterate);
			}
			
		}
		
		response.setTotal((int)(listQuestionsRepository.get().getTotalElements()));
		response.setElements(listQuestions);
		return response;
		
	}
	
	
	/**
	 * Lista de respuestas que tiene cada pregunta
	 * @param surveyId
	 * @return
	 */
	public List<ResponseDTO> getResponsesByQuestionsBySurveyIdService(long surveyId) {
		
		//Traemos todas las preguntas con las respuestas que se han dado
		Optional<List<Response>> listResponsesQuestionRepository = responseRepository.findListQuestionsWithResponseOptions(surveyId);
		List<ResponseDTO> listResponsesQuestion = new ArrayList<ResponseDTO>();
		if (listResponsesQuestionRepository.isPresent()) {
			listResponsesQuestion = listResponsesQuestionRepository.get().
					stream().
					map(responseDTOFactory::newResponse).
					collect(Collectors.toList());					
			
		}
		
		return listResponsesQuestion;
		
	}
	
	/**
	 * Servicio que se encarga de obtener los campos extra de cada participante que ya respondio a una encuesta específica
	 * @param surveyId
	 * @param companyId
	 * @param listPartakers
	 * @return
	 */
	public ResultsExtraFieldsDTO postExtraFieldsPartakersResponsesService(long surveyId, long companyId, List<PartakerDTO> listPartakers) {
		return employeeExtraFieldsDAO.getMapExtraFieldsByEmployeeSpecific(surveyId, companyId, listPartakers);
	}
	
	/**
	 * Análisis de los palabras claves de las respuestas a preguntas abiertas
	 */
	private void calculateWordCloud(Survey survey, String response) {
		String languageCode = survey.getCompany().getLanguageCode();
		
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
				Optional<List<SurveyKeyword>> optListCurrentEvaluationKeywords = surveyKeywordRepository.
						getDuplicateSurveyKeywordBySurveyId(survey.getId(), listWords);
				
				List<SurveyKeyword> wordsToSave=new ArrayList<SurveyKeyword>();
				
				mapCounting.forEach( (k,v)-> {
					
					if(optListCurrentEvaluationKeywords.isPresent()) {
						Optional<SurveyKeyword> optionalK= optListCurrentEvaluationKeywords.get().stream().filter(word-> word.getWord().equals(k.getWord())).findFirst();
						
						if (optionalK.isPresent()) {
							SurveyKeyword keyword=optionalK.get();
							keyword.setCount(keyword.getCount()+v);
							wordsToSave.add(keyword);
						} else {
							
							SurveyKeyword keyword = updateOrSaveSurveyEvaluationKeyword(survey, k.getWord(), k.getType(), v, null);
							wordsToSave.add(keyword);
						}
					} else {
						SurveyKeyword keyword = updateOrSaveSurveyEvaluationKeyword(survey, k.getWord(), k.getType(), v, null);
						wordsToSave.add(keyword);
					}
					
				});				
				
				// Guarda las nuevas palabras encontradas
				surveyKeywordRepository.saveAll(wordsToSave);
				
			}		
		
	}
	
	
	private SurveyKeyword updateOrSaveSurveyEvaluationKeyword(Survey survey, String text,
			String type, Long count, SurveyKeyword sek) {
		
		if(count == null) {
			count = 0l;
		}
		if (sek != null) {
			sek.setCount(sek.getCount() + count);
		} else {
			sek = new SurveyKeyword();
			sek.setSurvey(survey);
			sek.setType(type);
			sek.setWord(text);
			sek.setKeyResult(survey.getId() + text);
			sek.setCount(count);
		}
		return sek;
	}
	
	/**
	 * Análisis de los sentimientos de la respuesta
	 */
	private Response calculateSentiments(Response responseQuestion, String languageCode){
		try {
			AWSXRay.setGlobalRecorder(AWSXRayRecorderBuilder.defaultRecorder());
		    AWSXRay.beginSegment("AmazonComprehend");
		    // your AWS call
		  	SentimentScore ss = ComprehendUtil.getSentiment(responseQuestion.getResponseOptionValue().trim(), languageCode);
		  	responseQuestion.setPositiveSentimentScore(ss.getPositive().doubleValue()); 
		  	responseQuestion.setNegativeSentimentScore(ss.getNegative().doubleValue()); 
     		responseQuestion.setNeutralSentimentScore(ss.getNeutral().doubleValue()); 
     		responseQuestion.setMixedSentimentScore(ss.getMixed().doubleValue()); 
			SentimentType sentimentType = SentimentType.MIXED;
			if (responseQuestion.getPositiveSentimentScore() >= responseQuestion.getNegativeSentimentScore()
					&& responseQuestion.getPositiveSentimentScore() >= responseQuestion.getNeutralSentimentScore()
					&& responseQuestion.getPositiveSentimentScore() >= responseQuestion.getMixedSentimentScore()) {
				sentimentType = SentimentType.POSITIVE;
			} else if (responseQuestion.getNegativeSentimentScore() >= responseQuestion.getPositiveSentimentScore()
					&& responseQuestion.getNegativeSentimentScore() >= responseQuestion.getNeutralSentimentScore()
					&& responseQuestion.getNegativeSentimentScore() >= responseQuestion.getMixedSentimentScore()) {
				sentimentType = SentimentType.NEGATIVE;
			} else if (responseQuestion.getNeutralSentimentScore() >= responseQuestion.getPositiveSentimentScore()
					&& responseQuestion.getNeutralSentimentScore() >= responseQuestion.getNegativeSentimentScore()
					&& responseQuestion.getNeutralSentimentScore() >= responseQuestion.getMixedSentimentScore()) {
				sentimentType = SentimentType.NEUTRAL;
			}
			responseQuestion.setSentimenType(sentimentType);
		} catch (Exception e) {
		    throw e;
		} finally {
		    AWSXRay.endSegment();
		}		
		
		return responseQuestion;
	}
	
}
