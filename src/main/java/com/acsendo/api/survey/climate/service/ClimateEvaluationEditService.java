package com.acsendo.api.survey.climate.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acsendo.api.climate.dto.ClimateDimensionDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationTemplateContentDTO;
import com.acsendo.api.climate.dto.ClimateFactorDTO;
import com.acsendo.api.climate.dto.ClimateNPSResponseOptionDTO;
import com.acsendo.api.climate.dto.ClimateTemplateQuestion;
import com.acsendo.api.climate.dto.ClimateTemplateResponseOptionDTO;
import com.acsendo.api.climate.enumerations.EClimateQuestionType;
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
import com.acsendo.api.climate.repository.ClimateFactorRepository;
import com.acsendo.api.climate.repository.ClimateModelQuestionRepository;
import com.acsendo.api.climate.repository.ClimateModelRepository;
import com.acsendo.api.climate.repository.ClimateResponseOptionRespository;
import com.acsendo.api.hcm.enumerations.EntityState;

@Service
public class ClimateEvaluationEditService {

	@Autowired
	private ClimateDimensionQuestionRepository climateDimensionQuestionRepository;
	@Autowired
	private ClimateDimensionRepository climateDimensionRepository;
	@Autowired
	private ClimateFactorRepository climateFactorRepository;
	@Autowired
	private ClimateModelRepository climateModelRepository;
	@Autowired
	private ClimateModelQuestionRepository climateModelQuestionRepository;
	@Autowired
	private ClimateDemographicFieldRepository climateDemographicFieldRepository;
	@Autowired
	private ClimateResponseOptionRespository climateResponseOptionRespository;
	@Autowired
	private ClimateConfigurationRepository climateConfigurationRepository;
	@Autowired
	private ClimateDemographicOptionRepository climateDemographicOptionRepository;
	@Autowired
	private ClimateDependenceQuestionRepository climateDependenceQuestionRepository;
	@Autowired
	private ClimateEvaluationService climateEvaluationService;

	private Map<Long, ClimateFactor> mapFactors = new HashMap<>();
	private Map<Long, ClimateDimension> mapDimensions = new HashMap<>();

	public ClimateEvaluationTemplateContentDTO editClimateQuestions(Long companyId,
			ClimateEvaluationTemplateContentDTO previousContentDTO, ClimateEvaluationTemplateContentDTO newContentDTO) {
		this.mapFactors = new HashMap<>();
		this.mapDimensions = new HashMap<>();
		List<ClimateTemplateQuestion> newQuestions = new ArrayList<>(newContentDTO.getQuestions());
		Map<Long, ClimateTemplateQuestion> editedQuestions = new HashMap<>();
		ClimateModel model = climateModelRepository.findById(newContentDTO.getModelId().longValue()).get();

		Optional<List<ClimateDimension>> dimensions = this.climateDimensionRepository
				.findAllByModelIdAndState(model.getId(), EntityState.ACTIVE);
		Optional<List<ClimateFactor>> factors = this.climateFactorRepository.findAllByModelIdAndState(model.getId(),
				EntityState.ACTIVE);

		if (dimensions.isPresent())
			this.mapDimensions = dimensions.get().stream()
					.collect(Collectors.toMap(ClimateDimension::getId, Function.identity()));
		if (factors.isPresent())
			this.mapFactors = factors.get().stream()
					.collect(Collectors.toMap(ClimateFactor::getId, Function.identity()));

		for (int indexOrder = 0; indexOrder < newQuestions.size(); indexOrder++) {
			ClimateTemplateQuestion currentQuestion = newQuestions.get(indexOrder);

			switch (currentQuestion.getType()) {
			case CLIMATE_LABEL:
			case ENPS:
				this.validateClimateENPSQuestion(currentQuestion, model, indexOrder);
				break;
			case SOCIODEMOGRAPHIC:
				this.validateClimateSDemoGraphicQuestion(currentQuestion, model, indexOrder);
				break;
			case OPEN:
				this.validateClimateOpenQuestion(currentQuestion, model, indexOrder);
				break;
			}
			editedQuestions.put(currentQuestion.getRowNumber().longValue(), currentQuestion);
		}
		this.removeUnusedQuestions(previousContentDTO, editedQuestions);
		this.validateClimateResponseOption(previousContentDTO, newContentDTO, model);
		this.validateClimateENPSResponses(previousContentDTO, newContentDTO, model);
		this.updateDimensionsAndFactorsDTO(newContentDTO);
		this.updateDependentQuestion(newContentDTO, model);
		return newContentDTO;

	}

	private void updateDependentQuestion(ClimateEvaluationTemplateContentDTO newContentDTO,
			ClimateModel climateModel) {
		Optional<List<ClimateDependenceQuestion>> oldDependent = this.climateDependenceQuestionRepository
				.findAllByModelIdAndState(climateModel.getId(), EntityState.ACTIVE);
		if (oldDependent.isPresent())
			oldDependent.get().stream().forEach(dep -> {
				dep.setState(EntityState.DELETED);
				this.climateDependenceQuestionRepository.save(dep);
		});
		
		Map<String, ClimateResponseOption> climateResponseOptionsMap = new HashMap<>();
		Optional<List<ClimateResponseOption>> climateResponseOptionsList = this.climateResponseOptionRespository
				.findAllByModelIdAndState(climateModel.getId(), EntityState.ACTIVE);
		if (climateResponseOptionsList.isPresent()) {
			climateResponseOptionsMap = climateResponseOptionsList.get().stream().filter(opt -> opt.getKey() != null)
					.collect(Collectors.toMap(ClimateResponseOption::getKey, Function.identity()));
			climateResponseOptionsMap
					.putAll(climateResponseOptionsList.get().stream().filter(opt -> opt.getKey() == null)
							.collect(Collectors.toMap(opt -> String.valueOf(opt.getValue()), Function.identity())));
		}

		Map<String, ClimateDemographicOption> demographicResponseOptionsMap = new HashMap<>();
		Optional<List<ClimateDemographicOption>> climateDemographicOptionLit = this.climateDemographicOptionRepository
				.findAllByModelIdAndState(climateModel.getId(), EntityState.ACTIVE.name());
		if (climateDemographicOptionLit.isPresent())
			demographicResponseOptionsMap = climateDemographicOptionLit.get().stream()
					.collect(Collectors.toMap( opt ->  opt.getKey()+"-"+opt.getField().getId(), Function.identity()));

		climateEvaluationService.createClimateDependentQuestions(newContentDTO, climateResponseOptionsMap,
				demographicResponseOptionsMap, climateModel);

	}

	private void updateDimensionsAndFactorsDTO(ClimateEvaluationTemplateContentDTO newContentDTO) {
		List<ClimateDimensionDTO> dimensionsDTO = this.mapDimensions.values().stream()
				.map(d -> new ClimateDimensionDTO(d.getId(), d.getName())).collect(Collectors.toList());
		newContentDTO.setDimensions(dimensionsDTO);
		List<ClimateFactorDTO> factorsDTO = this.mapFactors.values().stream()
				.map(d -> new ClimateFactorDTO(d.getId(), d.getName())).collect(Collectors.toList());
		newContentDTO.setFactors(factorsDTO);

		List<Long> finalDimensions = newContentDTO.getQuestions().stream().filter(q -> q.getDimension() != null)
				.map(q -> q.getDimension().getId().longValue()).collect(Collectors.toList());

		this.mapDimensions.entrySet().removeIf(obj -> finalDimensions.contains(obj.getKey()));

		for (Entry<Long, ClimateDimension> entry : this.mapDimensions.entrySet()) {
			ClimateDimension deletedDimension = entry.getValue();
			if(deletedDimension.getState().equals(EntityState.DELETED)) continue;
			deletedDimension.setState(EntityState.DELETED);
			this.climateDimensionRepository.save(deletedDimension);
		}

		List<Long> finalFactors = newContentDTO.getQuestions().stream().filter(q -> q.getFactor() != null)
				.map(q -> q.getFactor().getId().longValue()).collect(Collectors.toList());

		this.mapFactors.entrySet().removeIf(obj -> finalFactors.contains(obj.getKey()));

		for (Entry<Long, ClimateFactor> entry : this.mapFactors.entrySet()) {
			ClimateFactor deletedFactor = entry.getValue();
			if(deletedFactor.getState().equals(EntityState.DELETED)) continue;
			deletedFactor.setState(EntityState.DELETED);
			this.climateFactorRepository.save(deletedFactor);
		}

	}

	private void validateClimateENPSResponses(ClimateEvaluationTemplateContentDTO previousContentDTO,
			ClimateEvaluationTemplateContentDTO newContentDTO, ClimateModel climateModel) {
		if (newContentDTO.geteNPS() == null || newContentDTO.geteNPS().getDetractorMinLimit() == null)
			return;
		ClimateNPSResponseOptionDTO neweNPS = newContentDTO.geteNPS();
		List<ClimateResponseOption> oldeNPS = climateResponseOptionRespository
				.findAllByModelIdAndKeyIsNullOrderByValue(climateModel.getId());
		Map<Integer, ClimateResponseOption> oldeNPSMap = oldeNPS.stream()
				.collect(Collectors.toMap(ClimateResponseOption::getValue, Function.identity()));
		if (oldeNPSMap.isEmpty()) {
			this.createClimateENPSResponses(newContentDTO.geteNPS(), climateModel);
			this.createClimateConfiguration(newContentDTO.geteNPS(), climateModel);
			return;
		}

		int min = oldeNPSMap.keySet().stream().findFirst().get() < neweNPS.getDetractorMinLimit().intValue()
				? oldeNPSMap.keySet().stream().findFirst().get()
				: neweNPS.getDetractorMinLimit().intValue();
		System.err.println("edit" + oldeNPSMap.keySet().stream().findFirst().get() + (oldeNPSMap.size() - 1) + " "
				+ neweNPS.getPromoterMaxLimit().intValue());
		int max = oldeNPSMap.keySet().stream().findFirst().get() + (oldeNPSMap.size() - 1) > neweNPS
				.getPromoterMaxLimit().intValue()
						? oldeNPSMap.keySet().stream().findFirst().get() + (oldeNPSMap.size() - 1)
						: neweNPS.getPromoterMaxLimit().intValue();
		for (int currentValue = min; currentValue <= max; currentValue++) {
			ClimateResponseOption climateResponseOption = new ClimateResponseOption();
			if (oldeNPSMap.containsKey(currentValue) && (currentValue < neweNPS.getDetractorMinLimit().intValue()
					|| currentValue > neweNPS.getPromoterMaxLimit().intValue())) {
				climateResponseOption = oldeNPSMap.get(currentValue);
				this.changeStatusClimateENPSOption(climateResponseOption);
				climateResponseOption.setState(EntityState.DELETED);
				this.climateResponseOptionRespository.save(climateResponseOption);
				continue;
			} else if (oldeNPSMap.containsKey(currentValue)
					&& currentValue >= neweNPS.getDetractorMinLimit().intValue())
				climateResponseOption.setId(oldeNPSMap.get(currentValue).getId());

			climateResponseOption.setModel(climateModel);
			climateResponseOption.setValue(currentValue);
			climateResponseOption.setPercentage((currentValue * 100) / neweNPS.getPromoterMaxLimit().intValue());
			this.climateResponseOptionRespository.save(climateResponseOption);
		}
		this.createClimateConfiguration(newContentDTO.geteNPS(), climateModel);

	}

	private void createClimateENPSResponses(ClimateNPSResponseOptionDTO eNPS, ClimateModel climateModel) {
		for (int i = eNPS.getDetractorMinLimit().intValue(); i <= eNPS.getPromoterMaxLimit(); i++) {
			ClimateResponseOption climateResponseOption = new ClimateResponseOption();
			climateResponseOption.setModel(climateModel);
			climateResponseOption.setValue(i);
			climateResponseOption.setPercentage((i * 100) / eNPS.getPromoterMaxLimit().intValue());
			climateResponseOption = this.climateResponseOptionRespository.save(climateResponseOption);
		}
	}

	// TODO: configuration factory
	private void createClimateConfiguration(ClimateNPSResponseOptionDTO geteNPS, ClimateModel climateModel) {
		Optional<ClimateConfiguration> configurationEntity = this.climateConfigurationRepository
				.findByModelId(climateModel.getId());
		if (!configurationEntity.isPresent())
			return;
		ClimateConfiguration config = configurationEntity.get();
		if (geteNPS.getDetractorLabel() != null)
			config.setDetractorLabel(geteNPS.getDetractorLabel());
		if (geteNPS.getDetractorMinLimit() != null)
			config.setDetractorMinLimit(geteNPS.getDetractorMinLimit());
		if (geteNPS.getDetractorMaxLimit() != null)
			config.setDetractorMaxLimit(geteNPS.getDetractorMaxLimit());

		if (geteNPS.getPromoterLabel() != null)
			config.setPromoterLabel(geteNPS.getPromoterLabel());
		if (geteNPS.getPromoterMinLimit() != null)
			config.setPromoterMinLimit(geteNPS.getPromoterMinLimit());
		if (geteNPS.getPromoterMaxLimit() != null)
			config.setPromoterMaxLimit(geteNPS.getPromoterMaxLimit());

		if (geteNPS.getNeutralLabel() != null)
			config.setNeutralLabel(geteNPS.getNeutralLabel());
		if (geteNPS.getNeutralMinLimit() != null)
			config.setNeutralMinLimit(geteNPS.getNeutralMinLimit());
		if (geteNPS.getNeutralMaxLimit() != null)
			config.setNeutralMaxLimit(geteNPS.getNeutralMaxLimit());

		config.setMinScaleLabel(geteNPS.getMinScaleLabel());
		config.setMiddleScaleLabel(geteNPS.getMiddleScaleLabel());
		config.setMaxScaleLabel(geteNPS.getMaxScaleLabel());

		this.climateConfigurationRepository.save(config);
	}

	private int[] validateClimateResponseOption(ClimateEvaluationTemplateContentDTO previousContentDTO,
			ClimateEvaluationTemplateContentDTO newContentDTO, ClimateModel climateModel) {
		List<ClimateTemplateResponseOptionDTO> climateResponses = newContentDTO.getClimateResponses();
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
			if (responseOption == null || responseOption.getRowNumber() == null)
				continue;
			Optional<ClimateResponseOption> climateResponseOptionOp = climateResponseOptionRespository
					.findById(responseOption.getRowNumber().longValue());
			ClimateResponseOption climateResponseOption = climateResponseOptionOp.isPresent() == true
					? climateResponseOptionOp.get()
					: new ClimateResponseOption();
			climateResponseOption.setModel(climateModel);
			climateResponseOption.setKey(responseOption.getResponseLabel());
			climateResponseOption.setValue(responseOption.getWeigth().intValue());
			if (max < 100)
				climateResponseOption.setPercentage((responseOption.getWeigth().intValue() * 100) / max);
			else if (max == 100)
				climateResponseOption.setPercentage(climateResponseOption.getValue());
			climateResponseOption = this.climateResponseOptionRespository.save(climateResponseOption);
			responseOption.setRowNumber(climateResponseOption.getId());
		}

		this.removeUnusedClimateQuestionsResponse(previousContentDTO, newContentDTO, climateModel);
		return weigths;

	}

	private void removeUnusedClimateQuestionsResponse(ClimateEvaluationTemplateContentDTO previousContentDTO,
			ClimateEvaluationTemplateContentDTO newContentDTO, ClimateModel climateModel) {
		if (previousContentDTO.getClimateResponses().size() >= newContentDTO.getClimateResponses().size())
			return;
		List<ClimateTemplateResponseOptionDTO> oldClimateResponses = previousContentDTO.getClimateResponses();
		Map<Long, ClimateTemplateResponseOptionDTO> newOptions = newContentDTO.getClimateResponses().stream()
				.collect(Collectors.toMap(ClimateTemplateResponseOptionDTO::getId, Function.identity()));
		for (ClimateTemplateResponseOptionDTO currentOldOptionDTO : oldClimateResponses) {
			if (newOptions.containsKey(currentOldOptionDTO.getId()))
				continue;
			Optional<ClimateResponseOption> climateResponseOptionOp = climateResponseOptionRespository
					.findById(currentOldOptionDTO.getRowNumber().longValue());
			if (!climateResponseOptionOp.isPresent())
				continue;

			ClimateResponseOption climateResponseOption = climateResponseOptionOp.get();
			this.changeStatusClimateENPSOption(climateResponseOption);
			climateResponseOption.setState(EntityState.DELETED);
			climateResponseOptionRespository.save(climateResponseOption);
		}

	}

	private void changeStatusClimateENPSOption(ClimateResponseOption opt) {
		Optional<List<ClimateDependenceQuestion>> oldDependent = this.climateDependenceQuestionRepository
				.findAllByParentENPSClimateOptionIdAndState(opt.getId(), EntityState.ACTIVE);
		if (oldDependent.isPresent())
			oldDependent.get().stream().forEach(dep -> {
				dep.setState(EntityState.DELETED);
				this.climateDependenceQuestionRepository.save(dep);
		});
	}

	private void removeUnusedQuestions(ClimateEvaluationTemplateContentDTO previousContentDTO,
			Map<Long, ClimateTemplateQuestion> editedQuestions) {
		List<ClimateTemplateQuestion> oldQuestions = previousContentDTO.getQuestions();
		for (int index = 0; index < oldQuestions.size(); index++) {
			ClimateTemplateQuestion oldQuestion = oldQuestions.get(index);
			if (editedQuestions.containsKey(oldQuestion.getRowNumber().longValue()))
				continue;

			switch (oldQuestion.getType()) {
			case CLIMATE_LABEL:
			case ENPS:
				this.changeStatusClimateENPSQuestion(oldQuestion);
				break;
			case OPEN:
				this.changeStatusClimateOpenQuestion(oldQuestion);
				break;
			case SOCIODEMOGRAPHIC:
				this.changeStatusClimateSDemoGraphicQuestion(oldQuestion);
				break;
			}
		}
	}

	private void changeStatusClimateSDemoGraphicQuestion(ClimateTemplateQuestion oldQuestion) {
		Optional<ClimateDemographicField> demographicQuestion = this.climateDemographicFieldRepository
				.findByIdAndState(oldQuestion.getRowNumber().longValue(), EntityState.ACTIVE);
		if (!demographicQuestion.isPresent()) return;
		ClimateDemographicField questionEntity = demographicQuestion.get();
		
		Optional<List<ClimateDemographicOption>> demographicOpt = this.climateDemographicOptionRepository.findAllByFieldIdAndState(questionEntity.getId(), EntityState.ACTIVE);
		if(demographicOpt.isPresent()) this.changeStatusClimateSDemoGraphicOption(demographicOpt.get());
		
		questionEntity.setState(EntityState.DELETED);
		this.changeStatusClimateSDemoGraphicFieldDependent(questionEntity);
		this.climateDemographicFieldRepository.save(questionEntity);
	}

	private void changeStatusClimateSDemoGraphicFieldDependent(ClimateDemographicField sDemoField) {
		Optional<List<ClimateDependenceQuestion>> oldDependent = this.climateDependenceQuestionRepository
				.findAllByChildDemographicQuestionIdAndState(sDemoField.getId(), EntityState.ACTIVE);
		if (oldDependent.isPresent())
			oldDependent.get().stream().forEach(dep -> {
				dep.setState(EntityState.DELETED);
				this.climateDependenceQuestionRepository.save(dep);
		});
		
	}

	private void changeStatusClimateSDemoGraphicOption(List<ClimateDemographicOption> demographicOpt) {
		demographicOpt.stream().forEach(opt -> {
			this.changeStatusClimateSDemoGraphicOptionDependent(opt);
			opt.setState(EntityState.DELETED);
			this.climateDemographicOptionRepository.save(opt);
		});
	}

	private void changeStatusClimateSDemoGraphicOptionDependent(ClimateDemographicOption opt) {
		Optional<List<ClimateDependenceQuestion>> oldDependent = this.climateDependenceQuestionRepository
				.findAllByParentDemographicOptionIdAndState(opt.getId(), EntityState.ACTIVE);
		if (oldDependent.isPresent())
			oldDependent.get().stream().forEach(dep -> {
				dep.setState(EntityState.DELETED);
				this.climateDependenceQuestionRepository.save(dep);
		});
	}

	private void changeStatusClimateOpenQuestion(ClimateTemplateQuestion oldQuestion) {
		Optional<ClimateModelQuestion> modelQuestion = this.climateModelQuestionRepository
				.findByIdAndState(oldQuestion.getRowNumber().longValue(), EntityState.ACTIVE);
		if (!modelQuestion.isPresent()) return;
		ClimateModelQuestion questionEntity = modelQuestion.get();
		this.changeStatusClimateOpenDependent(questionEntity);
		questionEntity.setState(EntityState.DELETED);
		this.climateModelQuestionRepository.save(questionEntity);

	}

	private void changeStatusClimateOpenDependent(ClimateModelQuestion questionEntity) {
		Optional<List<ClimateDependenceQuestion>> oldDependent = this.climateDependenceQuestionRepository
				.findAllByChildOpenQuestionIdAndState(questionEntity.getId(), EntityState.ACTIVE);
		if (oldDependent.isPresent())
			oldDependent.get().stream().forEach(dep -> {
				dep.setState(EntityState.DELETED);
				this.climateDependenceQuestionRepository.save(dep);
		});
		
	}

	private void changeStatusClimateENPSQuestion(ClimateTemplateQuestion oldQuestion) {
		Optional<ClimateDimensionQuestion> dimensionQuestion = this.climateDimensionQuestionRepository
				.findByIdAndState(oldQuestion.getRowNumber().longValue(), EntityState.ACTIVE);
		if (!dimensionQuestion.isPresent()) return;
		ClimateDimensionQuestion questionEntity = dimensionQuestion.get();
		this.changeStatusClimateENPSDependent(questionEntity);
		questionEntity.setState(EntityState.DELETED);
		this.climateDimensionQuestionRepository.save(questionEntity);
	
	}

	private void changeStatusClimateENPSDependent(ClimateDimensionQuestion questionEntity) {
		Optional<List<ClimateDependenceQuestion>> oldDependent = this.climateDependenceQuestionRepository
				.findAllByClimateENPSAndState(questionEntity.getId(), EntityState.ACTIVE.name());
		if (oldDependent.isPresent())
			oldDependent.get().stream().forEach(dep -> {
				dep.setState(EntityState.DELETED);
				this.climateDependenceQuestionRepository.save(dep);
		});
	}

	private void validateClimateSDemoGraphicQuestion(ClimateTemplateQuestion newQuestionDTO, ClimateModel model,
			int index) {
		Optional<ClimateDemographicField> oldQuestion = this.climateDemographicFieldRepository
				.findById(newQuestionDTO.getRowNumber().longValue());
		ClimateDemographicField newQuestionEntity = new ClimateDemographicField();
		List<String> newOptions = new ArrayList<>();
		if (oldQuestion.isPresent()) {
			newQuestionEntity.setId(oldQuestion.get().getId());
			newOptions = this.validateSDemographicOptions(oldQuestion.get(), newQuestionDTO, model);// TODO:cycle
		} else {
			newOptions = newQuestionDTO.getsDemograficResponses();
		}
		newQuestionEntity.setName(newQuestionDTO.getQuestionText());
		newQuestionEntity.setDescription(newQuestionDTO.getQuestionDesc());
		newQuestionEntity.setRequired(newQuestionDTO.getIsRequired());
		newQuestionEntity.setFieldOrder(index);
		newQuestionEntity.setModel(model);
		newQuestionEntity = this.climateDemographicFieldRepository.save(newQuestionEntity);
		this.updateOptions(newQuestionEntity, newOptions, model);// cycle
		newQuestionDTO.setRowNumber(newQuestionEntity.getId());

	}

	private void updateOptions(ClimateDemographicField questionEntity, List<String> newOptions, ClimateModel model) {
		int value = 0;
		for (String currentOption : newOptions) {
			ClimateDemographicOption newOption = new ClimateDemographicOption(questionEntity, currentOption,
					String.valueOf(value++));
			climateDemographicOptionRepository.save(newOption);
		}
	}

	private List<String> validateSDemographicOptions(ClimateDemographicField oldDemographicQuestion,
			ClimateTemplateQuestion newQuestionDTO, ClimateModel model) {
		List<ClimateDemographicOption> oldOptions = oldDemographicQuestion.getOptions();
		List<String> newOptions = new ArrayList<>(newQuestionDTO.getsDemograficResponses());

		for (ClimateDemographicOption currentOldOption : oldOptions) {
			int indexOldOption = newOptions.indexOf(currentOldOption.getKey());
			if (indexOldOption != -1) {
				newOptions.remove(indexOldOption);
				continue;
			}
			currentOldOption.setState(EntityState.DELETED);
			climateDemographicOptionRepository.save(currentOldOption);
		}
		return newOptions;
	}

	private void validateClimateOpenQuestion(ClimateTemplateQuestion questionDTO, ClimateModel model, int index) {
		Optional<ClimateModelQuestion> modelQuestion = this.climateModelQuestionRepository
				.findByIdAndState(questionDTO.getRowNumber().longValue(), EntityState.ACTIVE);
		ClimateModelQuestion questionEntity = new ClimateModelQuestion();
		if (modelQuestion.isPresent())
			questionEntity.setId(modelQuestion.get().getId());

		questionEntity.setRequired(questionDTO.getIsRequired());
		questionEntity.setFieldOrder(index);
		questionEntity.setQuestion(questionDTO.getQuestionText());
		questionEntity.setDescription(questionDTO.getQuestionDesc());
		questionEntity.setModel(model);
		this.climateModelQuestionRepository.save(questionEntity);
		questionDTO.setRowNumber(questionEntity.getId());
	}

	private void validateClimateENPSQuestion(ClimateTemplateQuestion questionDTO, ClimateModel model, int index) {
		Optional<ClimateDimensionQuestion> dimensionQuestion = this.climateDimensionQuestionRepository
				.findById(questionDTO.getRowNumber().longValue());
		ClimateDimensionQuestion questionEntity = new ClimateDimensionQuestion();
		if (dimensionQuestion.isPresent())
			questionEntity = dimensionQuestion.get();

		ClimateDimensionQuestion questionUpdated = this.validateDimensionWithFactor(questionDTO, questionEntity, model,
				index);
		questionDTO.setRowNumber(questionUpdated.getId());
		questionDTO.getDimension().setId(questionUpdated.getDimension().getId());
		if (questionUpdated.getDimension().getFactor() != null)
			questionDTO.getFactor().setId(questionUpdated.getDimension().getFactor().getId());
	}

	private ClimateDimensionQuestion validateDimensionWithFactor(ClimateTemplateQuestion questionDTO,
			ClimateDimensionQuestion questionEntity, ClimateModel climateModel, int index) {
		ClimateDimension dimensionEntity = null;
		if(questionDTO.getDimension() != null && questionDTO.getDimension().getId() != null
		&& questionDTO.getDimension().getId().longValue()>0L && this.mapDimensions.get(questionDTO.getDimension().getId().longValue()) != null)
		{
			Optional<ClimateDimension> optFimensionEntity = this.climateDimensionRepository.findById(questionDTO.getDimension().getId().longValue());
			dimensionEntity = optFimensionEntity.isPresent() ? optFimensionEntity.get() : null;
			questionEntity.setDimension(dimensionEntity);
		}
		ClimateDimensionDTO dimensionDTO = questionDTO.getDimension();

		ClimateFactor factorEntity = questionEntity.getDimension() != null ? questionEntity.getDimension().getFactor()
				: null;
		ClimateFactorDTO factorDTO = questionDTO.getFactor();

		this.validateChangesPreExistingQuestion(questionEntity, dimensionEntity, dimensionDTO, factorEntity, factorDTO,
				climateModel);

		questionEntity.setQuestion(questionDTO.getQuestionText());
		questionEntity.setDescription(questionDTO.getQuestionDesc());
		questionEntity.setRequired(questionDTO.getIsRequired());
		questionEntity.setFieldOrder(index);
		questionEntity.setModel(climateModel);
		questionEntity.seteNPS(questionDTO.getType().equals(EClimateQuestionType.ENPS));

		return climateDimensionQuestionRepository.save(questionEntity);
	}

	private void validateChangesPreExistingQuestion(ClimateDimensionQuestion questionEntity,
			ClimateDimension dimensionEntity, ClimateDimensionDTO dimensionDTO, ClimateFactor factorEntity,
			ClimateFactorDTO factorDTO, ClimateModel climateModel) {
		Boolean addFactor = factorEntity == null && factorDTO != null && !this.mapFactors.containsKey(factorDTO.getId().longValue());
		Boolean removeFactor = factorEntity != null && factorDTO == null;
		Boolean editFactor = factorEntity != null && factorDTO != null && !factorEntity.getName().equalsIgnoreCase(factorDTO.getName());

		Boolean existDimension = dimensionEntity != null
				|| this.mapDimensions.containsKey(dimensionDTO.getId().longValue());
		Boolean changeDimension = !existDimension || editFactor || removeFactor || addFactor
				|| (dimensionEntity != null &&  dimensionDTO.getId().longValue() != dimensionEntity.getId());
		
		if (addFactor) {
			factorEntity = this.climateFactorRepository.save(new ClimateFactor(factorDTO.getName(), climateModel));
			this.mapFactors.put(factorEntity.getId(), factorEntity);
		} else if (editFactor)
			factorEntity = this.mapFactors.get(factorDTO.getId().longValue());

		if (changeDimension && !existDimension)
			dimensionEntity = new ClimateDimension(dimensionDTO.getName(), removeFactor ? null : factorEntity,
					climateModel);
		else if (changeDimension && existDimension) {
			dimensionEntity = this.mapDimensions.get(dimensionDTO.getId().longValue());
			dimensionEntity.setFactor(removeFactor ? null : factorEntity);
		} else
			return;

		questionEntity.setDimension(this.climateDimensionRepository.save(dimensionEntity));
		this.mapDimensions.put(dimensionEntity.getId(), dimensionEntity);
	}

}