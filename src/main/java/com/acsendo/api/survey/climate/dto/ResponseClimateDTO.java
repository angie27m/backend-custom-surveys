package com.acsendo.api.survey.climate.dto;


import com.acsendo.api.climate.enumerations.EClimateQuestionType;

public class ResponseClimateDTO {

	private Long questionId;
	private String response;
	private EClimateQuestionType type;
	

	public Long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public EClimateQuestionType getType() {
		return type;
	}

	public void setType(EClimateQuestionType type) {
		this.type = type;
	}
}
