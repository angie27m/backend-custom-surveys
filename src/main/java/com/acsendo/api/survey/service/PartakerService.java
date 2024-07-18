package com.acsendo.api.survey.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.hcm.enumerations.EntityState;
import com.acsendo.api.hcm.model.Employee;
import com.acsendo.api.hcm.service.EmployeeService;
import com.acsendo.api.survey.dto.PartakerDTO;
import com.acsendo.api.survey.dto.SurveyDTO;
import com.acsendo.api.survey.exceptions.SurveyException;
import com.acsendo.api.survey.factory.PartakerDTOFactory;
import com.acsendo.api.survey.model.ConfigSurvey;
import com.acsendo.api.survey.model.Partaker;
import com.acsendo.api.survey.model.Survey;
import com.acsendo.api.survey.repository.ConfigSurveyRepository;
import com.acsendo.api.survey.repository.PartakerRepository;
import com.acsendo.api.survey.repository.ResponseRepository;
import com.acsendo.api.survey.repository.SurveyRepository;

@Service
public class PartakerService {

	@Autowired
	private PartakerDTOFactory partakerDTOFactory;
	
	@Autowired
	private PartakerRepository partakerRepository;
	
	@Autowired
	private ConfigSurveyRepository configSurveyRepository;
	
	@Autowired
	private SurveyRepository surveyRepository;
	
	@Autowired
	private ResponseRepository responseRepository;
	
	@Autowired
	private EmployeeService employeeService;
	
	
	/**
	 * Salvar el participante dentro de la encuesta. 
	 * Hay que tener una etapa de validacion en caso que no se tenga una doble participacion 
	 * ya sea por departamento o por correo externo
	 * @param surveyId
	 * @param partakerDTO
	 * @return
	 * @throws SurveyException 
	 * @throws UnsupportedEncodingException 
	 */
	public SurveyDTO savePartakerFor(Long surveyId, List<PartakerDTO> listPartakerDTO) throws SurveyException, UnsupportedEncodingException {
		Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
		SurveyDTO surveyDto = new SurveyDTO();
		if(surveyOpt.isPresent()) {
			Optional<ConfigSurvey> configSurvey = configSurveyRepository.getconfigSurveyBySurveyId(surveyId);
			
			if(configSurvey.isPresent()) {
				

				if(listPartakerDTO.size() > 10) {
					
					
					Thread newThread = new Thread(() -> {
					    try {
					    	//Iniciar el proceso Asyncrono, debido a que son demasiados participantes y tardará un rato en cargar
//							Optional<MailTemplate> templateOpt = emailService.getEmailTemplate("loadCompetenceModelStart", surveyOpt.get().getCompany().getLanguageCode());
//							if(templateOpt.isPresent()) {
//								String contentEmail = emailService.buildTemplateWithData(params, templateOpt.get().getContent());
//								String emailSend = companyService.getCompanyAdminByUserId(newModel.getUserAdmin());
//								emailService.sendEmail(contentEmail, templateOpt.get().getSubject(), emailSend, null, null, "teamacsendo@crehana.com", surveyOpt.get().getCompany().getId());
//							}
					    	System.out.println("Proceso asyncrono init");
					    	loadPartakers(surveyId, listPartakerDTO);
					    	System.out.println("Proceso asyncrono fin");	
					    	// Una vez finalizado se enviará un correo para notificar que ya cargaron los participantes.
							
//							Optional<MailTemplate> templateOpt = emailService.getEmailTemplate("loadCompetenceModelStart", surveyOpt.get().getCompany().getLanguageCode());
//							if(templateOpt.isPresent() && newModel.getUserAdmin()!=null) {
//								String contentEmail = emailService.buildTemplateWithData(params, templateOpt.get().getContent());
//								String emailSend = companyService.getCompanyAdminByUserId(newModel.getUserAdmin());
//								emailService.sendEmail(contentEmail, templateOpt.get().getSubject(), emailSend, null, null, "teamacsendo@crehana.com", surveyOpt.get().getCompany().getId());
//							}
					    	
						} catch (SurveyException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					    
					});
					newThread.start();
					
					do {
						//System.out.println("Aún con vida el hilo. esperar !!!!");
					} while (newThread.isAlive());
					
					System.out.println("MEsanje finalizado el cargue --------------------------");
					surveyDto.setPartakers(listPartakerDTO);
					surveyDto.setCountPartakers(listPartakerDTO.size());
					return surveyDto;
					
					
					
				}else {
					// Cargue normal sincrono de participantes 
					long listSizeLoaded = loadPartakers(surveyId, listPartakerDTO);
					surveyDto.setPartakers(listPartakerDTO);
					surveyDto.setCountPartakers(listSizeLoaded);
					return surveyDto;
				}
				
				
			}else {
				throw new SurveyException(SurveyException.SURVEY_CONFIG_NOT_EXISTS);
			}
			
		}else {
			throw new SurveyException(SurveyException.SURVEY_NOT_EXISTS);
		}
		
		// HACER ESTE PROCESO ASYNCRONO EN CASO QUE SI SON MAYORES A 300 PARTICPANTES
		
		
		
		
	}

	private long loadPartakers(Long surveyId, List<PartakerDTO> listPartakerDTO) throws SurveyException {

		List<Partaker> listPartakersTosave = new ArrayList<Partaker>();
		Map<String, String>  mapLoadedEmployee = new HashMap<String, String>();
		Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
		
		if(surveyOpt.isPresent()) {
			Optional<ConfigSurvey> configSurvey = configSurveyRepository.getconfigSurveyBySurveyId(surveyId);
			
			if(configSurvey.isPresent()) {
				
				Map<String, Employee>  mapEmployee = employeeService.getMapEmployeebyCompanyAndMail(surveyOpt.get().getCompany().getId());
				
				listPartakerDTO.stream().forEach(partakerDTO -> {
					boolean flagNewPartaker = true;
					
					
					// TODO> DEBEMOS SIEMPRE VALIDAR SI EL CORREO YA ESTA EN LA ENCUESTA.
					if(partakerDTO.getEmail() != null ) {
						// Buscar si el participante ya esta en la encuesta usando el correo.
						
//						Optional<List<Partaker>> existPartaker = partakerRepository.getPartakerByEvaluatedId(surveyId, partakerDTO.getParticipantEvaluated());
						Optional<List<Partaker>> existPartaker = partakerRepository.getPartakerByEmail(surveyId, partakerDTO.getEmail());
						if(existPartaker.isPresent()) {
							// si ya existe debe cambiarlo a ACTIVE y no crear otro participantes
							for (Partaker partaker : existPartaker.get()) {
								flagNewPartaker = false;
								partaker.setState(EntityState.ACTIVE);
								listPartakersTosave.add(partaker);
							}
						}
					}
					
					// Si no encuetra un partaker para ese empleado,  se debe crear el partaker nuevo.
					if(flagNewPartaker) {
						// Si en el mismo cargue hay 2 del mismo correo, no se carga duplicado o N veces
						if(!mapLoadedEmployee.containsKey(partakerDTO.getEmail())) {
							Partaker newPartaker = partakerDTOFactory.newPartakerSave(partakerDTO, mapEmployee);
							newPartaker.setSurvey(surveyOpt.get());
							newPartaker = partakerRepository.save(newPartaker);
							//Completar el link de survey en caso que tenga un evaluatedId, y salvar nuevamente segun el id de la participanción 
							try {
								newPartaker.setLinkSurvey(generateLinkSurveyEmployee(partakerDTO, newPartaker.getId(), configSurvey.get().isAnonymous()));
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							listPartakersTosave.add(newPartaker);
							//Agregar al mapa el participante que ya agrego, evitando que cree duplicamos en el cargue masivo
							mapLoadedEmployee.put(partakerDTO.getEmail(), partakerDTO.getEmail());
	//						newPartaker = partakerRepository.save(newPartaker);
	//						partakerDTO.setId(newPartaker.getId());
	//						partakerDTO = partakerDTOFactory.newPartaker(newPartaker);
						}
					}
					
				});
				
				if (!listPartakersTosave.isEmpty()) {
					partakerRepository.saveAll(listPartakersTosave);
				}
			
			}else {
				throw new SurveyException(SurveyException.SURVEY_CONFIG_NOT_EXISTS);
			}
			
		}else {
			throw new SurveyException(SurveyException.SURVEY_NOT_EXISTS);
		}

		return mapLoadedEmployee.size();
	}

	/**
	 * Generar un link unico para cada participante, con el objetivo de identificar quien esta respondiendo la encuesta.
	 * @param partakerDTO
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private String generateLinkSurveyEmployee(PartakerDTO partakerDTO, long id, boolean anonymous) throws UnsupportedEncodingException {
		return partakerDTO.getLinkSurvey().concat("&uid="+Base64.getEncoder().encodeToString(String.valueOf(id).getBytes("utf-8")));
	}


	/**
	 * hay que eliminar la participacion de la tabla 
	 * hay que validar que no se tengan ya respuestas con ese participante. ya que en ese escenario no se puede eliminar.
	 * @param surveyId
	 * @param partakerId
	 * @return
	 * @throws SurveyException 
	 */
	public Boolean deletePartakerFrom(Long surveyId, Long partakerId) throws SurveyException {
		
		//Validamos si el participante ha respondido la encuesta
		List<PartakerDTO> listPartakers = getListPartakerResponseService(surveyId);
		if (listPartakers.size() > 0) {
			//Procedemos a validar si el participante esta en la lista, si lo esta, procedemos a borrar los resultados
			PartakerDTO activePartaker = listPartakers.stream().filter(partakerResult -> partakerResult.getId() ==  partakerId).findAny().orElse(null);
			//Si encuentra algun participante es porque debemos eliminar las respuestas del partaker
			if (activePartaker != null) {
				responseRepository.deleteResponsesBySurveyIdAndPartakerId(surveyId, partakerId);
			}
		}
		//Segudamente procedemos a eliminar el participante
		partakerRepository.deleteById(partakerId);
		return true;
	}
	
	/**
	 * Lista los participantes que ya respondieron a una encuesta
	 * @param surveyId
	 * @return
	 */
	public List<PartakerDTO> getListPartakerResponseService(long surveyId) {
		
		List<Long> listPartakers = new ArrayList<Long>();
		Optional<List<String[]>> listRepository = responseRepository.getListSurveyThatResponse(surveyId);
		
		if (listRepository.isPresent() && !listRepository.get().isEmpty()) {
			listPartakers = listRepository.get()
								.stream().map(p -> partakerDTOFactory.listPartakerId(p))
								.collect(Collectors.toList());			
		}
		
		List<PartakerDTO> listPartakersNative = new ArrayList<PartakerDTO>();
		List<Partaker> listRepositoryNative = partakerRepository.findAllById(listPartakers);
		if (listRepositoryNative != null && !listRepositoryNative.isEmpty()) {
			listPartakersNative = listRepositoryNative.stream().map(partakerDTOFactory::newPartaker)
								.collect(Collectors.toList());			
		}
		
		return listPartakersNative;
		
	}
	
	/**
	 * Retorna la lista paginada de los participantes específicos de una encuesta
	 * @param surveyId
	 * @param statePartaker
	 * @param idPartaker
	 * @param pageable
	 * @return
	 */
	public PageableResponse<PartakerDTO> getListPartakersSurveyPaginator(long surveyId, String statePartaker, long idPartaker, Pageable pageable) {
		
		PageableResponse<PartakerDTO> response = new PageableResponse<PartakerDTO>();
		List<PartakerDTO> listPartakers = new ArrayList<PartakerDTO>();
		
		//Consulta para traer los participantes de la encuesta según el estado pasado por parámetro
		//Validamos si queremos la consulta específica para un solo participante o para los participantes de un estado en específico
		Optional<Page<Partaker>> listPartakersRepository;
		if (idPartaker > 0) 
			listPartakersRepository = partakerRepository.findPartakersBySurveyAndStatePaginator(surveyId, idPartaker, pageable);
		else
			listPartakersRepository = partakerRepository.findPartakersBySurveyAndStatePaginator(surveyId, statePartaker, pageable);
		
		if (listPartakersRepository.isPresent() && !listPartakersRepository.get().isEmpty()) {
			List<Partaker> listSurveysEntity = listPartakersRepository.get().getContent();
			listPartakers = listSurveysEntity.stream().map(partakerDTOFactory::newPartaker)
					.collect(Collectors.toList());	
			
			response.setTotal((int)(listPartakersRepository.get().getTotalElements()));
			response.setElements(listPartakers);
			
		}
		return response;
		
	}	
		
	
	
}
