package com.acsendo.api.survey.climate.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acsendo.api.climate.dto.ClimateDimensionDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationTemplateContentDTO;
import com.acsendo.api.climate.dto.ClimateFactorDTO;
import com.acsendo.api.climate.dto.ClimateNPSResponseOptionDTO;
import com.acsendo.api.climate.dto.ClimateTemplateQuestion;
import com.acsendo.api.climate.dto.ClimateTemplateResponseOptionDTO;
import com.acsendo.api.climate.enumerations.EClimateQuestionType;
import com.acsendo.api.climate.factory.ClimateCompanyEvaluationFactory;
import com.acsendo.api.climate.model.ClimateConfiguration;
import com.acsendo.api.climate.model.ClimateDemographicField;
import com.acsendo.api.climate.model.ClimateDemographicOption;
import com.acsendo.api.climate.model.ClimateDependenceQuestion;
import com.acsendo.api.climate.model.ClimateDimension;
import com.acsendo.api.climate.model.ClimateDimensionQuestion;
import com.acsendo.api.climate.model.ClimateFactor;
import com.acsendo.api.climate.model.ClimateModel;
import com.acsendo.api.climate.model.ClimateModelQuestion;
import com.acsendo.api.climate.model.ClimateResponseOption;
import com.acsendo.api.climate.repository.ClimateConfigurationRepository;
import com.acsendo.api.climate.repository.ClimateDemographicFieldRepository;
import com.acsendo.api.climate.repository.ClimateDemographicOptionRepository;
import com.acsendo.api.climate.repository.ClimateDependenceQuestionRepository;
import com.acsendo.api.climate.repository.ClimateDimensionQuestionRepository;
import com.acsendo.api.climate.repository.ClimateDimensionRepository;
import com.acsendo.api.climate.repository.ClimateEvaluationRepository;
import com.acsendo.api.climate.repository.ClimateFactorRepository;
import com.acsendo.api.climate.repository.ClimateModelQuestionRepository;
import com.acsendo.api.climate.repository.ClimateModelRepository;
import com.acsendo.api.climate.repository.ClimateResponseOptionRespository;
import com.acsendo.api.company.repository.CompanyRepository;
import com.acsendo.api.evaluation.model.Climate;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Employee;
import com.acsendo.api.hcm.repository.EmployeeRepository;

@Service
public class ClimateEvaluationService {
	@Autowired
	private ClimateFactorRepository climateFactorRepository;
	@Autowired
	private CompanyRepository companyRepository;
	@Autowired
	private ClimateModelRepository climateModelRepository;
	@Autowired
	private ClimateResponseOptionRespository climateResponseOptionRespository;
	@Autowired
	private ClimateDemographicFieldRepository climateDemographicFieldRepository;
	@Autowired
	private ClimateDimensionRepository climateDimensionRepository;
	@Autowired
	private ClimateModelQuestionRepository climateModelQuestionRepository;
	@Autowired
	private ClimateDemographicOptionRepository climateDemographicOptionRepository;
	@Autowired
	private ClimateDimensionQuestionRepository climateDimensionQuestionRepository;
	@Autowired
	private ClimateEvaluationRepository evaluationRepository;
	@Autowired
	private ClimateConfigurationRepository climateConfigurationRepository;
	@Autowired
	private ClimateDependenceQuestionRepository climateDependenceQuestionRepository;
	@Autowired
	private ClimateCompanyEvaluationFactory climateCompanyEvaluationFactory;	
	@Autowired
	private EmployeeRepository employeeRepository;

	public ClimateEvaluationDTO saveClimateEvaluation(ClimateEvaluationDTO climateEvaluationDTO, Long companyId, Long userId, Long employeeId) {
		Climate climateEvaluationEntity = null;
		climateEvaluationEntity = climateCompanyEvaluationFactory.entityFromClimateEvaluationDTO(climateEvaluationDTO,
				companyId);
		if (climateEvaluationDTO.getEvaluationId() != null) {
			Climate climateEvaluationEntityAux = evaluationRepository
					.findEvaluationById(climateEvaluationDTO.getEvaluationId());
			climateEvaluationEntity.setCreatedBy(climateEvaluationEntityAux.getCreatedBy());
			climateEvaluationEntity.setUserId(climateEvaluationEntityAux.getUserId());
			climateEvaluationEntity.setStartDate(climateEvaluationEntityAux.getStartDate());
			climateEvaluationEntity.setEndDate(climateEvaluationEntityAux.getEndDate());
			if (climateEvaluationEntityAux.getUserId() == null) {
				climateEvaluationEntity.setUserId(userId);
				if (employeeId == null) {
					Employee employee = employeeRepository.findEmployeeByUserId(userId);
					if (employee != null && climateEvaluationEntityAux.getCreatedBy() == null) {				
						climateEvaluationEntity.setCreatedBy(employee.getId());
					}			
				} else {
					if (climateEvaluationEntityAux.getCreatedBy() == null)
						climateEvaluationEntity.setCreatedBy(employeeId);
				}				
			}
		}
		climateEvaluationEntity = evaluationRepository.save(climateEvaluationEntity);
		climateEvaluationDTO.setEvaluationId(climateEvaluationEntity.getId());
		return climateEvaluationDTO;
	}
	
	public ClimateEvaluationDTO saveClimateDate(ClimateEvaluationDTO climateEvaluationDTO, Long companyId) {
		Climate climateEvaluationEntity = null;
		if (climateEvaluationDTO.getEvaluationId() != null) {
			 climateEvaluationEntity = evaluationRepository
					.findEvaluationById(climateEvaluationDTO.getEvaluationId());
			climateEvaluationEntity.setStartDate(climateEvaluationDTO.getStartDate());
			climateEvaluationEntity.setEndDate(climateEvaluationDTO.getEndDate());
			climateEvaluationEntity.setEvaluationState2(climateEvaluationDTO.getEvaluationState2());
		}
		climateEvaluationEntity = evaluationRepository.save(climateEvaluationEntity);
		climateEvaluationDTO.setEvaluationId(climateEvaluationEntity.getId());
		return climateEvaluationDTO;
	}

	public ClimateEvaluationTemplateContentDTO saveClimateQuestions(Long companyId,
			ClimateEvaluationTemplateContentDTO climateEvaluationContentDTO) {
		// Crear nuevo Modelo
		Optional<Company> company = this.companyRepository.findById(companyId);
		if (!company.isPresent())
			return null;
		ClimateModel climateModel = new ClimateModel(company.get());
		climateModel = this.climateModelRepository.save(climateModel);
		Map<String, ClimateDemographicOption> demographicOptionsMap = new HashMap<>();
		// CrearFactores
		Map<String, ClimateFactor> factorsMap = this.createClimateFactors(climateEvaluationContentDTO, climateModel);
		// CrearDimensiones
		Map<String, ClimateDimension> dimensionsMap = this
				.createClimateDimensionsWithFactors(climateEvaluationContentDTO, factorsMap, climateModel);
		// CrearPreguntas
		this.createClimateQuestions(climateEvaluationContentDTO, dimensionsMap, demographicOptionsMap, climateModel);
		// CrearOpciones de respuesta Clima
		int climateLimits[] = null;
		Map<String, ClimateResponseOption> climateResponseOptions = new HashMap<>();
		if (climateEvaluationContentDTO.getClimateResponses() != null
				&& climateEvaluationContentDTO.getClimateResponses().size() > 0)
			climateLimits = this.createClimateResponses(climateEvaluationContentDTO.getClimateResponses(), climateModel,
					climateResponseOptions);
		// CrearOpciones de respuesta eNPS
		if (climateEvaluationContentDTO.geteNPS() != null
				&& climateEvaluationContentDTO.geteNPS().getDetractorMinLimit() != null) {
			climateResponseOptions = this.createClimateENPSResponses(climateEvaluationContentDTO.geteNPS(),
					climateModel, climateResponseOptions);
			this.createClimateConfiguration(climateEvaluationContentDTO.geteNPS(), climateLimits, climateModel);

		}
		climateEvaluationContentDTO.setModelId(climateModel.getId());
		this.updateDimensionsAndFactors(factorsMap, dimensionsMap, climateEvaluationContentDTO);
		// CrearDependientes
		this.createClimateDependentQuestions(climateEvaluationContentDTO, climateResponseOptions, demographicOptionsMap,
				climateModel);
		return climateEvaluationContentDTO;
	}

	public void createClimateDependentQuestions(ClimateEvaluationTemplateContentDTO climateEvaluationContentDTO,
			Map<String, ClimateResponseOption> climateResponsesOption,
			Map<String, ClimateDemographicOption> demographicResponsesOption, ClimateModel climateModel) {
		this.updateParentId(climateEvaluationContentDTO.getQuestions());
		for (ClimateTemplateQuestion question : climateEvaluationContentDTO.getQuestions()) {
			if (question.getFinalResponses().isEmpty() && (question.getHasParent() == null || !question.getHasParent()))
				continue;
			ClimateDependenceQuestion dependenceQuestionEntity = new ClimateDependenceQuestion();

			if (!question.getFinalResponses().isEmpty())// Dependientes propias(Preguntas que finalizan la encuesta)
				this.saveDependentResponsesOption(question, climateResponsesOption, demographicResponsesOption,
						climateModel, dependenceQuestionEntity, true);

			if (!question.getParentResponses().isEmpty())// Dependientes del padre
				this.saveDependentResponsesOption(question, climateResponsesOption, demographicResponsesOption,
						climateModel, dependenceQuestionEntity, false);
		}

	}

	private void setDependentChild(ClimateTemplateQuestion question,
			ClimateDependenceQuestion dependenceQuestionEntity) {
		if (question.getFinalResponses().isEmpty() || question.getHasParent())
			switch (question.getType()) {
			case CLIMATE_LABEL:
			case ENPS:
				dependenceQuestionEntity
						.setChildENPSClimateQuestion(new ClimateDimensionQuestion(question.getRowNumber()));
				break;
			case OPEN:
				dependenceQuestionEntity.setChildOpenQuestion(new ClimateModelQuestion(question.getRowNumber()));
				break;
			case SOCIODEMOGRAPHIC:
				dependenceQuestionEntity
						.setChildDemographicQuestion(new ClimateDemographicField(question.getRowNumber()));
				break;
			}
	}

	private void saveDependentResponsesOption(ClimateTemplateQuestion question,
			Map<String, ClimateResponseOption> climateResponsesOption,
			Map<String, ClimateDemographicOption> demographicResponsesOption, ClimateModel climateModel,
			ClimateDependenceQuestion dependenceQuestionEntity, Boolean isFinal) {

		List<Map<String, String>> responses = isFinal ? question.getFinalResponses() : question.getParentResponses();

		for (Map<String, String> currentResponseOption : responses) {
			dependenceQuestionEntity = new ClimateDependenceQuestion(dependenceQuestionEntity);
			if (!isFinal)
				this.setDependentChild(question, dependenceQuestionEntity);
			String dependentResponseValue = isFinal
					? currentResponseOption.get(ClimateTemplateQuestion.FINAL_PARENT_RESPONSES_KEY)
					: currentResponseOption.get(ClimateTemplateQuestion.PARENT_RESPONSES_KEY);

			ClimateResponseOption climateOptionEntity = climateResponsesOption.get(dependentResponseValue);
			ClimateDemographicOption demographicOptionEntity = demographicResponsesOption.get(dependentResponseValue);
			ClimateDemographicOption demographicOptionEntityAux = question.getParent() != null
					? demographicResponsesOption.get(dependentResponseValue + "-" + question.getParent().getRowNumber())
					: null;
			demographicOptionEntity = demographicOptionEntity != null ? demographicOptionEntity
					: demographicOptionEntityAux;
			if (climateOptionEntity == null && demographicOptionEntity == null)
				continue;

			Long dependentResponseId = climateOptionEntity != null ? climateOptionEntity.getId()
					: demographicOptionEntity.getId();
			EClimateQuestionType dependentQuestionType = question.getParent() != null ? question.getParent().getType()
					: question.getType();
			switch (dependentQuestionType) {
			case CLIMATE_LABEL:
			case ENPS:
				dependenceQuestionEntity
						.setParentENPSClimateQuestion(isFinal ? new ClimateDimensionQuestion(question.getRowNumber())
								: new ClimateDimensionQuestion(question.getParent().getRowNumber()));
				dependenceQuestionEntity.setParentENPSClimateOption(new ClimateResponseOption(dependentResponseId));
				break;
			case SOCIODEMOGRAPHIC:
				dependenceQuestionEntity
						.setParentDemographicQuestion(isFinal ? new ClimateDemographicField(question.getRowNumber())
								: new ClimateDemographicField(question.getParent().getRowNumber()));
				dependenceQuestionEntity.setParentDemographicOption(new ClimateDemographicOption(dependentResponseId));
				break;
			default:
				break;
			}
			dependenceQuestionEntity.setModel(climateModel);
			this.climateDependenceQuestionRepository.save(dependenceQuestionEntity);
		}

	}

	private void updateParentId(List<ClimateTemplateQuestion> questions) {
		for (ClimateTemplateQuestion currentQuestion : questions) {
			for (ClimateTemplateQuestion child : questions) {
				ClimateTemplateQuestion parent = child.getParent();
				if (child.getHasParent() == null || !child.getHasParent())
					continue;
				if (parent.getRowNumber() != null
						&& !parent.getRowNumber().toString().equals(currentQuestion.getIdBefore()))
					continue;
				parent.setRowNumber(currentQuestion.getRowNumber());
				parent.setIdBefore(currentQuestion.getIdBefore());
			}
		}

	}

	private void updateDimensionsAndFactors(Map<String, ClimateFactor> factorsMap,
			Map<String, ClimateDimension> dimensionsMap,
			ClimateEvaluationTemplateContentDTO climateEvaluationContentDTO) {
		List<ClimateDimensionDTO> dimensionsDTO = new ArrayList<ClimateDimensionDTO>();
		for (ClimateDimension climateDimensionEntity : dimensionsMap.values()) {
			ClimateDimensionDTO dimension = new ClimateDimensionDTO(climateDimensionEntity.getId(),
					climateDimensionEntity.getName());
			dimensionsDTO.add(dimension);
		}
		climateEvaluationContentDTO.setDimensions(dimensionsDTO);

		List<ClimateFactorDTO> factorsDTO = new ArrayList<ClimateFactorDTO>();
		for (ClimateFactor factorEntity : factorsMap.values()) {
			ClimateFactorDTO factor = new ClimateFactorDTO(factorEntity.getId(), factorEntity.getName());
			factorsDTO.add(factor);
		}
		climateEvaluationContentDTO.setFactors(factorsDTO);

	}

	private void createClimateConfiguration(ClimateNPSResponseOptionDTO geteNPS, int[] climateLimits,
			ClimateModel climateModel) {
		ClimateConfiguration config = new ClimateConfiguration();
		config.setModel(climateModel);

		config.setDetractorLabel(geteNPS.getDetractorLabel());
		config.setDetractorMinLimit(geteNPS.getDetractorMinLimit());
		config.setDetractorMaxLimit(geteNPS.getDetractorMaxLimit());

		config.setPromoterLabel(geteNPS.getPromoterLabel());
		config.setPromoterMinLimit(geteNPS.getPromoterMinLimit());
		config.setPromoterMaxLimit(geteNPS.getPromoterMaxLimit());

		config.setNeutralLabel(geteNPS.getNeutralLabel());
		config.setNeutralMinLimit(geteNPS.getNeutralMinLimit());
		config.setNeutralMaxLimit(geteNPS.getNeutralMaxLimit());

		config.setMinScaleLabel(geteNPS.getMinScaleLabel());
		config.setMiddleScaleLabel(geteNPS.getMiddleScaleLabel());
		config.setMaxScaleLabel(geteNPS.getMaxScaleLabel());

		if (climateLimits != null) {
			config.setClimateMinLimit(new Double(climateLimits[0]));
			config.setClimateMaxLimit(new Double(climateLimits[climateLimits.length - 1]));
		}
		this.climateConfigurationRepository.save(config);
	}

	private Map<String, ClimateResponseOption> createClimateENPSResponses(ClimateNPSResponseOptionDTO eNPS,
			ClimateModel climateModel, Map<String, ClimateResponseOption> climateResponseOptions) {
		for (int i = eNPS.getDetractorMinLimit().intValue(); i <= eNPS.getPromoterMaxLimit(); i++) {
			ClimateResponseOption climateResponseOption = new ClimateResponseOption();
			climateResponseOption.setModel(climateModel);
			climateResponseOption.setValue(i);
			climateResponseOption.setPercentage((i * 100) / eNPS.getPromoterMaxLimit().intValue());
			climateResponseOption = this.climateResponseOptionRespository.save(climateResponseOption);
			climateResponseOptions.put(String.valueOf(i), climateResponseOption);
		}
		return climateResponseOptions;
	}

	private int[] createClimateResponses(List<ClimateTemplateResponseOptionDTO> climateResponses,
			ClimateModel climateModel, Map<String, ClimateResponseOption> climateResponseOptions) {
		if (climateResponses == null || climateResponses.isEmpty())
			return null;
		int weigths[] = new int[climateResponses.size()], max = 0;
		for (int i = 0; i < climateResponses.size(); i++) {
			weigths[i] = climateResponses.get(i).getWeigth().intValue();
		}
		Arrays.sort(weigths);
		if (weigths.length <= 0)
			return null;
		max = weigths[weigths.length - 1];

		for (ClimateTemplateResponseOptionDTO responseOption : climateResponses) {
			ClimateResponseOption climateResponseOption = new ClimateResponseOption();
			climateResponseOption.setModel(climateModel);
			climateResponseOption.setKey(responseOption.getResponseLabel());
			climateResponseOption.setValue(responseOption.getWeigth().intValue());
			if (max < 100)
				climateResponseOption.setPercentage((responseOption.getWeigth().intValue() * 100) / max);
			else if (max == 100)
				climateResponseOption.setPercentage(climateResponseOption.getValue());
			climateResponseOption = this.climateResponseOptionRespository.save(climateResponseOption);
			responseOption.setRowNumber(climateResponseOption.getId());
			climateResponseOptions.put(climateResponseOption.getKey(), climateResponseOption);
		}
		return weigths;
	}

	private void createClimateQuestions(ClimateEvaluationTemplateContentDTO climateEvaluationContentDTO,
			Map<String, ClimateDimension> dimensionsMap, Map<String, ClimateDemographicOption> demographicOptions,
			ClimateModel climateModel) {
		Number index = 0;
		for (ClimateTemplateQuestion question : climateEvaluationContentDTO.getQuestions()) {
			EClimateQuestionType climateQuestionType = question.getType();
			switch (climateQuestionType) {
			case CLIMATE_LABEL:
			case ENPS:
				this.createClimateDimensionQuestion(question, dimensionsMap, climateModel, index.intValue());
				break;
			case OPEN:
				this.createClimateModelQuestion(question, dimensionsMap, climateModel, index.intValue());
				break;
			case SOCIODEMOGRAPHIC:
				this.createClimateSDemographicQuestion(question, dimensionsMap, climateModel, index.intValue(),
						demographicOptions);
				break;
			}
			index = index.intValue() + 1;
		}

	}

	private void createClimateSDemographicQuestion(ClimateTemplateQuestion question,
			Map<String, ClimateDimension> dimensionsMap, ClimateModel climateModel, int index,
			Map<String, ClimateDemographicOption> demographicOptions) {
		ClimateDemographicField climateDemographicField = new ClimateDemographicField();
		climateDemographicField.setModel(climateModel);
		climateDemographicField.setName(question.getQuestionText());
		climateDemographicField.setRequired(question.getIsRequired());
		climateDemographicField.setFieldOrder(index);
		if (question.getQuestionDesc() != null && !question.getQuestionDesc().isEmpty())
			climateDemographicField.setDescription(question.getQuestionDesc());
		climateDemographicField = this.climateDemographicFieldRepository.save(climateDemographicField);
		climateDemographicField.setOptions(this.createsDemographicOptions(question.getsDemograficResponses(),
				climateDemographicField, demographicOptions));
		question.setIdBefore(String.valueOf(question.getRowNumber()));
		climateDemographicField = this.climateDemographicFieldRepository.save(climateDemographicField);
		question.setRowNumber(climateDemographicField.getId());
	}

	private List<ClimateDemographicOption> createsDemographicOptions(List<String> sDemograficResponses,
			ClimateDemographicField question, Map<String, ClimateDemographicOption> demographicOptionsMap) {
		List<ClimateDemographicOption> demographicOptions = new ArrayList<>();
		for (String optionResponse : sDemograficResponses) {
			ClimateDemographicOption currentDemographicOption = new ClimateDemographicOption();
			currentDemographicOption.setKey(optionResponse);
			currentDemographicOption.setField(question);
			currentDemographicOption = this.climateDemographicOptionRepository.save(currentDemographicOption);
			demographicOptions.add(currentDemographicOption);
			demographicOptionsMap.put(currentDemographicOption.getKey(), currentDemographicOption);
		}
		return demographicOptions;
	}

	private void createClimateModelQuestion(ClimateTemplateQuestion question,
			Map<String, ClimateDimension> dimensionsMap, ClimateModel climateModel, int index) {
		ClimateModelQuestion climateModelQuestion = new ClimateModelQuestion();
		question.setIdBefore(String.valueOf(question.getRowNumber()));
		climateModelQuestion.setModel(climateModel);
		climateModelQuestion.setFieldOrder(index);
		climateModelQuestion.setQuestion(question.getQuestionText());
		climateModelQuestion.setRequired(question.getIsRequired());
		if (question.getQuestionDesc() != null && !question.getQuestionDesc().isEmpty())
			climateModelQuestion.setDescription(question.getQuestionDesc());
		climateModelQuestion = this.climateModelQuestionRepository.save(climateModelQuestion);
		question.setRowNumber(climateModelQuestion.getId());
	}

	private void createClimateDimensionQuestion(ClimateTemplateQuestion question,
			Map<String, ClimateDimension> dimensionsMap, ClimateModel climateModel, int index) {
		ClimateDimensionQuestion climateDimensionQuestion = new ClimateDimensionQuestion();
		question.setIdBefore(String.valueOf(question.getRowNumber()));
		climateDimensionQuestion.setQuestion(question.getQuestionText());
		climateDimensionQuestion.setDimension(dimensionsMap.get(question.getDimension().getName().trim()));
		climateDimensionQuestion.setFieldOrder(index);
		climateDimensionQuestion.setModel(climateModel);
		climateDimensionQuestion.setRequired(question.getIsRequired());
		if (question.getQuestionDesc() != null && !question.getQuestionDesc().isEmpty())
			climateDimensionQuestion.setDescription(question.getQuestionDesc());
		if (question.getType().equals(EClimateQuestionType.ENPS))
			climateDimensionQuestion.seteNPS(true);
		else if (question.getType().equals(EClimateQuestionType.CLIMATE_LABEL))
			climateDimensionQuestion.seteNPS(false);
		climateDimensionQuestion = this.climateDimensionQuestionRepository.save(climateDimensionQuestion);
		question.setRowNumber(climateDimensionQuestion.getId());
		question.setDimension(new ClimateDimensionDTO(dimensionsMap.get(question.getDimension().getName().trim()).getId(),
				question.getDimension().getName().trim()));
		if (question.getFactor() != null)
			question.setFactor(
					new ClimateFactorDTO(dimensionsMap.get(question.getDimension().getName().trim()).getFactor().getId(),
							question.getFactor().getName().trim()));
	}

	private Map<String, ClimateFactor> createClimateFactors(
			ClimateEvaluationTemplateContentDTO climateEvaluationContentDTO, ClimateModel climateModel) {

		Map<String, ClimateFactor> factorsTemplateContentMap = new HashMap<>();

		for (ClimateTemplateQuestion question : climateEvaluationContentDTO.getQuestions()) {
			if (question.getFactor() == null)
				continue;

			String currentFactorName = question.getFactor().getName().trim();
			if (factorsTemplateContentMap.containsKey(currentFactorName))
				continue;

			ClimateFactor newFactorEntity = new ClimateFactor();
			newFactorEntity.setName(currentFactorName);
			newFactorEntity.setModel(climateModel);
			newFactorEntity = this.climateFactorRepository.save(newFactorEntity);
			factorsTemplateContentMap.put(newFactorEntity.getName(), newFactorEntity);
			question.getFactor().setId(newFactorEntity.getId());
		}
		return factorsTemplateContentMap;
	}

	private Map<String, ClimateDimension> createClimateDimensionsWithFactors(
			ClimateEvaluationTemplateContentDTO climateEvaluationContentDTO,
			Map<String, ClimateFactor> factorsTemplateContentMap, ClimateModel climateModel) {

		Map<String, ClimateDimension> dimensions = new HashMap<>();

		for (ClimateTemplateQuestion question : climateEvaluationContentDTO.getQuestions()) {
			if (question.getDimension() == null || dimensions.containsKey(question.getDimension().getName().trim()))
				continue;
			ClimateDimension currentDimensionEntity = new ClimateDimension();
			currentDimensionEntity.setModel(climateModel);
			currentDimensionEntity.setName(question.getDimension().getName().trim());
			if (!factorsTemplateContentMap.isEmpty() && question.getFactor() != null) {
				ClimateFactor factor = factorsTemplateContentMap.get(question.getFactor().getName().trim());
				currentDimensionEntity.setFactor(factor);
			}
			currentDimensionEntity = this.climateDimensionRepository.save(currentDimensionEntity);
			dimensions.put(currentDimensionEntity.getName(), currentDimensionEntity);
			question.getDimension().setId(currentDimensionEntity.getId());
		}
		return dimensions;
	}

}
