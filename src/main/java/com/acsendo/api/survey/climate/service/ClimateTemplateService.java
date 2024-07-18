package com.acsendo.api.survey.climate.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.acsendo.api.climate.dto.ClimateEvaluationTemplateContentDTO;
import com.acsendo.api.climate.enumerations.EClimateQuestionType;
import com.acsendo.api.climate.enumerations.ELabelClimateExcelTemplate;
import com.acsendo.api.climate.model.ClimateModel;
import com.acsendo.api.climate.repository.ClimateModelRepository;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.survey.climate.handler.ClimateExcelTemplateHandler;
import com.acsendo.api.survey.climate.handler.ClimateExcelTemplateValidator;

@Service
public class ClimateTemplateService {

	@Autowired
	private ClimateExcelTemplateHandler climateExcelTemplateHandler;
	@Autowired
	private ClimateExcelTemplateValidator climateExcelTemplateValidator;

	@Autowired
	private ClimateModelRepository climateModelRepository;

	@Autowired
	private LabelRepository labelRepository;

	public byte[] getExcelTemplate(String companyLanguage) {
		String templateName = getLabel(ELabelClimateExcelTemplate.TITLE_HEADER_CLIMATE, companyLanguage);

		List<List<String>> sheetNames = this.createSheetNamesWithDesc(companyLanguage);

		List<String> headersQuestion = this.createHeadersQuestionSheet(companyLanguage);
		List<String> tipsHeadersQuestion = this.createTipsHeadersQuestionSheet(companyLanguage);

		List<String> headersGeneralAnswers = this.createHeadersGeneralAnswersSheet(companyLanguage);
		List<String> tipsHeadersGeneralAnswers = Arrays.asList("",
				getLabel(ELabelClimateExcelTemplate.CLIMATE_WEIGHT_TIP, companyLanguage));

		List<String> headersScaleNPS = this.createHeadersScaleNPS(companyLanguage);
		List<String> tipsHeadersScaleNPS = Arrays
				.asList(getLabel(ELabelClimateExcelTemplate.CLIMATE_ENPS_CUSTOM_LABEL_TIP, companyLanguage), "", "");

		List<String> headersDemograficAnswers = Arrays.asList(
				getLabel(ELabelClimateExcelTemplate.CLIMATE_CODE, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.CLIMATE_SOCIODEMOGRAPHIC_RESPONSE, companyLanguage));
		List<String> tipsHeadersDemograficAnswers = Arrays.asList(
				getLabel(ELabelClimateExcelTemplate.CLIMATE_SOCIODEMOGRAPHIC_RESPONSE_CODE_TIP, companyLanguage), "");

		List<List<String>> workBookHeaders = Arrays.asList(headersQuestion, headersGeneralAnswers, headersScaleNPS,
				headersDemograficAnswers, new ArrayList<String>(), new ArrayList<String>());
		List<List<String>> workBookHeadersTips = Arrays.asList(tipsHeadersQuestion, tipsHeadersGeneralAnswers,
				tipsHeadersScaleNPS, tipsHeadersDemograficAnswers, new ArrayList<String>(), new ArrayList<String>());

		byte[] workBookTemplate = this.climateExcelTemplateHandler.generateClimateExcelWorkBookTemplate(templateName,
				sheetNames, workBookHeaders, workBookHeadersTips);

		List<EClimateQuestionType> validatorQuestionsPage = new ArrayList<EClimateQuestionType>(
				Arrays.asList(EClimateQuestionType.values()));
		List<String> validatorTraslateQuestionsPage = Arrays.asList(
				getLabel(EClimateQuestionType.CLIMATE_LABEL, companyLanguage),
				getLabel(EClimateQuestionType.ENPS, companyLanguage),
				getLabel(EClimateQuestionType.OPEN, companyLanguage),
				getLabel(EClimateQuestionType.SOCIODEMOGRAPHIC, companyLanguage));
		workBookTemplate = this.climateExcelTemplateHandler.createValidatorsEnum(workBookTemplate,
				ELabelClimateExcelTemplate.TYPE_OPTIONS.name(), validatorQuestionsPage, validatorTraslateQuestionsPage,
				4);
		List<ELabelClimateExcelTemplate> validatorRequiredQuestionsPage = Arrays
				.asList(ELabelClimateExcelTemplate.REQUIRED, ELabelClimateExcelTemplate.OPTIONAL);
		validatorTraslateQuestionsPage = Arrays.asList(getLabel(ELabelClimateExcelTemplate.OPTIONAL, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.REQUIRED, companyLanguage));
		workBookTemplate = this.climateExcelTemplateHandler.createValidatorsEnum(workBookTemplate,
				ELabelClimateExcelTemplate.REQUIRED.name(), validatorRequiredQuestionsPage,
				validatorTraslateQuestionsPage, 6);
		workBookTemplate = this.climateExcelTemplateHandler.hideSheet(workBookTemplate,
				ELabelClimateExcelTemplate.REQUIRED.name());
		workBookTemplate = this.climateExcelTemplateHandler.hideSheet(workBookTemplate,
				ELabelClimateExcelTemplate.TYPE_OPTIONS.name());
		return this.addDefaultValues(workBookTemplate, companyLanguage);
	}

	private byte[] addDefaultValues(byte[] workBookTemplate, String companyLanguage) {
		List<String> defaultValues = Arrays.asList(getLabel(ELabelClimateExcelTemplate.RETRACTORS, companyLanguage),
				"0", "3", getLabel(ELabelClimateExcelTemplate.NEUTRAL, companyLanguage), "4", "7",
				getLabel(ELabelClimateExcelTemplate.PROMOTERS, companyLanguage), "8", "10");
		workBookTemplate = this.climateExcelTemplateHandler.addDefaultValues(workBookTemplate,
				getLabel(ELabelClimateExcelTemplate.CLIMATE_ENPS_SCALE, companyLanguage), defaultValues, 3);
		return workBookTemplate;
	}

	private List<String> createHeadersScaleNPS(String companyLanguage) {
		return Arrays.asList(getLabel(ELabelClimateExcelTemplate.CLIMATE_ENPS_CUSTOM_LABEL, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.CLIMATE_ENPS_MIN_LIMIT, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.CLIMATE_ENPS_MAX_LIMIT, companyLanguage));

	}

	private List<String> createHeadersGeneralAnswersSheet(String companyLanguage) {
		return Arrays.asList(getLabel(ELabelClimateExcelTemplate.ANSWER, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.WEIGHT, companyLanguage));
	}

	private List<String> createTipsHeadersQuestionSheet(String companyLanguage) {
		return Arrays.asList("", "", "", "",
				getLabel(ELabelClimateExcelTemplate.CLIMATE_QUESTION_TYPE_TIP, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.CLIMATE_QUESTION_CODE_TIP, companyLanguage), "");
	}

	private List<String> createHeadersQuestionSheet(String companyLanguage) {
		return Arrays.asList(getLabel(ELabelClimateExcelTemplate.CLIMATE_QUESTION, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.CLIMATE_QUESTION_DESC, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.CLIMATE_FACTOR, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.CLIMATE_DIMENSION, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.TYPE, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.CLIMATE_CODE, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.REQUIRED, companyLanguage));
	}

	private List<List<String>> createSheetNamesWithDesc(String companyLanguage) {
		List<String> questions = Arrays.asList(getLabel(ELabelClimateExcelTemplate.QUESTIONS, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.QUESTION_SHEET_DESC, companyLanguage, true));
		List<String> climate = Arrays.asList(getLabel(ELabelClimateExcelTemplate.CLIMATE_RESPONSE, companyLanguage),
				"");
		List<String> eNPS = Arrays.asList(getLabel(ELabelClimateExcelTemplate.CLIMATE_ENPS_SCALE, companyLanguage),
				getLabel(ELabelClimateExcelTemplate.ENPS_SHEET_DESC, companyLanguage, true));
		List<String> sDemographic = Arrays
				.asList(getLabel(ELabelClimateExcelTemplate.CLIMATE_SOCIODEMOGRAPHIC_RESPONSE, companyLanguage), "");
		List<String> type = Arrays.asList(ELabelClimateExcelTemplate.TYPE_OPTIONS.name(), "");
		List<String> required = Arrays.asList(ELabelClimateExcelTemplate.REQUIRED.name(), "");
		return Arrays.asList(questions, climate, eNPS, sDemographic, type, required);
	}

	private <T extends Enum<T>> String getLabel(T labelCode, String companyLanguage, Boolean... IgnoreCase ) {
		Optional<List<Label>> labels = this.labelRepository.findByCode(labelCode.toString().toLowerCase());
		if (!labels.isPresent())
			return " ";
		Label l = labels.get().get(0);
		Boolean ignoreCase = IgnoreCase.length > 0;
		if (companyLanguage.equalsIgnoreCase("en"))
			return ignoreCase ? l.getEnglish() : l.getEnglish().toUpperCase();
		else if (companyLanguage.equalsIgnoreCase("pt"))
			return ignoreCase ? l.getPortuguese(): l.getPortuguese().toUpperCase();

		return ignoreCase ? l.getSpanish(): l.getSpanish().toUpperCase();

	}

	public ClimateEvaluationTemplateContentDTO createClimateEvaluationFromTemplate(MultipartFile template,
			Long companyId, String companyLanguage) {
		return this.climateExcelTemplateValidator.validateTemplateFile(template, companyId,
				this.createSheetNamesWithDesc(companyLanguage));
	}

	/**
	 * Obtiene plantilla excel para carga de participantes
	 * 
	 * @param modelId Identificador de un modelo de clima
	 */
	public byte[] getPartakerTemplate(Long modelId) {

		byte[] result = null;
		Optional<ClimateModel> climateModel = climateModelRepository.findById(modelId);
		if (climateModel.isPresent()) {
			Company company = climateModel.get().getCompany();
			String companyLanguage = company.getLanguageCode();

			// Crea el libro de excel con las respectivas hojas
			Workbook workBook = new XSSFWorkbook();
			createSheetPartakerNames(companyLanguage, workBook,
					getLabel(ELabelClimateExcelTemplate.PARTAKERS, companyLanguage));
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				workBook.write(out);
				out.close();
				result = out.toByteArray();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private void createSheetPartakerNames(String companyLanguage, Workbook workBook, String title) {

		Sheet sheetOne = workBook.createSheet(title);
		int rowNumber = 0;
		Row rowHeader = sheetOne.createRow(rowNumber);

		List<String> headers = new ArrayList<String>();
		headers.add(getLabel(ELabelClimateExcelTemplate.NAME, companyLanguage));
		headers.add(getLabel(ELabelClimateExcelTemplate.EMAIL, companyLanguage));

		// Establece los headers
		for (int i = 0; i < headers.size(); i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headers.get(i));
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 42 puntos
			sheetOne.setColumnWidth(i, 42 * 256);
		}
	}

	/**
	 * Aplica los estillos a los headers del reporte
	 * 
	 * @param workBook
	 * @param sheet
	 */
	private CellStyle getStyleHeaders(Workbook workBook, Sheet sheet) {

		Font headerFont = workBook.createFont();
		headerFont.setColor(IndexedColors.WHITE.index);
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.index);
		cellStyle.setFillPattern((short) 1);
		cellStyle.setFont(headerFont);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		return cellStyle;
	}
}
