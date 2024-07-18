package com.acsendo.api.survey.climate.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.acsendo.api.climate.dto.ClimateConfigurationDTO;
import com.acsendo.api.climate.dto.ClimateDimensionDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationFilterDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationTemplateContentDTO;
import com.acsendo.api.climate.dto.ClimateFactorDTO;
import com.acsendo.api.climate.dto.ClimateModelDTO;
import com.acsendo.api.climate.dto.ClimateTemplateResponseOptionDTO;
import com.acsendo.api.climate.dto.ClimateTemplateQuestion;
import com.acsendo.api.climate.enumerations.EClimateQuestionType;
import com.acsendo.api.climate.factory.ClimateCompanyEvaluationFactory;
import com.acsendo.api.climate.factory.ClimateDependenceFactory;
import com.acsendo.api.climate.factory.ClimateModelFactory;
import com.acsendo.api.climate.model.ClimateCalculationType;
import com.acsendo.api.climate.model.ClimateConfiguration;
import com.acsendo.api.climate.model.ClimateDemographicField;
import com.acsendo.api.climate.model.ClimateDemographicOption;
import com.acsendo.api.climate.model.ClimateDimension;
import com.acsendo.api.climate.model.ClimateDimensionQuestion;
import com.acsendo.api.climate.model.ClimateFactor;
import com.acsendo.api.climate.model.ClimateModel;
import com.acsendo.api.climate.model.ClimateModelQuestion;
import com.acsendo.api.climate.model.ClimateResponseOption;
import com.acsendo.api.climate.model.ClimateDependenceQuestion;
import com.acsendo.api.climate.repository.ClimateCalculationTypeRepository;
import com.acsendo.api.climate.repository.ClimateConfigurationRepository;
import com.acsendo.api.climate.repository.ClimateDemographicFieldRepository;
import com.acsendo.api.climate.repository.ClimateDemographicOptionRepository;
import com.acsendo.api.climate.repository.ClimateDependenceQuestionRepository;
import com.acsendo.api.climate.repository.ClimateDimensionQuestionRepository;
import com.acsendo.api.climate.repository.ClimateDimensionRepository;
import com.acsendo.api.climate.repository.ClimateFactorRepository;
import com.acsendo.api.climate.repository.ClimateModelQuestionRepository;
import com.acsendo.api.climate.repository.ClimateModelRepository;
import com.acsendo.api.climate.repository.ClimatePartakerRepository;
import com.acsendo.api.climate.repository.ClimateResponseOptionRespository;
import com.acsendo.api.company.repository.CompanyRepository;
import com.acsendo.api.competences.repository.QuestionnaireRepository;
import com.acsendo.api.evaluation.model.Climate;
import com.acsendo.api.evaluation.model.FormEvaluation;
import com.acsendo.api.hcm.enumerations.EntityState;
import com.acsendo.api.hcm.enumerations.EvaluationDType;
import com.acsendo.api.hcm.enumerations.EvaluationState2;
import com.acsendo.api.hcm.enumerations.EvaluationType;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.repository.EvaluationRepository;
import com.acsendo.api.util.PageableUtil;

/**

 * @author jvelandia Servicio de creación de clima
 */
@Service
public class ClimateCreationService {

	@Autowired
	private EvaluationRepository evaluationRepository;

	@Autowired
	private ClimateModelRepository climateModelRepository;

	@Autowired
	private ClimateResponseOptionRespository climateResponseOptionRespository;

	@Autowired
	private ClimateDependenceQuestionRepository climateDependenceQuestionRespository;

	@Autowired
	private ClimateFactorRepository climateFactorRepository;

	@Autowired
	private ClimateDemographicFieldRepository climateDemographicFieldRepository;

	@Autowired
	private ClimateDimensionRepository climateDimensionRepository;

	@Autowired
	private ClimateDimensionQuestionRepository climateDimensionQuestionRepository;

	@Autowired
	private ClimateModelQuestionRepository climateModelQuestionRepository;

	@Autowired
	private ClimateDemographicOptionRepository climateDemographicOptionRepository;

	@Autowired
	private ClimateConfigurationRepository climateConfigurationRepository;

	@Autowired
	private QuestionnaireRepository questionnaireRepository;

	@Autowired
	private ClimatePartakerRepository climatePartakerRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private ClimateCompanyEvaluationFactory climateCompanyEvaluationFactory;

	@Autowired
	private ClimateModelFactory climateModelFactory;

	@Autowired
	private ClimateDependenceFactory climateDependenceFactory;

	@Autowired
	private PageableUtil<ClimateEvaluationDTO> pageableUtil;

	@Autowired
	private ClimateCalculationTypeRepository calculationTypeRepository;

	private Map<Long, ClimateTemplateQuestion> climateQuestionsMap = new HashMap<Long, ClimateTemplateQuestion>();
	private Map<Long, ClimateTemplateQuestion> sQraphicQuestionsMap = new HashMap<Long, ClimateTemplateQuestion>();
	private Map<Long, ClimateTemplateQuestion> openQuestionsMap = new HashMap<Long, ClimateTemplateQuestion>();


	/**
	 * 
	 * @param companyId
	 * @return
	 */
	public List<ClimateModelDTO> getClimateModels(Long companyId) {
		Optional<List<ClimateModel>> modelsFound = climateModelRepository.getClimateModelsByCompanyId(companyId);
		if (!modelsFound.isPresent())
			return new ArrayList<ClimateModelDTO>();
		List<ClimateModelDTO> climatesDTO = this.climateModelFactory.climatesModelToDTO(modelsFound.get());
		return climatesDTO;
	}

	/**
	 * 
	 * @param companyId
	 * @return
	 */
	public List<FormEvaluation> getFormEvaluationsClimate(Long companyId) {
		Optional<List<FormEvaluation>> formEvaluations = evaluationRepository
				.findAllFormEvaluationByCompanyIdAndEvaluationType(companyId, EvaluationType.CLIMATE);
		return formEvaluations.get();
	}


	public Page<ClimateEvaluationDTO> getEvaluationsClimateGeneral(Long companyId, Pageable pageable,
			ClimateEvaluationFilterDTO filterDTO) {

		Optional<List<Object[]>> evaluations = evaluationRepository.getEvaluationsClimateDetailByCompany(companyId);
		if (!evaluations.isPresent())
			return Page.empty(pageable);
		EvaluationState2 evaluationState2 = getStateEvaluationFilter(filterDTO);
		List<ClimateEvaluationDTO> evaluationDetail = this.climateCompanyEvaluationFactory
				.evaluationDetailToClimateEvaluationDTO(evaluations.get(), filterDTO, evaluationState2);
		return pageableUtil.getPageFromList(pageable, evaluationDetail);
	}

	private EvaluationState2 getStateEvaluationFilter(ClimateEvaluationFilterDTO filterDTO) {
		EvaluationState2 evaluationState2 = null;
		if (filterDTO.getState() != null && !filterDTO.getState().equals("")) {
			if (filterDTO.getState().intValue() == 2) {
				evaluationState2 = EvaluationState2.CREATED;
			} else if (filterDTO.getState().intValue() == 3) {
				evaluationState2 = EvaluationState2.STARTED;
			} else if (filterDTO.getState().intValue() == 4) {
				evaluationState2 = EvaluationState2.FINISHED;
			} else if (filterDTO.getState().intValue() == 5) {
				evaluationState2 = EvaluationState2.INCOMPLETE;
			}
		}
		return evaluationState2;
	}

	public List<ClimateEvaluationDTO> getEvaluationsClimate(Long companyId) {
		Optional<List<Object[]>> evaluations = evaluationRepository.getEvaluationsClimateByCompany(companyId);
		if (!evaluations.isPresent())
			return new ArrayList<ClimateEvaluationDTO>();
		List<ClimateEvaluationDTO> evaluationDetail = this.climateCompanyEvaluationFactory
				.evaluationsToClimateEvaluationDTO(evaluations.get());
		return evaluationDetail;
	}

	public ClimateEvaluationDTO getEvaluationsClimateDetail(Long modelId) {
		Optional<List<Object[]>> dimensionsAndFactorsCount = this.climateModelRepository
				.getDimensionsAndFactorsCount(modelId);
		Optional<List<Object>> questionsCount = this.climateModelRepository.getQuestionsCount(modelId);

		if (!dimensionsAndFactorsCount.isPresent() || !questionsCount.isPresent())
			return new ClimateEvaluationDTO();

		Object[] dimensionsAndFactorsCountObj = dimensionsAndFactorsCount.get().get(0);
		List<Object> questionsCountObj = questionsCount.get();

		ClimateEvaluationDTO evaluationDetail = new ClimateEvaluationDTO();
		ClimateModelDTO modelDetail = new ClimateModelDTO();
		modelDetail.setId(modelId);
		modelDetail.setDimensionCount(((BigInteger) dimensionsAndFactorsCountObj[0]).longValue());
		modelDetail.setFactorCount(((BigInteger) dimensionsAndFactorsCountObj[1]).longValue());
		modelDetail.setDimensionQuestionCount(((BigInteger) questionsCountObj.get(0)).longValue());// clima-enps
		modelDetail.setModelQuestionCount(((BigInteger) questionsCountObj.get(1)).longValue());// abiertas
		modelDetail.setDemographicCount(((BigInteger) questionsCountObj.get(2)).longValue());// sociodemograficas
		evaluationDetail.setClimateModel(modelDetail);

		Long surveyedCount = 0L;
		Optional<List<Object[]>> evaluation = evaluationRepository.findByClimateModelId(modelId);

		if (!evaluation.isPresent())
			return evaluationDetail;

		evaluationDetail.setEvaluationId(((BigInteger) evaluation.get().get(0)[0]).longValue());

		if (((String) evaluation.get().get(0)[1]).equalsIgnoreCase(EvaluationDType.FORMEVALUATION.name()))
			surveyedCount = (long) questionnaireRepository
					.getQuestionnairesByEvaluationId(evaluationDetail.getEvaluationId()).get().get(0);
		else if (((String) evaluation.get().get(0)[1]).equalsIgnoreCase(EvaluationDType.CLIMATE.name()))
			surveyedCount = (long) climatePartakerRepository.findActivePartakersByModelId(modelId).size();

		evaluationDetail.setSurveyedCount(surveyedCount);
		return evaluationDetail;
	}

	public ClimateConfigurationDTO getClimateConfiguration(Long companyId, Long modelId) {
		Optional<ClimateConfiguration> confEntityObj = this.climateConfigurationRepository.findByModelId(modelId);
		ClimateConfiguration confEntity = new ClimateConfiguration();
		if (confEntityObj.isPresent()) {
			confEntity = confEntityObj.get();
		}
		return this.climateModelFactory.entityToConfiguration(confEntity);
	}

	public ClimateConfigurationDTO saveClimateConfiguration(Long companyId, ClimateConfigurationDTO config) {
		System.out.println("entro a modificar o guardar la config");
		Optional<Company> company = companyRepository.findById(companyId);
		if (company.isPresent()) {
			ClimateConfiguration confEntity = this.climateModelFactory.configurationToEntity(config, company.get());
			confEntity = climateConfigurationRepository.saveAndFlush(confEntity);
			config.setId(confEntity.getId());
		}
		return config;
	}

	/**
	 * Duplica una evaluacion de clima a a partir de su Id
	 */
	public ClimateEvaluationTemplateContentDTO duplicateEvaluationClimateByDTO(Long companyId, Long evaluationId) {
		Number modelId = null;
		this.climateQuestionsMap = new HashMap<Long, ClimateTemplateQuestion>();
		this.sQraphicQuestionsMap = new HashMap<Long, ClimateTemplateQuestion>();
		this.openQuestionsMap = new HashMap<Long, ClimateTemplateQuestion>();
		Optional<FormEvaluation> evaluationOp = evaluationRepository.findEvaluationById(evaluationId);
		Optional<Climate> climateEvaluationOp = evaluationRepository.findClimateEvaluationById(evaluationId);
		if (evaluationOp.isPresent() && evaluationOp.get().getModel() != null)
			modelId = evaluationOp.get().getModel().getId();
		else if (climateEvaluationOp.isPresent() && climateEvaluationOp.get().getModel() != null)
			modelId = climateEvaluationOp.get().getModel().getId();

		ClimateConfigurationDTO conf = new ClimateConfigurationDTO();
		
		// Copiar opciones de respuesta de clima
		List<ClimateTemplateResponseOptionDTO> climateResponseOptions =  new ArrayList<>(findResponseOptions(modelId));

		// Copiar preguntas
		List<ClimateTemplateQuestion> questionsTemplate = fillAllQuestions(modelId);

		// Copiar Configuración
		Optional<ClimateConfiguration> climateconfigOp = climateConfigurationRepository
				.findByModelId(modelId.longValue());
		if (climateconfigOp.isPresent())
			conf = climateModelFactory.entityToConfiguration(climateconfigOp.get());

		ClimateEvaluationTemplateContentDTO duplicateEvaluation = this.getEvaluationContentWithDimensionsAndFactor(
				questionsTemplate, climateResponseOptions, conf);
		duplicateEvaluation.setModelId(modelId);
		// Crear Depentientes
			this.validateDependentQuestion(duplicateEvaluation, findAllDependences(modelId));
		return duplicateEvaluation;
	}

	/**
	 * Encuentra todas las preguntas dependientes de un modelo en particular
	 */
	private List<ClimateDependenceQuestion> findAllDependences(Number modelId) {
		Optional<List<ClimateDependenceQuestion>> climateDependenceQuestionOp = climateDependenceQuestionRespository
				.findAllByModelIdAndState(modelId.longValue(), EntityState.ACTIVE);
		if (!climateDependenceQuestionOp.isPresent())
			return new ArrayList<>();
		return climateDependenceQuestionOp.get();
	}

	private List<ClimateTemplateResponseOptionDTO> findResponseOptions(Number modelId) {
		Optional<List<ClimateResponseOption>> climateResponseOptionOp = climateResponseOptionRespository
				.findAllByModelId(modelId.longValue());
		if (!climateResponseOptionOp.isPresent())
			return new ArrayList<>();
		return this.createClimateResponseOptions(climateResponseOptionOp.get());
	}

	public List<ClimateTemplateQuestion> fillAllQuestions(Number modelId) {
		List<ClimateTemplateQuestion> questionsTemplate = new ArrayList<>();
		// Copiar preguntas de clima/nps
		Optional<List<ClimateDimensionQuestion>> climateDimensionQuestions = this.climateDimensionQuestionRepository
				.findAllByModelIdAndState (modelId.longValue(), EntityState.ACTIVE);
		if (climateDimensionQuestions.isPresent())
			questionsTemplate.addAll(this.createClimateTemplateQuestion(climateDimensionQuestions.get()));

		// Copiar preguntas sociodemograficas
		Optional<List<ClimateDemographicField>> climateDemographicFieldOp = this.climateDemographicFieldRepository
				.getClimateDemographicFieldByModelIdAndState(modelId.longValue(), EntityState.ACTIVE);
		if (climateDemographicFieldOp.isPresent())
			questionsTemplate.addAll(this.createClimateDemographicTemplateQuestion(climateDemographicFieldOp.get()));

		// Copiar preguntas abiertas
		Optional<List<ClimateModelQuestion>> climateModelQuestionOp = this.climateModelQuestionRepository
				.getClimateModelQuestionByModelIdAndState(modelId.longValue(), EntityState.ACTIVE);
		if (climateModelQuestionOp.isPresent())
			questionsTemplate.addAll(this.createClimateOpenTemplateQuestion(climateModelQuestionOp.get()));
		Collections.sort(questionsTemplate);
		return questionsTemplate;
	}

	private ClimateEvaluationTemplateContentDTO getEvaluationContentWithDimensionsAndFactor(
			List<ClimateTemplateQuestion> questionsTemplate,
			List<ClimateTemplateResponseOptionDTO> climateResponseOptions, ClimateConfigurationDTO conf) {

		List<ClimateDimensionDTO> climateDimensions = new ArrayList<>();
		List<ClimateFactorDTO> climateFactors = new ArrayList<>();
		Map<String, Number> dimensionsMap = new HashMap<>();
		Map<String, Number> factorsMap = new HashMap<>();

		for (ClimateTemplateQuestion question : questionsTemplate) {
			if (question.getDimension() == null)
				continue;

			this.setDimensionIdInClimateTemplateQuestion(climateDimensions, dimensionsMap, question);
			if (question.getFactor() == null)
				continue;

			this.setFactorIdInClimateTemplateQuestion(climateFactors, factorsMap, question);
		}

		return new ClimateEvaluationTemplateContentDTO(questionsTemplate, climateResponseOptions, conf.getEnps(),
				climateDimensions, climateFactors);

	}

	/**
	 * Encontramos el factor id asociado a una pregunta en particular
	 */
	private void setFactorIdInClimateTemplateQuestion(List<ClimateFactorDTO> climateFactors,
			Map<String, Number> factorsMap, ClimateTemplateQuestion question) {
		String factorName = question.getFactor().getName();
		if (!factorsMap.containsKey(factorName)) {
			factorsMap.put(factorName, question.getFactor().getId());
			climateFactors.add(question.getFactor());
		}
	}

	/**
	 * Encontramos el dimension id asociado a una pregunta en particular
	 */
	private void setDimensionIdInClimateTemplateQuestion(List<ClimateDimensionDTO> climateDimensions,
			Map<String, Number> dimensionsMap, ClimateTemplateQuestion question) {
		String dimensionName = question.getDimension().getName();
		if (!dimensionsMap.containsKey(dimensionName)) {
			dimensionsMap.put(dimensionName, question.getDimension().getId());
			climateDimensions.add(new ClimateDimensionDTO(question.getDimension().getId(), dimensionName));
		} else {
			question.getDimension().setId(dimensionsMap.get(dimensionName));
		}
	}

	private List<ClimateTemplateQuestion> createClimateOpenTemplateQuestion(
			List<ClimateModelQuestion> rootClimateModelQuestion) {
		List<ClimateTemplateQuestion> questionsTemplate = new ArrayList<>();
		for (ClimateModelQuestion climateModelQuestion : rootClimateModelQuestion) {
			if(!climateModelQuestion.getState().equals(EntityState.ACTIVE)) continue;
			ClimateTemplateQuestion currentQuestionTemplate = new ClimateTemplateQuestion(climateModelQuestion.getId(),
					climateModelQuestion.getQuestion(),
					climateModelQuestion.getDescription() == null ? "" : climateModelQuestion.getDescription(), null,
					null, EClimateQuestionType.OPEN, null, null, climateModelQuestion.getFieldOrder(),
					climateModelQuestion.getRequired());
			questionsTemplate.add(currentQuestionTemplate);
		}
		this.openQuestionsMap = questionsTemplate.stream()
				.collect(Collectors.toMap(ClimateTemplateQuestion::getId, Function.identity()));
		return questionsTemplate;
	}

	private List<ClimateTemplateQuestion> createClimateDemographicTemplateQuestion(List<ClimateDemographicField> rootsGraphicField) {
		List<ClimateTemplateQuestion> questionsTemplate = new ArrayList<>();
		for (ClimateDemographicField climateDemographicField : rootsGraphicField) {
			if(!climateDemographicField.getState().equals(EntityState.ACTIVE)) continue;
			List<String> sDemograficResponses = new ArrayList<>();
			ClimateTemplateQuestion currentQuestionTemplate = new ClimateTemplateQuestion(
					climateDemographicField.getId(), climateDemographicField.getName(),
					climateDemographicField.getDescription() == null ? "" : climateDemographicField.getDescription(),
					null, null, EClimateQuestionType.SOCIODEMOGRAPHIC, null, null,
					climateDemographicField.getFieldOrder(), climateDemographicField.getRequired());

			sDemograficResponses = climateDemographicField.getOptions().stream()
					.filter(opt -> opt.getState().equals(EntityState.ACTIVE)).map(ClimateDemographicOption::getKey)
					.collect(Collectors.toList());
			currentQuestionTemplate.setsDemograficResponses(sDemograficResponses);
			questionsTemplate.add(currentQuestionTemplate);
		}
		this.sQraphicQuestionsMap = new HashMap<>(questionsTemplate.stream()
				.collect(Collectors.toMap(ClimateTemplateQuestion::getId, Function.identity()))) ;
		return questionsTemplate;
	}

	private List<ClimateTemplateQuestion> createClimateTemplateQuestion(List<ClimateDimensionQuestion> questions) {

		List<ClimateTemplateQuestion> questionsTemplate = new ArrayList<>();

		for (int index = 0; index < questions.size(); index++) {
			ClimateDimensionQuestion currentQuestion = questions.get(index);
			if(!currentQuestion.getState().equals(EntityState.ACTIVE)) continue;
			ClimateTemplateQuestion currentQuestionTemplate = new ClimateTemplateQuestion(currentQuestion.getId(),
					currentQuestion.getQuestion(),
					currentQuestion.getDescription() == null ? "" : currentQuestion.getDescription(),
					currentQuestion.getDimension().getFactor() == null ? null
							: new ClimateFactorDTO(currentQuestion.getDimension().getFactor().getId(), currentQuestion.getDimension().getFactor().getName()),
					new ClimateDimensionDTO(currentQuestion.getDimension().getId(), currentQuestion.getDimension().getName()),
					currentQuestion.geteNPS() ? EClimateQuestionType.ENPS : EClimateQuestionType.CLIMATE_LABEL, null,
					null, currentQuestion.getFieldOrder(), currentQuestion.getRequired());
			questionsTemplate.add(currentQuestionTemplate);

		}
		this.climateQuestionsMap = questionsTemplate.stream()
				.collect(Collectors.toMap(ClimateTemplateQuestion::getId, Function.identity()));
		return questionsTemplate;
	}

	private void validateDependentQuestion(ClimateEvaluationTemplateContentDTO duplicateEvaluation,
			List<ClimateDependenceQuestion> climateDependenceQuestions) {
		if (climateDependenceQuestions.isEmpty())
			return;

		ClimateTemplateQuestion childQuestion = new ClimateTemplateQuestion();
		ClimateTemplateQuestion parentQuestion = null;
		for (ClimateDependenceQuestion dependentQuestion : climateDependenceQuestions) {
			EClimateQuestionType childType = this.climateDependenceFactory.getChildType(dependentQuestion);
			EClimateQuestionType parentType = this.climateDependenceFactory.getParentType(dependentQuestion);
			if (childType != null)
				switch (childType) {
				case CLIMATE_LABEL:
				case ENPS:
					childQuestion = climateQuestionsMap
							.get(this.climateDependenceFactory.getChildId(dependentQuestion));
					break;
				case SOCIODEMOGRAPHIC:
					childQuestion = sQraphicQuestionsMap
							.get(this.climateDependenceFactory.getChildId(dependentQuestion));
					break;
				case OPEN:
					childQuestion = openQuestionsMap.get(this.climateDependenceFactory.getChildId(dependentQuestion));
					break;
				}
			switch (parentType) {
			case CLIMATE_LABEL:
			case ENPS:
				parentQuestion = climateQuestionsMap.get(this.climateDependenceFactory.getParentId(dependentQuestion));
				break;
			case SOCIODEMOGRAPHIC:
				parentQuestion = sQraphicQuestionsMap.get(this.climateDependenceFactory.getParentId(dependentQuestion));
				break;
			case OPEN:
				parentQuestion = openQuestionsMap.get(this.climateDependenceFactory.getParentId(dependentQuestion));
				break;
			}
			if(parentQuestion == null )
			{
				this.updateDependentState(dependentQuestion);
				continue;// existe el registro de dependiente pero eliminaron la pregunta padre
			}
			Map<String, String> option = new HashMap<>();
			if(childQuestion != null)
			{
				childQuestion.setHasParent(Boolean.TRUE);
				childQuestion.setParent(parentQuestion);				
				option.put(ClimateTemplateQuestion.PARENT_RESPONSES_KEY,
						climateDependenceFactory.getResponseOptionLabel(dependentQuestion));
				childQuestion.getParentResponses().add(option);
			}else
			{
				parentQuestion.setHasParent(Boolean.TRUE);
				option.put(ClimateTemplateQuestion.FINAL_PARENT_RESPONSES_KEY,
						climateDependenceFactory.getResponseOptionLabel(dependentQuestion));
				parentQuestion.addFinalResponses(option);
			}

		}
		List<ClimateTemplateQuestion> duplicateQuestions = new ArrayList<ClimateTemplateQuestion>(
				climateQuestionsMap.values());
		duplicateQuestions.addAll(sQraphicQuestionsMap.values());
		duplicateQuestions.addAll(openQuestionsMap.values());
		Collections.sort(duplicateQuestions);
		duplicateEvaluation.setQuestions(duplicateQuestions);
	}

	private void updateDependentState(ClimateDependenceQuestion dependentQuestion) {
		dependentQuestion.setState(EntityState.DELETED);
		this.climateDependenceQuestionRespository.save(dependentQuestion);
	}

	private List<ClimateTemplateResponseOptionDTO> createClimateResponseOptions(
			List<ClimateResponseOption> rootClimateResponseOption) {
		List<ClimateTemplateResponseOptionDTO> climateResponses = new ArrayList<>();
		for (ClimateResponseOption climateResponseOption : rootClimateResponseOption) {
			if (climateResponseOption.getKey() == null || climateResponseOption.getKey().isEmpty()
					|| climateResponseOption.getKey() == "")
				continue;
			ClimateTemplateResponseOptionDTO currentOpResponse = new ClimateTemplateResponseOptionDTO(
					climateResponseOption.getId(), climateResponseOption.getKey(),
					climateResponseOption.getPercentage(), null);
			climateResponses.add(currentOpResponse);
		}
		return climateResponses;
	}

	public void duplicateEvaluationClimateByBD(Long companyId, Long evaluationId) {
		FormEvaluation rootEvaluation = null;
		ClimateModel rootClimateModel = null;
		ClimateModel newClimateModel = null;
		Optional<FormEvaluation> evaluationOp = evaluationRepository.findEvaluationById(evaluationId);

		if (!evaluationOp.isPresent()) {
			return;
		}
		rootEvaluation = evaluationOp.get();
		if (rootEvaluation.getModel() == null) {
			return;
		}

		rootClimateModel = rootEvaluation.getModel();
		newClimateModel = new ClimateModel(rootClimateModel);
		climateModelRepository.saveAndFlush(newClimateModel);

		List<ClimateResponseOption> rootClimateResponseOption = null;
		Optional<List<ClimateResponseOption>> climateResponseOptionOp = climateResponseOptionRespository
				.findAllByModelId(rootClimateModel.getId());
		if (climateResponseOptionOp.isPresent()) {
			rootClimateResponseOption = climateResponseOptionOp.get();
			this.cloneAndSaveClimateResponseOptions(rootClimateResponseOption, newClimateModel);
		}

		List<ClimateDimension> rootClimateDimension = null;
		Optional<List<ClimateDimension>> climateDimensionOp = climateDimensionRepository
				.getClimateDimensionByModelId(rootClimateModel.getId());
		if (climateDimensionOp.isPresent()) {
			rootClimateDimension = climateDimensionOp.get();
			this.cloneAndSaveClimateDimension(rootClimateDimension, newClimateModel);
		}

		List<ClimateFactor> rootClimateFactors = null;
		Optional<List<ClimateFactor>> climateFactorsOp = climateFactorRepository
				.findAllByModelId(rootClimateModel.getId());
		if (climateFactorsOp.isPresent()) {
			rootClimateFactors = climateFactorsOp.get();
			this.cloneAndSaveClimateFactor(rootClimateFactors, newClimateModel);
		}

		List<ClimateDemographicField> rootClimateDemographicField = null;
		Optional<List<ClimateDemographicField>> climateDemographicFieldOp = climateDemographicFieldRepository
				.getClimateDemographicFieldByModelIdAndState(rootClimateModel.getId(), EntityState.ACTIVE);
		if (climateDemographicFieldOp.isPresent()) {
			rootClimateDemographicField = climateDemographicFieldOp.get();
			this.cloneAndSaveClimateDemographicField(rootClimateDemographicField, newClimateModel);
		}

		List<ClimateModelQuestion> rootClimateModelQuestion = null;
		Optional<List<ClimateModelQuestion>> climateModelQuestionOp = climateModelQuestionRepository
				.getClimateModelQuestionByModelId(rootClimateModel.getId());
		if (climateModelQuestionOp.isPresent()) {
			rootClimateModelQuestion = climateModelQuestionOp.get();
			this.cloneAndSaveClimateModelQuestion(rootClimateModelQuestion, newClimateModel);
		}

	}

	private void cloneAndSaveClimateModelQuestion(List<ClimateModelQuestion> rootClimateModelQuestion,
			ClimateModel newClimateModel) {
		for (ClimateModelQuestion climateModelQuestion : rootClimateModelQuestion) {

			ClimateModelQuestion newClimateModelQuestion = new ClimateModelQuestion(climateModelQuestion,
					newClimateModel);
			climateModelQuestionRepository.saveAndFlush(newClimateModelQuestion);
		}

	}

	private List<ClimateDimensionQuestion> cloneAndSaveClimateDimensionQuestion(
			List<ClimateDimensionQuestion> rootClimateDimensionQuestion, ClimateDimension newClimateDimension) {
		List<ClimateDimensionQuestion> questions = new ArrayList<ClimateDimensionQuestion>();
		for (ClimateDimensionQuestion climateDimensionQuestion : rootClimateDimensionQuestion) {

			ClimateDimensionQuestion newClimateDimensionQuestion = new ClimateDimensionQuestion(
					climateDimensionQuestion, newClimateDimension);
			questions.add(newClimateDimensionQuestion);
		}
		climateDimensionQuestionRepository.saveAll(questions);
		return questions;

	}

	private void cloneAndSaveClimateDimension(List<ClimateDimension> rootClimateDimension,
			ClimateModel newClimateModel) {
		for (ClimateDimension climateDimension : rootClimateDimension) {

			ClimateDimension newClimateDimension = new ClimateDimension(climateDimension, newClimateModel);
			climateDimensionRepository.saveAndFlush(newClimateDimension);

			List<ClimateDimensionQuestion> questions = this
					.cloneAndSaveClimateDimensionQuestion(newClimateDimension.getQuestions(), newClimateDimension);
			newClimateDimension.setQuestions(questions);
			climateDimensionRepository.saveAndFlush(newClimateDimension);
		}

	}

	private void cloneAndSaveClimateDemographicField(List<ClimateDemographicField> rootClimateDemographicField,
			ClimateModel newClimateModel) {
		for (ClimateDemographicField climateDemographicField : rootClimateDemographicField) {
			ClimateDemographicField newClimateDemographicField = new ClimateDemographicField(climateDemographicField,
					newClimateModel);
			climateDemographicFieldRepository.saveAndFlush(newClimateDemographicField);

			List<ClimateDemographicOption> options = this.cloneAndSaveClimateDemographicOption(
					climateDemographicField.getOptions(), newClimateDemographicField);
			newClimateDemographicField.setOptions(options);
			climateDemographicFieldRepository.saveAndFlush(newClimateDemographicField);
		}

	}

	private List<ClimateDemographicOption> cloneAndSaveClimateDemographicOption(List<ClimateDemographicOption> options,
			ClimateDemographicField newClimateDemographicField) {
		List<ClimateDemographicOption> newOptions = new ArrayList<ClimateDemographicOption>();
		for (ClimateDemographicOption climateDemographicFieldOption : options) {
			ClimateDemographicOption newClimateDemographicFieldOption = new ClimateDemographicOption(
					climateDemographicFieldOption, newClimateDemographicField);
			newOptions.add(newClimateDemographicFieldOption);
		}
		climateDemographicOptionRepository.saveAll(newOptions);
		return newOptions;
	}

	private void cloneAndSaveClimateFactor(List<ClimateFactor> rootClimateFactors, ClimateModel newClimateModel) {
		for (ClimateFactor climateFactor : rootClimateFactors) {

			ClimateFactor newClimateFactor = new ClimateFactor(climateFactor, newClimateModel);
			climateFactorRepository.saveAndFlush(newClimateFactor);
		}

	}

	private void cloneAndSaveClimateResponseOptions(List<ClimateResponseOption> rootClimateResponseOption,
			ClimateModel newClimateModel) {
		for (ClimateResponseOption climateResponseOption : rootClimateResponseOption) {

			ClimateResponseOption newClimateResponseOption = new ClimateResponseOption(climateResponseOption,
					newClimateModel);
			climateResponseOptionRespository.saveAndFlush(newClimateResponseOption);
		}

	}

	public boolean deleteEvaluation(Long evaluationId) {
		Optional<Climate> evaluationOp = evaluationRepository.findClimateEvaluationById(evaluationId);

		if(evaluationOp.isPresent()) {
			Climate evaluation  = evaluationOp.get();
			evaluation.setState(EntityState.DELETED);
			evaluationRepository.save(evaluation);
		}
		return true;
	}

	public List<String> getClimateEvaluationTypes(Long companyId, Long modelId) {
		List<String> climateEvaluationTypes = new ArrayList<>();
		Optional<Object[]> types = this.evaluationRepository.getClimateEvaluationTypes(modelId);
		if (!types.isPresent())
			return climateEvaluationTypes;
		for (Object type : types.get())
			if (!type.toString().isEmpty())
				climateEvaluationTypes.add(type.toString());
		return climateEvaluationTypes;
	}

	/***
	 * Método que obtiene los tipos de cálculo para resultados para enps
	 */
	public Map<Long, String> getCalculationTypesEnps() {

		List<ClimateCalculationType> types = calculationTypeRepository.findAll();

		Map<Long, String> cTypes = types.stream()
				.collect(Collectors.toMap(ClimateCalculationType::getId, ClimateCalculationType::getEquation));
		return cTypes;

	}

	public Boolean getClimateDependent(Long companyId, Long modelId) {
		try {
			Optional<List<ClimateDependenceQuestion>> dependent = this.climateDependenceQuestionRespository.findAllByModelIdAndState(modelId, EntityState.ACTIVE);
			return dependent.isPresent();		
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}

}