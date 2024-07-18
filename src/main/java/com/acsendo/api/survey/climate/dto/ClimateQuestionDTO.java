package com.acsendo.api.survey.climate.dto;

import java.util.List;

import com.acsendo.api.climate.enumerations.EClimateQuestionType;

/**
 *  Clase para la información de todas las preguntas de un modelo
 * */
public class ClimateQuestionDTO {
	
	private Long id;
	
	private String question;
	
	private String description;
	
	private Boolean required;
	
	private EClimateQuestionType type;
	
	private Integer order;
	
	private String response;
	
	private List<ClimateQuestionOptionDependenceDTO> questionsDependence;
	
	private ClimateQuestionOptionDependenceDTO father;
	
	private Integer lastQuestionOrder;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public EClimateQuestionType getType() {
		return type;
	}

	public void setType(EClimateQuestionType type) {
		this.type = type;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public List<ClimateQuestionOptionDependenceDTO> getQuestionsDependence() {
		return questionsDependence;
	}

	public void setQuestionsDependence(List<ClimateQuestionOptionDependenceDTO> questionsDependence) {
		this.questionsDependence = questionsDependence;
	}

	public ClimateQuestionOptionDependenceDTO getFather() {
		return father;
	}

	public void setFather(ClimateQuestionOptionDependenceDTO father) {
		this.father = father;
	}

	public Integer getLastQuestionOrder() {
		return lastQuestionOrder;
	}

	public void setLastQuestionOrder(Integer lastQuestionOrder) {
		this.lastQuestionOrder = lastQuestionOrder;
	}
	
}
