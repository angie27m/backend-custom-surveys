package com.acsendo.api.survey.climate.dto;

public class InfoEmailClimateDTO {

	private String subject;
	private String content;
	private String[] emailRecipients;

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String[] getEmailRecipients() {
		return emailRecipients;
	}

	public void setEmailRecipients(String[] emailRecipients) {
		this.emailRecipients = emailRecipients;
	}

}
