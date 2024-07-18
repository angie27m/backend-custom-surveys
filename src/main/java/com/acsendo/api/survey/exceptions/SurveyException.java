package com.acsendo.api.survey.exceptions;

@SuppressWarnings("serial")
public class SurveyException extends Exception{

	
	public static final int FORM_DOES_NOT_EXIST = 1;
	public static final int FORM_READ_ERROR = 2;
	public static final int SURVEY_NOT_EXISTS = 3;
	public static final int SURVEY_CONFIG_NOT_EXISTS = 4;
	public static final int QUESTION_NOT_EXISTS = 5;
	public static final int SURVEY_QUESTIONS_EMPTY = 6;
	public static final int SURVEY_PARTAKERS_EMPTY = 7;
	public static final int EVALUATION_CANT_FINISH = 8;

	private int codeError;	
			
	public SurveyException(int codeError) {
		super();
		this.codeError = codeError;
	}

	@Override
	public String getMessage() {
		String message = "";
		if(this.codeError != 0) {
			message = SurveyException.getMessage(codeError);
		}
		
		if(message != null && !message.isEmpty()) {
			return message;
		}
		
		return super.getMessage();
	}
	
	private static String getMessage(int codeError) {
		String message = "";
		
		switch (codeError) {
		case FORM_DOES_NOT_EXIST:
			message = "El formulario relacionado a la evaluación no existe";
			break;
		case FORM_READ_ERROR:
			message = "Error al intentar leer el json de configuración del formulario";
			break;
		case SURVEY_NOT_EXISTS:
			message = "Error la encuesta no existe con el ID especificado";
			break;
		case SURVEY_CONFIG_NOT_EXISTS:
			message = "Error la encuesta no tiene una entidad de configuración ";
			break;
		case QUESTION_NOT_EXISTS:
			message = "Error al guardar la pregunta de la encuesta ";
			break;
		case SURVEY_QUESTIONS_EMPTY:
			message = "Error no hay preguntas en la encuesta.";
			break;
		case SURVEY_PARTAKERS_EMPTY:
			message = "Error hay participantes.";
			break;
		case EVALUATION_CANT_FINISH:
			message =  "No se puede finalizar la evaluación porque tiene preguntas sin responder";
			break;	
		default:
			break;
		}
		
		return message;
	}
	
}
