package com.acsendo.api.survey.climate.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ClimateEvaluationConfigurationDTO {

	private String title;
	private String description;
	private Boolean backButton;
	private Boolean progressBar;
	private Long questionsPage;
	private String welcomeImage;
	private String welcomeText;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private Date startDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private Date endDate;
	private String endText;
	private Boolean anonymous;
	private Boolean withCookie;
	
	private Boolean hasDependenceQuestions;
	

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getBackButton() {
		return backButton;
	}

	public void setBackButton(Boolean backButton) {
		this.backButton = backButton;
	}

	public Boolean getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(Boolean progressBar) {
		this.progressBar = progressBar;
	}

	public Long getQuestionsPage() {
		return questionsPage;
	}

	public void setQuestionsPage(Long questionsPage) {
		this.questionsPage = questionsPage;
	}

	public String getWelcomeImage() {
		return welcomeImage;
	}

	public void setWelcomeImage(String welcomeImage) {
		this.welcomeImage = welcomeImage;
	}
	
	public String getWelcomeText() {
		return welcomeText;
	}

	public void setWelcomeText(String welcomeText) {
		this.welcomeText = welcomeText;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getEndText() {
		return endText;
	}

	public void setEndText(String endText) {
		this.endText = endText;
	}

	public Boolean getAnonymous() {
		return anonymous;
	}

	public void setAnonymous(Boolean anonymous) {
		this.anonymous = anonymous;
	}

	public Boolean getWithCookie() {
		return withCookie;
	}

	public void setWithCookie(Boolean withCookie) {
		this.withCookie = withCookie;
	}

	public Boolean getHasDependenceQuestions() {
		return hasDependenceQuestions;
	}

	public void setHasDependenceQuestions(Boolean hasDependenceQuestions) {
		this.hasDependenceQuestions = hasDependenceQuestions;
	}

}
