package com.acsendo.api.survey.climate.dto;

public class ClimateQuestionOptionDependenceDTO {
	
	
	private Long questionId;
	private Long optionId;
	private Integer order;
	
	
	public Long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public Long getOptionId() {
		return optionId;
	}
	
	public void setOptionId(Long optionId) {
		this.optionId = optionId;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
	

}
