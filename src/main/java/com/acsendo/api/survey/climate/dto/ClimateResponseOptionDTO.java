package com.acsendo.api.survey.climate.dto;

public class ClimateResponseOptionDTO {
	
	
	private Long id;
	private String key;
	private String value;
	private Long questionId;
	
	
	
	public ClimateResponseOptionDTO() {

	}

	public ClimateResponseOptionDTO(Long id, String key, String value, Long questionId) {
		this.id = id;
		this.key = key;
		this.value = value;
		this.questionId = questionId;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public Long getQuestionId() {
		return questionId;
	}
	
	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
	

}
