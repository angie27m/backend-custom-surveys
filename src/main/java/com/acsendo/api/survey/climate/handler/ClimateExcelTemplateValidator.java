package com.acsendo.api.survey.climate.handler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.acsendo.api.climate.dto.ClimateDimensionDTO;
import com.acsendo.api.climate.dto.ClimateEvaluationTemplateContentDTO;
import com.acsendo.api.climate.dto.ClimateFactorDTO;
import com.acsendo.api.climate.dto.ClimateNPSResponseOptionDTO;
import com.acsendo.api.climate.dto.ClimateTemplateResponseOptionDTO;
import com.acsendo.api.climate.dto.ClimateTemplateQuestion;
import com.acsendo.api.climate.enumerations.EClimateErrorFile;
import com.acsendo.api.climate.enumerations.EClimateErrorQuestion;
import com.acsendo.api.climate.enumerations.EClimateQuestionType;

@Component
public class ClimateExcelTemplateValidator {

	public static final int CLIMATE_QUESTION_INDEX = 0;
	public static final int CLIMATE_QUESTION_DESC_INDEX = CLIMATE_QUESTION_INDEX + 1;
	public static final int CLIMATE_FACTOR_INDEX = CLIMATE_QUESTION_DESC_INDEX + 1;
	public static final int CLIMATE_DIMENSION_INDEX = CLIMATE_FACTOR_INDEX + 1;
	public static final int CLIMATE_TYPE_INDEX = CLIMATE_DIMENSION_INDEX + 1;
	public static final int CLIMATE_CODE_INDEX = CLIMATE_TYPE_INDEX + 1;
	public static final int CLIMATE_REQUIRED_INDEX = CLIMATE_CODE_INDEX + 1;

	public static final int QUESTION_SHEET = 0;
	public static final int CLIMATE_OPTIONS_SHEET = QUESTION_SHEET + 1;
	public static final int NPS_OPTIONS_SHEET = CLIMATE_OPTIONS_SHEET + 1;
	public static final int SDEMOGRAFIC_OPTIONS_SHEET = NPS_OPTIONS_SHEET + 1;
	public static final int QUESTION_SHEET_VALIDATION = SDEMOGRAFIC_OPTIONS_SHEET + 1;
	public static final String INTEGER_VALIDATION = "^\\d+.0$";
	
	public static final int QUESTIONS_SHEET_START__INDEX = 9;

	private Workbook workBook;
	private List<ClimateTemplateQuestion> climateTemplateQuestions;
	private List<ClimateTemplateResponseOptionDTO> climateOptions;
	private List<ClimateDimensionDTO> climateDimensions;
	private List<ClimateFactorDTO> climateFactors;
	private Map<Number, List<String>> sDemograficOptions;
	private Map<Number, List<String>> eNPSOptions;
	private ClimateNPSResponseOptionDTO eNPSInfo;

	private Map<String, Number> dimensionsMap = new HashMap<>();
	private Map<String, Number> factorsMap = new HashMap<>();;

	private Boolean hasClimate = false;
	private Boolean hasENPS = false;
	private Boolean hasSDemographic = false;

	public ClimateEvaluationTemplateContentDTO validateTemplateFile(MultipartFile template, Long companyId,
			List<List<String>> sheetsNamesWithDesc) {
		List<String> sheetsNames = new ArrayList<>();
		for (List<String> sheetsName: sheetsNamesWithDesc) sheetsNames.add(sheetsName.get(0));
		this.initFields();
		this.iterateRowsAndCreateSDemographicOptions(template, sheetsNames.get(SDEMOGRAFIC_OPTIONS_SHEET));
		this.iterateRowsAndCreateENPSOptions(template, sheetsNames.get(NPS_OPTIONS_SHEET));
		this.validateQuestionSheet(template, sheetsNames.get(QUESTION_SHEET),
				sheetsNames.get(QUESTION_SHEET_VALIDATION));
		if (this.hasClimate)
			this.iterateRowsAndCreateClimateOptions(template, sheetsNames.get(CLIMATE_OPTIONS_SHEET));
		this.convertClimateMapsToDTO();
		return new ClimateEvaluationTemplateContentDTO(this.climateTemplateQuestions, climateOptions, eNPSInfo,
				climateDimensions, climateFactors);
	}

	private void initFields() {
		this.climateTemplateQuestions = new ArrayList<ClimateTemplateQuestion>();
		this.climateOptions = new ArrayList<ClimateTemplateResponseOptionDTO>();
		this.sDemograficOptions = new HashMap<>();
		this.eNPSOptions = new HashMap<>();
		this.eNPSInfo = new ClimateNPSResponseOptionDTO();
		this.hasClimate = false;
		this.hasENPS = false;
		this.hasSDemographic = false;
		this.dimensionsMap = new HashMap<>();
		this.factorsMap = new HashMap<>();		
	}

	private void convertClimateMapsToDTO() {
		if (!dimensionsMap.isEmpty())
			this.convertClimateDimensionsMapToDTO();
		if (!factorsMap.isEmpty())
			this.convertClimateFactorsMapToDTO();

	}

	private void convertClimateDimensionsMapToDTO() {
		climateDimensions = new ArrayList<>();
		for (Map.Entry<String, Number> entry : dimensionsMap.entrySet()) {
			String key = entry.getKey();
			Number val = entry.getValue();
			climateDimensions.add(new ClimateDimensionDTO(val, key));
		}

	}

	private void convertClimateFactorsMapToDTO() {
		climateFactors = new ArrayList<>();
		for (Map.Entry<String, Number> entry : factorsMap.entrySet()) {
			String key = entry.getKey();
			Number val = entry.getValue();
			climateFactors.add(new ClimateFactorDTO(val, key));
		}

	}

	private void iterateRowsAndCreateENPSOptions(MultipartFile template, String sheetName) {
		try {

			Sheet climateOptionsSheet = this.workBook.getSheet(sheetName);
			if (climateOptionsSheet == null)
				return;
			Iterator<Row> optionsRows = climateOptionsSheet.iterator();
			while (optionsRows.hasNext()) {
				Row currentRow = optionsRows.next();
				Cell enpsLabelCell = currentRow.getCell(0);
				Cell minOptionCell = currentRow.getCell(1);
				Cell maxOptionCell = currentRow.getCell(2);
				if (currentRow.getRowNum() < QUESTIONS_SHEET_START__INDEX || enpsLabelCell == null
						|| minOptionCell == null || maxOptionCell == null
						|| minOptionCell.getCellType() != Cell.CELL_TYPE_NUMERIC
						|| maxOptionCell.getCellType() != Cell.CELL_TYPE_NUMERIC
						|| !String.valueOf(minOptionCell.getNumericCellValue()).matches(INTEGER_VALIDATION)
						|| !String.valueOf(maxOptionCell.getNumericCellValue()).matches(INTEGER_VALIDATION))
					continue;
				String enpsLabelLabel = enpsLabelCell.getStringCellValue().trim();
				String minOption = String.valueOf(minOptionCell.getNumericCellValue());
				String maxOption = String.valueOf(maxOptionCell.getNumericCellValue());
				Number rowNum = currentRow.getRowNum();
				this.eNPSOptions.put(rowNum.intValue() - QUESTIONS_SHEET_START__INDEX, Arrays.asList(enpsLabelLabel, minOption, maxOption));

			}
			if (!this.validateENPSOptions())
				this.eNPSOptions = new HashMap<>();
			else {
				this.createENPSInfo();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void createENPSInfo() {
		if (this.eNPSOptions.isEmpty())
			return;

		this.eNPSInfo.setDetractorLabel(this.eNPSOptions.get(0).get(0));
		this.eNPSInfo.setDetractorMinLimit(new Double(this.eNPSOptions.get(0).get(1)).longValue());
		this.eNPSInfo.setDetractorMaxLimit(new Double(this.eNPSOptions.get(0).get(2)).longValue());

		this.eNPSInfo.setNeutralLabel(this.eNPSOptions.get(1).get(0));
		this.eNPSInfo.setNeutralMinLimit(new Double(this.eNPSOptions.get(1).get(1)).longValue());
		this.eNPSInfo.setNeutralMaxLimit(new Double(this.eNPSOptions.get(1).get(2)).longValue());

		this.eNPSInfo.setPromoterLabel(this.eNPSOptions.get(2).get(0));
		this.eNPSInfo.setPromoterMinLimit(new Double(this.eNPSOptions.get(2).get(1)).longValue());
		this.eNPSInfo.setPromoterMaxLimit(new Double(this.eNPSOptions.get(2).get(2)).longValue());

	}

	private Boolean validateENPSOptions() {
		if (this.eNPSOptions.isEmpty())
			return false;
		int min = -1;
		int max = -1;
		for (int i = 0; i < this.eNPSOptions.size(); i++) {
			List<String> row = this.eNPSOptions.get(i);
			int currentMin = Double.valueOf(row.get(1)).intValue();
			int currentMax = Double.valueOf(row.get(2)).intValue();

			if (currentMin > min && currentMin > max)
				min = currentMin;
			else
				return false;
			if (currentMax > max)
				max = currentMax;
		}
		if (max > 10)
			return false;

		return true;

	}

	private void iterateRowsAndCreateClimateOptions(MultipartFile template, String sheetName) {
		try {

			Sheet climateOptionsSheet = this.workBook.getSheet(sheetName);
			if (climateOptionsSheet == null)
				return;
			Iterator<Row> optionsRows = climateOptionsSheet.iterator();
			Integer min = 0;
			Integer max = 100;
			List<EClimateErrorFile> errors = new ArrayList<EClimateErrorFile>();
			while (optionsRows.hasNext()) {
				Row currentRow = optionsRows.next();
				Cell optionLabelCell = currentRow.getCell(0);
				Cell weightOptionCell = currentRow.getCell(1);
				errors = new ArrayList<EClimateErrorFile>();
				String optionLabel = null;
				Number weigthOption = null;
				Number currentRowNum = currentRow.getRowNum() + 1;
				if (currentRowNum.intValue() == 1)//header row number
					continue;
				if (optionLabelCell == null || optionLabelCell.getCellType() != Cell.CELL_TYPE_STRING
						|| optionLabelCell.getStringCellValue().isEmpty())

					errors.add(EClimateErrorFile.EMPTY_CLIMATE_RESPONSE_LABEL);
				else
					optionLabel = optionLabelCell.getStringCellValue().trim();

				if (weightOptionCell == null || weightOptionCell.getCellType() != Cell.CELL_TYPE_NUMERIC
						|| !String.valueOf(weightOptionCell.getNumericCellValue()).matches(INTEGER_VALIDATION))
					errors.add(EClimateErrorFile.EMPTY_CLIMATE_RESPONSE_WEIGHT);
				else
					weigthOption = new Double(weightOptionCell.getNumericCellValue()).intValue();

				if (optionLabel == null && weigthOption == null)
					continue;

				if (weigthOption != null && weigthOption.intValue() > 100) {
					errors.add(EClimateErrorFile.OVERFLOW_CLIMATE_RESPONSE_WEIGHT);
					this.climateOptions
							.add(new ClimateTemplateResponseOptionDTO(currentRowNum, optionLabel, weigthOption, errors));
					break;
				}

				if (weigthOption != null && weigthOption.intValue() > min)
					min = weigthOption.intValue();
				else if (weigthOption != null && weigthOption.intValue() < max)
					max = weigthOption.intValue();
				else
					errors.add(EClimateErrorFile.INVALID_CLIMATE_RESPONSE_WEIGHT);

				this.climateOptions
						.add(new ClimateTemplateResponseOptionDTO(currentRowNum, optionLabel, weigthOption, errors));
			}
			if(this.climateOptions.isEmpty() && errors.isEmpty())
			{
				errors.add(EClimateErrorFile.EMPTY_CLIMATE_RESPONSE_OPTION);
				this.climateOptions.add(new ClimateTemplateResponseOptionDTO(2, null, null, errors));										

			}else if(this.climateOptions.isEmpty() && !errors.isEmpty())
			{
				this.climateOptions.add(new ClimateTemplateResponseOptionDTO(2, null, null, errors));				
			}
			else if(this.climateOptions.size() == 1)
			{
				errors.add(EClimateErrorFile.MIN_CLIMATE_RESPONSE_OPTION);
				this.climateOptions.add(new ClimateTemplateResponseOptionDTO(3, null, null, errors));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void iterateRowsAndCreateSDemographicOptions(MultipartFile template, String sheetName) {
		try {
			InputStream inputStream = template.getInputStream();
			this.workBook = new XSSFWorkbook(inputStream);
			Sheet sDemographicOptionsSheet = this.workBook.getSheet(sheetName);
			if (sDemographicOptionsSheet == null)
				return;

			Iterator<Row> optionsRows = sDemographicOptionsSheet.iterator();
			while (optionsRows.hasNext()) {
				Row currentRow = optionsRows.next();
				Cell codeCell = currentRow.getCell(0);
				Cell optionResponseCell = currentRow.getCell(1);
				if (currentRow.getRowNum() == 0 || codeCell == null || optionResponseCell == null)
					continue;

				Number sDemograficCode;
				String curentOptionResponse = "";
				if (codeCell.getCellType() == Cell.CELL_TYPE_NUMERIC
						&& String.valueOf(codeCell.getNumericCellValue()).matches(INTEGER_VALIDATION))
					sDemograficCode = new Double(codeCell.getNumericCellValue()).intValue();
				else if (codeCell.getCellType() == Cell.CELL_TYPE_STRING
						&& codeCell.getStringCellValue().matches(INTEGER_VALIDATION))
					sDemograficCode = Integer.valueOf(codeCell.getStringCellValue());
				else
					continue;
				List<String> optionsResponses = sDemograficOptions.get(sDemograficCode);
				if (optionsResponses == null)
					optionsResponses = new ArrayList<>();
				if (currentRow.getCell(1).getCellType() == Cell.CELL_TYPE_STRING)
					curentOptionResponse = currentRow.getCell(1).getStringCellValue();
				else if (currentRow.getCell(1).getCellType() == Cell.CELL_TYPE_NUMERIC)
					curentOptionResponse = String.valueOf(currentRow.getCell(1).getNumericCellValue());
				optionsResponses.add(curentOptionResponse);
				this.sDemograficOptions.put(sDemograficCode, optionsResponses);
			}

			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void validateQuestionSheet(MultipartFile template, String questionsSheetName,
			String questionsSheetNameValidator) {
		try {

			Sheet questionSheet = this.workBook.getSheet(questionsSheetName);
			Sheet questionSheetValidator = this.workBook.getSheet(questionsSheetNameValidator);
			if (questionSheet == null || questionSheetValidator == null)
				return;
			this.climateTemplateQuestions = this.iterateRowsAndCreateClimateTemplateQuestions(questionSheet,
					questionSheetValidator);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<ClimateTemplateQuestion> iterateRowsAndCreateClimateTemplateQuestions(Sheet questionPageSheet,
			Sheet questionPageValidatorSheet) {
		try {
			Map<String, EClimateQuestionType> eCQTMap = new HashMap<>();

			Iterator<Row> questionPageValidatorRows = questionPageValidatorSheet.iterator();
			while (questionPageValidatorRows.hasNext()) {
				Row currentRow = questionPageValidatorRows.next();

				String eClimateQuestionTypeText = currentRow.getCell(0).getStringCellValue();
				String traslate = currentRow.getCell(1).getStringCellValue();

				eCQTMap.put(traslate, EClimateQuestionType.valueOf(eClimateQuestionTypeText));
			}

			for (int i = QUESTIONS_SHEET_START__INDEX; questionPageSheet.getRow(i) != null; i++) {
				Row currentRow = questionPageSheet.getRow(i);
				ClimateTemplateQuestion currentCTQ = this.createClimateTemplateQuestion(currentRow, eCQTMap);
				if(currentCTQ != null)
					this.climateTemplateQuestions.add(currentCTQ);
			}
			this.validateClimateTemplateQuestions();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.climateTemplateQuestions;
	}

	private void validateClimateTemplateQuestions() {

		for (int i = 0; i < this.climateTemplateQuestions.size(); i++) {
			ClimateTemplateQuestion currentCTQ = this.climateTemplateQuestions.get(i);

			String climateQuestionText = currentCTQ.getQuestionText();
			String climateDimension = currentCTQ.getDimension() == null ? null: currentCTQ.getDimension().getName();
			Number climateDemographicCode = currentCTQ.getDemograficCode();
			EClimateQuestionType qType = currentCTQ.getType();
			List<EClimateErrorQuestion> errorsQuestion = new ArrayList<>();
			List<String> sDemograficResponses = this.sDemograficOptions.get(climateDemographicCode);

			Boolean hasDemographicCode = climateDemographicCode != null;
			Boolean isQTypeDemographic = qType == null ? false : qType.equals(EClimateQuestionType.SOCIODEMOGRAPHIC);
			Boolean isQTypeClimate = qType == null ? false : qType.equals(EClimateQuestionType.CLIMATE_LABEL);
			Boolean isQTypeENPS = qType == null ? false : qType.equals(EClimateQuestionType.ENPS);
			Boolean hasResponse = sDemograficResponses != null;
			Boolean isValidsDemographic = hasDemographicCode && isQTypeDemographic;

			this.hasSDemographic = this.hasSDemographic || isQTypeDemographic;
			this.hasClimate = this.hasClimate || isQTypeClimate;
			this.hasENPS = this.hasENPS || isQTypeENPS;

			if (climateQuestionText == null)
				errorsQuestion.add(EClimateErrorQuestion.EMPTY_QUESTION);

			if (climateDimension == null && (isQTypeClimate || isQTypeENPS))
				errorsQuestion.add(EClimateErrorQuestion.EMPTY_DIMENSION);

			if (isQTypeDemographic && !hasDemographicCode)
				errorsQuestion.add(EClimateErrorQuestion.EMPTY_CODE);

			if (qType == null || hasDemographicCode && !isQTypeDemographic)
				errorsQuestion.add(EClimateErrorQuestion.INVALID_TYPE);

			if (isValidsDemographic && hasResponse && sDemograficResponses.size() >= 2)
				currentCTQ.setsDemograficResponses(sDemograficResponses);
			else if (sDemograficResponses != null && sDemograficResponses.size() < 2)
				errorsQuestion.add(EClimateErrorQuestion.LACK_SDEMOGRAPHIC_RESPONSES);
			else if (isValidsDemographic && !hasResponse)
				errorsQuestion.add(EClimateErrorQuestion.EMPTY_SDEMOGRAPHIC_RESPONSES);

			if (isQTypeENPS && this.eNPSOptions.size() != 3)
				errorsQuestion.add(EClimateErrorQuestion.EMPTY_ENPS_RESPONSES);

			if (!errorsQuestion.isEmpty())
				currentCTQ.setIsValid(false);

			currentCTQ.setErrorsQuestion(errorsQuestion);
		}
	}

	private ClimateTemplateQuestion createClimateTemplateQuestion(Row currentRow,
			Map<String, EClimateQuestionType> eCQTMap) {
		if(this.isEmptyRow(currentRow)) return null;
		Number rownNumber = currentRow.getRowNum() + 1;
		String climateQuestion = null;
		String climateQuestionDesc = null;
		String climateFactor = null;
		String climateDimension = null;
		Number climateDemographicCode = null;
		EClimateQuestionType eclimateType = null;

		Cell climateQuestionCell = currentRow.getCell(CLIMATE_QUESTION_INDEX);
		Cell climateQuestionDescCell = currentRow.getCell(CLIMATE_QUESTION_DESC_INDEX);
		Cell climateFactorCell = currentRow.getCell(CLIMATE_FACTOR_INDEX);
		Cell climateDimensionCell = currentRow.getCell(CLIMATE_DIMENSION_INDEX);
		Cell climateTypeCell = currentRow.getCell(CLIMATE_TYPE_INDEX);
		Cell climateDemographicCodeCell = currentRow.getCell(CLIMATE_CODE_INDEX);
		Cell climateRequiredCell = currentRow.getCell(CLIMATE_REQUIRED_INDEX);
		
		ClimateDimensionDTO dimension = null;
		ClimateFactorDTO factor = null;
		
		if (climateQuestionCell != null && !climateQuestionCell.getStringCellValue().isEmpty())
			climateQuestion = climateQuestionCell.getStringCellValue().trim();
		if (climateQuestionDescCell != null && !climateQuestionDescCell.getStringCellValue().isEmpty())
			climateQuestionDesc = climateQuestionDescCell.getStringCellValue().trim();
		if (climateFactorCell != null && !climateFactorCell.getStringCellValue().isEmpty())
			climateFactor = climateFactorCell.getStringCellValue().trim();
		if (climateDimensionCell != null && !climateDimensionCell.getStringCellValue().isEmpty())
			climateDimension = climateDimensionCell.getStringCellValue().trim();
		if (climateTypeCell != null && !climateTypeCell.getStringCellValue().isEmpty())
			eclimateType = eCQTMap.get(climateTypeCell.getStringCellValue().trim());
		if (climateDemographicCodeCell != null && climateDemographicCodeCell.getCellType() == Cell.CELL_TYPE_NUMERIC
				&& String.valueOf(climateDemographicCodeCell.getNumericCellValue()).matches(INTEGER_VALIDATION))
			climateDemographicCode = new Double(climateDemographicCodeCell.getNumericCellValue()).intValue();
		
		if(eclimateType != null && (EClimateQuestionType.CLIMATE_LABEL.compareTo(eclimateType) == 0 || EClimateQuestionType.ENPS.compareTo(eclimateType) == 0))
		{
			if (climateDimension != null && !dimensionsMap.containsKey(climateDimension)) {
				dimension = new ClimateDimensionDTO(dimensionsMap.size(), climateDimension);
				dimensionsMap.put(climateDimension, dimensionsMap.size());
			} else if (climateDimension != null)
				dimension = new ClimateDimensionDTO(dimensionsMap.get(climateDimension), climateDimension);
			
			if (climateFactor != null && !factorsMap.containsKey(climateFactor)) {
				factor = new ClimateFactorDTO(factorsMap.size(), climateFactor);
				factorsMap.put(climateFactor, factorsMap.size());
			} else if (climateFactor != null)
				factor = new ClimateFactorDTO(factorsMap.get(climateFactor), climateFactor);
		}
		
		boolean requiredQuestion = climateRequiredCell != null && climateRequiredCell.getCellType() == Cell.CELL_TYPE_STRING
				&& !climateRequiredCell.getStringCellValue().startsWith("OP");

		return new ClimateTemplateQuestion(rownNumber, climateQuestion, climateQuestionDesc, factor, dimension,
				eclimateType, climateDemographicCode, requiredQuestion);
		
	}

	private boolean isEmptyRow(Row currentRow) {
		for (Cell cell : currentRow)
			if(cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK )
				return false;
		return true;
	}

}
