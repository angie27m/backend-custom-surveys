package com.acsendo.api.survey.climate.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.acsendo.api.climate.dao.ClimateModelDAO;
import com.acsendo.api.climate.dao.ClimatePartakerDAO;
import com.acsendo.api.climate.dto.ClimatePartakerDTO;
import com.acsendo.api.climate.dto.ClimatePartakerFilterDTO;
import com.acsendo.api.climate.factory.ClimatePartakerFactory;
import com.acsendo.api.climate.model.ClimateDemographicField;
import com.acsendo.api.climate.model.ClimateDemographicResponse;
import com.acsendo.api.climate.model.ClimateDimensionQuestion;
import com.acsendo.api.climate.model.ClimateDimensionQuestionResponse;
import com.acsendo.api.climate.model.ClimateModelQuestion;
import com.acsendo.api.climate.model.ClimateModelQuestionResponse;
import com.acsendo.api.climate.model.ClimatePartaker;
import com.acsendo.api.climate.repository.ClimateDemographicFieldRepository;
import com.acsendo.api.climate.repository.ClimateDemographicResponseRepository;
import com.acsendo.api.climate.repository.ClimateDimensionQuestionRepository;
import com.acsendo.api.climate.repository.ClimateDimensionQuestionResponseRepository;
import com.acsendo.api.climate.repository.ClimateModelQuestionRepository;
import com.acsendo.api.climate.repository.ClimateModelQuestionResponseRepository;
import com.acsendo.api.climate.repository.ClimatePartakerRepository;
import com.acsendo.api.hcm.dto.EmployeeDTO;
import com.acsendo.api.hcm.enumerations.EntityState;
import com.acsendo.api.survey.enumerations.SurveyParticipanState;
import com.acsendo.api.survey.util.PartakersClimateExcelHandler;
import com.acsendo.api.util.PageableUtil;

@Service
public class ClimatePartakerService {

	@Autowired
	private PageableUtil<ClimatePartakerDTO> pageableUtil;

	@Autowired
	private ClimatePartakerFactory climatePartakerFactory;
	
	@Autowired
	private ClimatePartakerRepository partakerRepo;

	@Autowired
	private ClimatePartakerDAO climatePartakerDAO;
	
	@Autowired
	private ClimateDimensionQuestionRepository climateDimensionQuestionRepository;

	@Autowired
	private ClimateModelQuestionRepository climateModelQuestionRepository;
	
	@Autowired
	private ClimateDemographicFieldRepository climateDemographicFieldRepository;
	
	@Autowired
	private  ClimateDemographicResponseRepository demographicResponseRepo;
	
	@Autowired
	private  ClimateDimensionQuestionResponseRepository dimensionResponseRepo;
	
	@Autowired
	private ClimateModelQuestionResponseRepository modelResponseRepo;
	
	@Autowired
	private ClimateModelDAO modelDao;
	
	@Autowired
	private PartakersClimateExcelHandler partakersClimateExcelHandler;
		
	
	public Page<ClimatePartakerDTO> getPartakersDetail(Long companyId, Long modelId, ClimatePartakerFilterDTO filters, Pageable pageable) {
		List<ClimatePartaker> partakers = this.climatePartakerDAO.findActivePartakersByModelIdAndFilters(modelId, filters);
		if (partakers == null || partakers.isEmpty())
			return Page.empty(pageable);
		List<ClimatePartakerDTO> partakersDTO = this.climatePartakerFactory.entitysToDTOs(partakers);
		int sizes = partakersDTO.size();
		partakersDTO = this.pageableUtil.getPageFromList(pageable, partakersDTO).getContent();
		this.updatePercentageAnswared(partakersDTO, modelId);
		return partakersDTO == null || partakersDTO.isEmpty() ? Page.empty(pageable)
				: new PageImpl<ClimatePartakerDTO>(partakersDTO, pageable, sizes);
	}

	private void updatePercentageAnswared(List<ClimatePartakerDTO> partakersDTO, Long modelId) {
		Integer totalQuestions = this.findAllQuestions(modelId);
		partakersDTO.stream()
				.forEach(result ->{
					Double percentageAnswered = calculatePercentageAnswered(modelId, totalQuestions, result);		
					result.setPercentageAnswered(percentageAnswered);	
				} );
	}

	private Double calculatePercentageAnswered(Long modelId, Integer totalQuestions, ClimatePartakerDTO result) {
		Double percentageAnswered = null;

		if(result.getPartakerState().compareTo(SurveyParticipanState.FINISHED) == 0){
			percentageAnswered = 1.0;
		}else {
			percentageAnswered = this.countAnsweredQuestion(result, modelId) / (totalQuestions == 0 ? 1: totalQuestions);
		}
		
		return round(percentageAnswered, 2);
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

	private Integer findAllQuestions(Long modelId) {
		Integer totalQuestions = 0;
		Optional<List<ClimateDimensionQuestion>> climateDimensionQuestions = this.climateDimensionQuestionRepository
				.findAllByModelIdAndState (modelId.longValue(), EntityState.ACTIVE);
		totalQuestions += climateDimensionQuestions.isPresent() ? climateDimensionQuestions.get().size() : 0; 
		Optional<List<ClimateDemographicField>> climateDemographicFieldOp = this.climateDemographicFieldRepository
				.getClimateDemographicFieldByModelIdAndState(modelId.longValue(), EntityState.ACTIVE);
		totalQuestions += climateDemographicFieldOp.isPresent() ? climateDemographicFieldOp.get().size() : 0;
		Optional<List<ClimateModelQuestion>> climateModelQuestionOp = this.climateModelQuestionRepository
				.getClimateModelQuestionByModelIdAndState(modelId.longValue(), EntityState.ACTIVE);
		totalQuestions += climateModelQuestionOp.isPresent() ? climateModelQuestionOp.get().size() : 0;
		return totalQuestions;
	}

	private Double countAnsweredQuestion(ClimatePartakerDTO result, Long modelId) {
		Optional<Number> answered = modelDao.getTotalQuestionAnsweredByPartakerId(modelId, result.getId().longValue());
		return answered.isPresent() ? answered.get() != null ? answered.get().doubleValue() : 0.0 : 0.0;
	}

	public List<ClimatePartakerFilterDTO> getPartakersJobs(Long companyId, Long modelId) {
		Optional<List<Object[]>> partakersData = this.partakerRepo.findJobsFromActivePartakersByModelId(modelId);
		if(!partakersData.isPresent())
			return new ArrayList<>();
		return partakersData.get().stream().map( data -> new ClimatePartakerFilterDTO((String) data[0], (String) data[1]))
				.collect(Collectors.toList());
	}

	public List<EmployeeDTO> getAvailablePartakers(Long companyId, Long modelId) {
		List<ClimatePartaker> partakers = this.partakerRepo.findActivePartakersByModelId(modelId);
		if(partakers == null || partakers.isEmpty())
			return new ArrayList<>();
		return partakers.stream().map( p -> new EmployeeDTO(p.getEmployee().getId(),p.getEmployee().getPerson().getName())).collect(Collectors.toList()) ;
	}

	public Boolean rebootPartakerSurvey(Long companyId, Long partakerId) {
		Optional<List<ClimateDemographicResponse>> climateSDemoQuestionOp = this.demographicResponseRepo
				.findAllByPartakerId(partakerId);
		if(climateSDemoQuestionOp.isPresent())
		{
			climateSDemoQuestionOp.get().forEach(q -> {
		        q.setState(EntityState.DELETED);
		        this.demographicResponseRepo.save(q);
		    });
		}
		Optional<List<ClimateDimensionQuestionResponse>> climateDimesionQuestionOp = this.dimensionResponseRepo
				.findAllByPartakerId(partakerId);
		if(climateDimesionQuestionOp.isPresent())
		{
			climateDimesionQuestionOp.get().forEach(q -> {
		        q.setState(EntityState.DELETED);
		        this.dimensionResponseRepo.save(q);
		    });
		}
		Optional<List<ClimateModelQuestionResponse>> climateModelQuestionOp = this.modelResponseRepo
				.findAllByPartakerId(partakerId);
		if(climateModelQuestionOp.isPresent())
		{
			climateModelQuestionOp.get().forEach(q -> {
		        q.setState(EntityState.DELETED);
		        this.modelResponseRepo.save(q);
		    });
		}
		Optional<ClimatePartaker> partakerOp = this.partakerRepo.findById(partakerId);
		if (!partakerOp.isPresent()) {
			return Boolean.FALSE;
		}
		ClimatePartaker p = partakerOp.get();
		p.setPartakerState(SurveyParticipanState.CREATED);
		p = this.partakerRepo.save(p);
		return Boolean.TRUE;
	}

	/**
	 * Obtiene reporte excel con participantes de una evaluación de clima
	 */
	public byte[] getClimatePartakersExcelReport(Long companyId, Long modelId, ClimatePartakerFilterDTO filters) {
		List<Object[]> partakers = this.climatePartakerDAO.getPartakersAndPercentageResponse(modelId);
		List<ClimatePartakerDTO> partakersDTO = climatePartakerFactory.getClimatePartakerDTOFromObject(partakers);
	
		return partakersClimateExcelHandler.getClimateExcelReport(partakersDTO, companyId);
	}

}
