package com.acsendo.api.survey.climate.service;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acsendo.api.climate.model.ClimateConfiguration;
import com.acsendo.api.climate.model.ClimateDemographicField;
import com.acsendo.api.climate.model.ClimateDemographicOption;
import com.acsendo.api.climate.model.ClimateDemographicResponse;
import com.acsendo.api.climate.model.ClimateDimension;
import com.acsendo.api.climate.model.ClimateDimensionQuestion;
import com.acsendo.api.climate.model.ClimateDimensionQuestionResponse;
import com.acsendo.api.climate.model.ClimateEvaluationKeyword;
import com.acsendo.api.climate.model.ClimateFactor;
import com.acsendo.api.climate.model.ClimateModel;
import com.acsendo.api.climate.model.ClimateModelQuestion;
import com.acsendo.api.climate.model.ClimateModelQuestionResponse;
import com.acsendo.api.climate.model.ClimatePartaker;
import com.acsendo.api.climate.model.ClimateResponseOption;
import com.acsendo.api.climate.repository.ClimateConfigurationRepository;
import com.acsendo.api.climate.repository.ClimateDemographicFieldRepository;
import com.acsendo.api.climate.repository.ClimateDemographicOptionRepository;
import com.acsendo.api.climate.repository.ClimateDemographicResponseRepository;
import com.acsendo.api.climate.repository.ClimateDimensionQuestionRepository;
import com.acsendo.api.climate.repository.ClimateDimensionQuestionResponseRepository;
import com.acsendo.api.climate.repository.ClimateDimensionRepository;
import com.acsendo.api.climate.repository.ClimateEvaluationKeywordRepository;
import com.acsendo.api.climate.repository.ClimateFactorRepository;
import com.acsendo.api.climate.repository.ClimateModelQuestionRepository;
import com.acsendo.api.climate.repository.ClimateModelQuestionResponseRepository;
import com.acsendo.api.climate.repository.ClimateModelRepository;
import com.acsendo.api.climate.repository.ClimatePartakerRepository;
import com.acsendo.api.climate.repository.ClimateResponseOptionRespository;
import com.acsendo.api.company.repository.CompanyRepository;
import com.acsendo.api.customReports.dao.ClimateResultsRedshiftDAO;
import com.acsendo.api.evaluation.model.Climate;
import com.acsendo.api.hcm.enumerations.EntityState;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Employee;
import com.acsendo.api.hcm.model.Evaluation;
import com.acsendo.api.hcm.repository.EmployeeRepository;
import com.acsendo.api.hcm.repository.EvaluationRepository;
import com.acsendo.api.hcm.service.EmployeeService;

@Service
public class ClimateMigrationDemoService {
	
	
	@Autowired
	private EvaluationRepository evaluationRepository;
	
	@Autowired
	private CompanyRepository companyRepository;
	
	@Autowired
	private ClimateModelRepository climateModelRepository;
	
	@Autowired
	private ClimateConfigurationRepository climateConfigurationRepository;
	
	@Autowired
	ClimateEvaluationKeywordRepository climateEvaluationKeywordRepository;
	
	@Autowired
	ClimateDemographicFieldRepository climateDemographicFieldRepository;
	
	@Autowired
	ClimateDemographicOptionRepository climateDemographicOptiondRepository;
	
	@Autowired
	ClimateDemographicResponseRepository climateDemographicResponseRepository;
	
	@Autowired
	ClimatePartakerRepository climatePartakerRepository;
	
	@Autowired
	ClimateEvaluationEmployeeService climateEmployeeService;
	
	@Autowired
	ClimateFactorRepository climateFactorRepository;
	
	@Autowired
	ClimateDimensionRepository climateDimensionRepository;
	
	@Autowired
	ClimateDimensionQuestionRepository climateDimensionQuestionRepository;
	
	@Autowired
	ClimateDimensionQuestionResponseRepository climateDimensionQuestionResponseRepository;
	
	@Autowired
	ClimateModelQuestionRepository climateModelQuestionRepository;
	
	@Autowired
	ClimateModelQuestionResponseRepository climateModelQuestionResponseRepository;
	
	@Autowired
	ClimateResponseOptionRespository climateResponseOptionRepository;
	
	@Autowired
	ClimateResultsRedshiftDAO climateRedshiftDao;
	
	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	
	
	public void  cloneEvaluationClimateAndGenerateResults( Long companyDemoBaseId, Long newCompanyId,
			Long userId, Long employeeId) throws CloneNotSupportedException, InterruptedException {
	List<Climate> climates = evaluationRepository.findClimatesByCompanyId(companyDemoBaseId);
		
		for(Climate climateEvaluation: climates){
		
		   Climate climate = cloneEvaluationClimateDemo(climateEvaluation, newCompanyId, userId, employeeId);
		   if(climate!=null){
			 Thread.sleep(12000);
			//Llamar migración de resultados en redshift
		    climateRedshiftDao.executeStoredProcedureClimate(newCompanyId,  climate.getModel().getId(),  climate.getId());
		   }
		}
		
	}
	
	@Transactional
	public Climate cloneEvaluationClimateDemo(Climate climateEvaluation, Long newCompanyId, Long userId, Long employeeId) throws CloneNotSupportedException {
		
		Optional<Company> company = companyRepository.findById(newCompanyId);
			
		Climate newClimate=null;
		
			if( company.isPresent()) {
				
				//FECHAS DE INICIO Y CIERRE
				//DEPENDECIA
				// Clona la evaluación de clima
				newClimate=climateEvaluation.clone();
				newClimate.setAuditable(null);
				newClimate.setId(0);
				newClimate.setCompany(company.get());
				newClimate.setStartDate(new Date());
				newClimate.setUserId(userId);
				if (employeeId == null) {
					Employee employee = employeeRepository.findEmployeeByUserId(userId);
					if (employee != null) {
						newClimate.setCreatedBy(employee.getId());					
					}			
				} else {
					newClimate.setUserId(employeeId);
			}
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.YEAR, 1);
				newClimate.setEndDate(c.getTime());
				//Clonar Modelo
				newClimate.setModel(cloneClimateModel(climateEvaluation.getModel(), company.get()));
				newClimate=evaluationRepository.save(newClimate);
				
				//Clona configuración
				ClimateConfiguration configuration=cloneClimateConfiguration(climateEvaluation.getModel().getId(), newClimate);
				//Clona Participantes
				Map<Long, ClimatePartaker> partakersRelated=cloneClimatePartakers(climateEvaluation.getModel().getId(),configuration.getAnonymous(), 
						newClimate.getModel(), climateEvaluation.getCompany().getId(), newCompanyId);
				
				// Clona Factores, dimensiones, preguntas y respuestas cuantitativas
				cloneClimateFactors(climateEvaluation.getModel().getId(), newClimate.getModel(),  partakersRelated);
			
				//Clona preguntas y respuestas abiertas
				cloneClimateModelQuestion(climateEvaluation.getModel().getId(), newClimate.getModel(), partakersRelated);
				
				// Clona Preguntas Demograficas, opciones y respuestas
				cloneClimateDemographicField(climateEvaluation.getModel().getId(), newClimate.getModel(), partakersRelated);
				
				//Clonar opciones de respuesta
				cloneOptionsResponses(climateEvaluation.getModel().getId(), newClimate.getModel());
				
				//Clonar palabras claves para el diagrama de dispersion
				cloneClimateKeyWords(climateEvaluation.getId(), newClimate);
			}
		
			return newClimate;
		
	}
	
	
	private ClimateModel cloneClimateModel (ClimateModel model, Company company) throws CloneNotSupportedException {
		
		if(model!=null) {
			
			ClimateModel newModel=model.clone();
			newModel.setAuditable(null);
			newModel.setCompany(company);
			newModel.setCompany(company);
			newModel.setId(0);
			return climateModelRepository.save(newModel);
			
		}
		return null;
		
	}
	
	private ClimateConfiguration cloneClimateConfiguration(Long modelId, Climate climate) throws CloneNotSupportedException {
		
		Optional<ClimateConfiguration> configuration= climateConfigurationRepository.findByModelId(modelId);
		if(configuration.isPresent()) {
			ClimateConfiguration newConfiguration=configuration.get().clone();
			newConfiguration.setId(0L);
			newConfiguration.setModel(climate.getModel());
			newConfiguration.setStartDate(ZonedDateTime.ofInstant(climate.getStartDate().toInstant(),
                    ZoneId.systemDefault()));
			newConfiguration.setEndDate(ZonedDateTime.ofInstant(climate.getEndDate().toInstant(),
                    ZoneId.systemDefault()));
			//Si es anonima genera Link nuevo
			if(configuration.get().getAnonymous()) {
				newConfiguration.setLinkAnonymous(climateEmployeeService.generateNewLinkSurveyClimate());
			}
			return climateConfigurationRepository.save(newConfiguration);
			
		}
		
		return null;
	}
	
	private Map<Long, ClimatePartaker>  cloneClimatePartakers(Long modelIdFather, boolean isAnonymous, ClimateModel newModel, Long companyDemoBaseId, Long newCompanyId) {
		
		List<ClimatePartaker> partakers=climatePartakerRepository.findActivePartakersByModelId(modelIdFather);
		
		List<ClimatePartaker> newPartakers=new ArrayList<ClimatePartaker>();
		
		Map<Long, ClimatePartaker> idsRelated=new HashMap<Long, ClimatePartaker>();
		
		String link = climateEmployeeService.generateNewLinkSurveyClimate();
		partakers.stream().forEach(partaker->{
			
			ClimatePartaker newPartaker;
			try {
				newPartaker = partaker.clone();
				newPartaker.setId(0);
				newPartaker.setAuditable(null);
				newPartaker.setModel(newModel);
				newPartaker.setEmail(partaker.getEmail().replaceAll(companyDemoBaseId.toString(), newCompanyId.toString()));
				if(!isAnonymous) {
					String linkEmployee=link+"&uid="+Base64.getEncoder().encodeToString(String.valueOf(partaker.getEmployee().getId()).getBytes("utf-8"));
					newPartaker.setLink(linkEmployee);
				}
				Employee employee=employeeService.findEmployeeByEmail(newPartaker.getEmail(), newCompanyId);
				newPartaker.setEmployee(employee);
				newPartakers.add(newPartaker);
				newPartaker=climatePartakerRepository.save(newPartaker);
				idsRelated.put(partaker.getId(), newPartaker);
				
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		
	   return idsRelated;
		
	}
	
	private void cloneClimateFactors(Long modelIdFather, ClimateModel newModel, Map<Long, ClimatePartaker> partakersRelated) {
		
		Optional<List<ClimateFactor>> factors=climateFactorRepository.findAllByModelIdAndState(modelIdFather , EntityState.ACTIVE);
		
		if(factors.isPresent()) {
			factors.get().stream().forEach(factor->{
				ClimateFactor newFactor;
				try {
					newFactor = factor.clone();
					newFactor.setAuditable(null);
					newFactor.setId(0);
					newFactor.setModel(newModel);
					newFactor=climateFactorRepository.save(newFactor);
					
					cloneClimateDimensions(factor.getId(), modelIdFather, newModel, newFactor, partakersRelated);
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
	}
	
	private void cloneClimateDimensions(Long factorIdFather, Long modelIdFather, ClimateModel newModel, ClimateFactor factor, Map<Long, ClimatePartaker> partakersRelated) {
		
		Optional<List<ClimateDimension>> dimensions=climateDimensionRepository.findByFactorIdAndModelIdAndState(factorIdFather, modelIdFather, EntityState.ACTIVE);
		
		if(dimensions.isPresent()) {
			dimensions.get().stream().forEach(dimension->{
				ClimateDimension newEntity;
				try {
					dimension.setQuestions(null);
					newEntity= dimension.clone();
					newEntity.setAuditable(null);
					newEntity.setId(0);
				    newEntity.setModel(newModel);
				    newEntity.setFactor(factor);
				    newEntity.setQuestions(null);
					newEntity=climateDimensionRepository.save(newEntity);
					
					cloneClimateDimensionQuestion(dimension.getId(), newModel, newEntity, partakersRelated);
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
	}
	
	private void cloneClimateDimensionQuestion(Long dimensionIdFather, ClimateModel newModel, ClimateDimension dimension, Map<Long, ClimatePartaker> partakersRelated) {
		
		Optional<List<ClimateDimensionQuestion>> questions=climateDimensionQuestionRepository.findByDimensionIdAndState(dimensionIdFather, EntityState.ACTIVE);
		
		if(questions.isPresent()) {
			questions.get().stream().forEach(entity->{
				ClimateDimensionQuestion newEntity;
				try {
					newEntity= entity.clone();
					newEntity.setAuditable(null);
					newEntity.setId(0);
				    newEntity.setModel(newModel);
				    newEntity.setDimension(dimension);
					newEntity=climateDimensionQuestionRepository.save(newEntity);
					
					cloneClimateDimensionQuestionResponse(entity.getId(), newEntity, partakersRelated);
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
	}
	
	private void cloneClimateDimensionQuestionResponse(Long questionIdFather, ClimateDimensionQuestion question, Map<Long, ClimatePartaker> partakersRelated) {
		
		Optional<List<ClimateDimensionQuestionResponse>> responses=climateDimensionQuestionResponseRepository.findByQuestionIdAndPartakerNotNull(questionIdFather);
		List<ClimateDimensionQuestionResponse> qResponses=new ArrayList<ClimateDimensionQuestionResponse>();
		
		if(responses.isPresent()) {
			responses.get().stream().forEach(entity->{
				ClimateDimensionQuestionResponse newEntity;
				try {
					newEntity= entity.clone();
					newEntity.setAuditable(null);
					newEntity.setId(0);
				    newEntity.setModel(question.getModel());
				    newEntity.setQuestion(question);
				    newEntity.setQuestionnaire(null);
				    newEntity.setPartaker(partakersRelated.get(entity.getPartaker().getId()));
				    qResponses.add(newEntity);
										
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
		climateDimensionQuestionResponseRepository.saveAll(qResponses);
		
	}
	
	
	private void cloneClimateModelQuestion(Long modelIdFather, ClimateModel newModel, Map<Long, ClimatePartaker> partakersRelated) {
		
		Optional<List<ClimateModelQuestion>> questions=climateModelQuestionRepository.getClimateModelQuestionByModelIdAndState(modelIdFather, EntityState.ACTIVE);
		
		if(questions.isPresent()) {
			questions.get().stream().forEach(entity->{
				ClimateModelQuestion newEntity;
				try {
					newEntity= entity.clone();
					newEntity.setAuditable(null);
					newEntity.setId(0);
				    newEntity.setModel(newModel);
					newEntity=climateModelQuestionRepository.save(newEntity);
					
					cloneClimateModelQuestionResponse(entity.getId(), newEntity, partakersRelated);
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
	}
	
	
	private void cloneClimateModelQuestionResponse(Long questionIdFather, ClimateModelQuestion question, Map<Long, ClimatePartaker> partakersRelated) {
		
		Optional<List<ClimateModelQuestionResponse>> responses=climateModelQuestionResponseRepository.getByQuestionIdAndPartakerActive(questionIdFather);
		List<ClimateModelQuestionResponse> qResponses=new ArrayList<ClimateModelQuestionResponse>();
		
		if(responses.isPresent()) {
			responses.get().stream().forEach(entity->{
				ClimateModelQuestionResponse newEntity;
				try {
					newEntity= entity.clone();
					newEntity.setAuditable(null);
					newEntity.setId(0);
				    newEntity.setModel(question.getModel());
				    newEntity.setQuestion(question);
				    newEntity.setQuestionnaire(null);
				    newEntity.setPartaker(partakersRelated.get(entity.getPartaker().getId()));
				    qResponses.add(newEntity);
										
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
		climateModelQuestionResponseRepository.saveAll(qResponses);
		
	}
	
	
	

	private void cloneClimateDemographicField(Long modelIdFather, ClimateModel newModel, Map<Long, ClimatePartaker> partakersRelated) {
		
		Optional<List<ClimateDemographicField>> questions=climateDemographicFieldRepository.getClimateDemographicFieldByModelIdAndState(modelIdFather, EntityState.ACTIVE);
		
		if(questions.isPresent()) {
			questions.get().stream().forEach(entity->{
				ClimateDemographicField newEntity;
				try {
					entity.setOptions(null);
					newEntity= entity.clone();
					newEntity.setAuditable(null);
					newEntity.setId(0);
				    newEntity.setModel(newModel);
				    newEntity.setOptions(null);
					newEntity=climateDemographicFieldRepository.save(newEntity);
					
					cloneClimateDemographicOptions(entity.getId(), newEntity, partakersRelated);
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
	}
	
	
	private void cloneClimateDemographicOptions(Long fieldIdFather, ClimateDemographicField field, Map<Long, ClimatePartaker> partakersRelated) {
		
		Optional<List<ClimateDemographicOption>> questions=climateDemographicOptiondRepository.findAllByFieldIdAndState(fieldIdFather, EntityState.ACTIVE);
		
		if(questions.isPresent()) {
			questions.get().stream().forEach(entity->{
				ClimateDemographicOption newEntity;
				try {
					newEntity= entity.clone();
					newEntity.setAuditable(null);
					newEntity.setId(0);
				    newEntity.setField(field);
					newEntity=climateDemographicOptiondRepository.save(newEntity);
					
					cloneClimateDemographicResponses(fieldIdFather,entity.getId(), field, newEntity, partakersRelated);
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
	}
	
	
	private void cloneClimateDemographicResponses(Long fieldIdFather, Long optionFatherId, ClimateDemographicField field, ClimateDemographicOption option, Map<Long, ClimatePartaker> partakersRelated) {
		
		Optional<List<ClimateDemographicResponse>> responses=climateDemographicResponseRepository.findByFieldIdAndOptionIdAndStateAndAndPartakerNotNull(fieldIdFather, optionFatherId, EntityState.ACTIVE);
	
		List<ClimateDemographicResponse> resDemog=new ArrayList<ClimateDemographicResponse>();
		
		if(responses.isPresent()) {
			responses.get().stream().forEach(entity->{
				ClimateDemographicResponse newEntity;
				try {
					newEntity= entity.clone();
					newEntity.setAuditable(null);
					newEntity.setId(0);
				    newEntity.setField(field);
				    newEntity.setOption(option);
				    newEntity.setQuestionnaire(null);
				    newEntity.setPartaker(partakersRelated.get(entity.getPartaker().getId()));
				    resDemog.add(newEntity);
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
		climateDemographicResponseRepository.saveAll(resDemog);
		
	}
	
	
	
	
	private void cloneOptionsResponses(Long modelFatherId, ClimateModel model) {
		
		Optional<List<ClimateResponseOption>> options=climateResponseOptionRepository.findAllByModelIdAndState(modelFatherId, EntityState.ACTIVE);
		
	    List<ClimateResponseOption> newOptions=new ArrayList<ClimateResponseOption>();
		
		if(options.isPresent()) {
			options.get().stream().forEach(entity->{
				ClimateResponseOption newEntity;
				try {
					newEntity= entity.clone();
					newEntity.setAuditable(null);
					newEntity.setModel(model);
					newEntity.setId(0);
				    newOptions.add(newEntity);
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
		climateResponseOptionRepository.saveAll(newOptions);
		
	}
	
	
	private void cloneClimateKeyWords(Long evaluationOldId, Evaluation evaluation) {
		
		Optional<List<ClimateEvaluationKeyword>> keys=climateEvaluationKeywordRepository.getClimateEvaluationKeywordByEvaluationId(evaluationOldId);
		
	    List<ClimateEvaluationKeyword> newKeys=new ArrayList<ClimateEvaluationKeyword>();
		
		if(keys.isPresent()) {
			keys.get().stream().forEach(entity->{
				ClimateEvaluationKeyword newEntity;
				try {
					newEntity= entity.clone();
					newEntity.setAuditable(null);
                    newEntity.setEvaluation(evaluation);
                    newEntity.setKeyResult(evaluation.getId()+entity.getWord());
					newEntity.setId(0);
				    newKeys.add(newEntity);
					
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			});
		}
		
		climateEvaluationKeywordRepository.saveAll(newKeys);
		
	}

	

	

}
