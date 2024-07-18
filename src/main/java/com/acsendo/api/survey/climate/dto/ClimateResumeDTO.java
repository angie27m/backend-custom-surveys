package com.acsendo.api.survey.climate.dto;

import com.acsendo.api.hcm.enumerations.EntityState;
import com.acsendo.api.survey.enumerations.SurveyParticipanState;

public class ClimateResumeDTO {

	private Long companyId;
	private Long modelId;
	private EntityState state;
	private SurveyParticipanState partakerState;
	private Long partakerId;

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public Long getModelId() {
		return modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}		

	public EntityState getState() {
		return state;
	}

	public void setState(EntityState state) {
		this.state = state;
	}

	public SurveyParticipanState getPartakerState() {
		return partakerState;
	}

	public void setPartakerState(SurveyParticipanState partakerState) {
		this.partakerState = partakerState;
	}

	public Long getPartakerId() {
		return partakerId;
	}

	public void setPartakerId(Long partakerId) {
		this.partakerId = partakerId;
	}

}
