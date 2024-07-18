package com.acsendo.api.survey.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.survey.dto.QuestionDTO;
import com.acsendo.api.survey.dto.ResponseDTO;
import com.acsendo.api.survey.dto.ResultsIndividualDTO;
import com.acsendo.api.survey.dto.SurveyResponseExcelDTO;
import com.acsendo.api.survey.dto.TemplateQuestionDTO;
import com.acsendo.api.survey.service.QuestionService;

@Component
public class SurveyExcelTemplateHandler {
	
	private final int MAX_ROW = 200;
	
	@Autowired
	private LabelRepository labelRepository;
	
	@Autowired
	private QuestionService questionService;
	
	private String language = "";
	
	
	public byte[] generateSurveyExcelTemplate(String language) {

		byte[] result = null;
		
		this.language = language;
		
		// Crea un libro de excel
		Workbook workBook = new XSSFWorkbook();

		// Creamos la Hoja de Excel
		Sheet sheetOne = workBook.createSheet("Cargue_De_Encuesta");
		
		this.setNewSurveyKeys(workBook, sheetOne);

		// Anadimos el titulo de la hoja
		Row rowTitle = sheetOne.createRow(1);
		Row rowHeader = sheetOne.createRow(6);
		Row rowTip1 = sheetOne.createRow(2);
		Row rowTip2 = sheetOne.createRow(3);
		Row rowTip3 = sheetOne.createRow(4);

		Cell cellTitle = rowTitle.createCell(0);
		cellTitle.setCellValue("Acsendo");
		
		Cell cellTipsTitle = rowTitle.createCell(4);
		cellTipsTitle.setCellValue(getLabel("tips", ESurveyLabelExcelTemplate.COMPETENCES));
		
		Cell cellTips1 = rowTip1.createCell(4);
		cellTips1.setCellValue(getLabel("tip_text_1", ESurveyLabelExcelTemplate.SURVEY));
		
		Cell cellTips2 = rowTip2.createCell(4);
		cellTips2.setCellValue(getLabel("tip_text_2", ESurveyLabelExcelTemplate.SURVEY));

		Cell cellTips3 = rowTip3.createCell(4);
		cellTips3.setCellValue(getLabel("tip_text_3", ESurveyLabelExcelTemplate.SURVEY));
		
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("E3:G3"));
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("E4:G4"));
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("E5:K5"));
		
		
		
		cellTitle.setCellStyle(getMainTitleStyle(workBook, sheetOne));
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("A2:D6"));

		String[] headersNames = buildArrayOfHeadLabel();

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataValidation(workBook);
		
		try {

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workBook.write(out);
			out.close();
			result = out.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public byte[] generateAddPartakersExcelTemplate(String language) {

		byte[] result = null;
		
		this.language = language;
		
		// Crea un libro de excel
		Workbook workBook = new XSSFWorkbook();

		// Creamos la Hoja de Excel
		Sheet sheetOne = workBook.createSheet("Participantes");
		
		Row keyRow = sheetOne.createRow(0);
		Cell cellKey = keyRow.createCell(0);
		cellKey.setCellValue("email");
		cellKey.setCellStyle(getStyleKeyCell(workBook, sheetOne));
		

		// Anadimos el titulo de la hoja
		Row rowTitle = sheetOne.createRow(1);
		Row rowHeader = sheetOne.createRow(6);
		Row rowTip1 = sheetOne.createRow(2);
		Row rowTip2 = sheetOne.createRow(3);


		Cell cellTitle = rowTitle.createCell(0);
		cellTitle.setCellValue("Acsendo");
		
		Cell cellTipsTitle = rowTitle.createCell(4);
		cellTipsTitle.setCellValue(getLabel("tips", ESurveyLabelExcelTemplate.COMPETENCES));
		
		Cell cellTips1 = rowTip1.createCell(4);
		cellTips1.setCellValue(getLabel("tip_text_1_partakers", ESurveyLabelExcelTemplate.SURVEY));
		
		Cell cellTips2 = rowTip2.createCell(4);
		cellTips2.setCellValue(getLabel("tip_text_2_partakers", ESurveyLabelExcelTemplate.SURVEY));
		
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("E3:J3"));
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("E4:J4"));
		
		
		
		cellTitle.setCellStyle(getMainTitleStyle(workBook, sheetOne));
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("A2:D6"));

		String[] headersNames = {this.getLabel("email_contact", ESurveyLabelExcelTemplate.SURVEY)};

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		try {

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workBook.write(out);
			out.close();
			result = out.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public byte[] generateSurveyResponseExcel(SurveyResponseExcelDTO filter) {

		byte[] result = null;
		
		
		// Crea un libro de excel
		Workbook workBook = new XSSFWorkbook();
		
		this.language = filter.getLanguage();
		

		createResultsSheet(filter, workBook, this.getLabel("results", ESurveyLabelExcelTemplate.COMMON));

		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workBook.write(out);
			out.close();
			result = out.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;

	}
	
	private void createResultsSheet(SurveyResponseExcelDTO data, Workbook workBook, String title) {

		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add("partaker");
		headers.add("title_xls");
		headers.add("answer");
		
		if(data.getExtraFields() != null) {
			if(data.getExtraFields().getNamesExtraFields() != null && data.getExtraFields().getNamesExtraFields().size() > 0) {
				for(String field : data.getExtraFields().getNamesExtraFields()) {
					headers.add(field);
				}
			}
		}
		
		String[] headersNames = this.getHeaderLabels(headers);
		
		
		
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataResponse(data, rowNumber, sheetOne);
	}
	
	
	private void setDataResponse(SurveyResponseExcelDTO data, int row, Sheet sheetOne) {
		if(data.getResults() != null) {
			int idPartaker = 1;
			for (ResultsIndividualDTO temp : data.getResults()) {
				if(temp != null && temp.getListQuestions() != null && temp.getListQuestions().size() != 0) {
					for(QuestionDTO question : temp.getListQuestions()) {
						for(ResponseDTO response : question.getResponseResultsList()) {
							String name = this.getLabel("partaker", ESurveyLabelExcelTemplate.SURVEY) + ' ' + idPartaker;
							row++;
							int celIdex = 0;
							Row newRow = sheetOne.createRow(row);
							Cell cellNamePartaker = newRow.createCell(celIdex);
							Cell cellQuestion = newRow.createCell(celIdex+1);
							Cell cellAnswer = newRow.createCell(celIdex+2);
							
							int lastCell = celIdex + 2;
							
							if(temp.getPartakerName() != null && temp.getPartakerName() != "") {
								name = name + " (" + temp.getPartakerName() + " )";
							}
							
							cellNamePartaker.setCellValue(name);
							cellQuestion.setCellValue(question.getQuestionName());
							
							if(question.getQuestionCode().equals("DATAUSER") || question.getQuestionCode().equals("TEXTSHORT") || question.getQuestionCode().equals("TEXTLONG")) {
								cellAnswer.setCellValue(response.getResponseOptionValue());
							}else if(question.getQuestionCode().equals("PRIORIZATION")) {
								cellAnswer.setCellValue(response.getResponseOption().getValue() + " = " + response.getResponseOptionValue());
							}else {
								cellAnswer.setCellValue(response.getResponseOption().getValue());
							}
							
							if(data.getExtraFields() != null) {
								if(data.getExtraFields().getMapExtraFieldsPartakers() != null && data.getExtraFields().getMapExtraFieldsPartakers().size() > 0) {
									List<String> cadExtraFieldsEmployee = data.getExtraFields().getMapExtraFieldsPartakers().get(temp.getPartakerId());
									if(cadExtraFieldsEmployee != null) {
										for(String fieldEmployee : cadExtraFieldsEmployee) {
											Cell tempCell = newRow.createCell(lastCell + 1);
											tempCell.setCellValue(fieldEmployee);
										}
									}
								}
							}
							
							celIdex++;
						}
					}
				}
				idPartaker++;
			}
		}
	}
	
	
	/**
	 * Estilos usados para las celdas cabeceras de la tabla
	 * 
	 * @param workBook
	 * @param sheet
	 * @return
	 */
	private CellStyle getMainTitleStyle(Workbook workBook, Sheet sheet) {

		Font headerFont = workBook.createFont();
		headerFont.setColor(IndexedColors.GREY_80_PERCENT.index);
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);;
		headerFont.setFontHeightInPoints((short) 50);
		headerFont.setFontName("Arial Black");

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFont(headerFont);

		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		return cellStyle;
	}
	
	/**
	 * Estilo para header de tabla
	 * 
	 * @param workBook
	 * @param sheet
	 * @return
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
	
	private CellStyle getStyleKeyCell(Workbook workBook, Sheet sheet) {

		Font fontKey = workBook.createFont();
		fontKey.setColor(IndexedColors.WHITE.index);

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFont(fontKey);

		return cellStyle;
	}
	
	private String[] buildArrayOfHeadLabel() {

		List<String> labelObject = new ArrayList<String>();
		
		labelObject.add(getLabel("title_xls",ESurveyLabelExcelTemplate.SURVEY));
		labelObject.add(getLabel("description_xls",ESurveyLabelExcelTemplate.SURVEY));
		labelObject.add(getLabel("question_type_xls",ESurveyLabelExcelTemplate.SURVEY));
		labelObject.add(getLabel("options_response_xls",ESurveyLabelExcelTemplate.SURVEY));
		labelObject.add(getLabel("requiered_xls",ESurveyLabelExcelTemplate.SURVEY));
		labelObject.add(getLabel("time_xls",ESurveyLabelExcelTemplate.SURVEY));		
		return labelObject.toArray(new String[0]);

	}
	
	public String getLabel(String labelCode, ESurveyLabelExcelTemplate module) {
		Label labelTemp = null;		
		labelTemp = labelRepository.findByModuleCode(module.toString().toLowerCase(), labelCode);

		if(labelTemp != null) {
			if(this.language.equals("es")) {
				return labelTemp.getSpanish();
			}else if(this.language.equals("en")) {
				return labelTemp.getEnglish();
			}else {
				return labelTemp.getPortuguese();
			}
		}else {
			return labelCode;
		}
	}
	
	/**
	 * Crea los validadores para la hoja de excel
	 * 
	 * @param wb
	 */
	private void setDataValidation(Workbook wb) {

		String[] dataFillQuestionType= buildArrayQuestionTypeValidator();
		String[] dataFillRequired = buildArrayRequiredValidator();
		
		// Crea hoja para los datos
		String nameSheet = "";
		Sheet sheet = null;
		int indexColumn = 0;

		if(dataFillQuestionType.length != 0) {
			nameSheet = "question_type";
			sheet = wb.createSheet(nameSheet);
			indexColumn = 2;

			// llenado de datos
			for (int i = 0; i < dataFillQuestionType.length; i++) {
				Row row = sheet.createRow(i);
				Cell cellHeader = row.createCell(0);
				cellHeader.setCellValue(dataFillQuestionType[i]);
				sheet.setColumnWidth(i, dataFillQuestionType[i].length() * 256);
			}
			nameSheet = "'" + nameSheet + "'!$A$1:$A$" + dataFillQuestionType.length;
			loadDataValidation(wb, indexColumn, nameSheet);
		}
		
		if(dataFillRequired.length != 0) {
			nameSheet = "required";
			sheet = wb.createSheet(nameSheet);
			indexColumn = 4;

			// llenado de datos
			for (int i = 0; i < dataFillRequired.length; i++) {
				Row row = sheet.createRow(i);
				Cell cellHeader = row.createCell(0);
				cellHeader.setCellValue(dataFillRequired[i]);
				sheet.setColumnWidth(i, dataFillRequired[i].length() * 256);
			}
			nameSheet = "'" + nameSheet + "'!$A$1:$A$" + dataFillRequired.length;
			loadDataValidation(wb, indexColumn, nameSheet);
		}
		

	}
	
	/**
	 * Inserta las validaciones de datos prederminadas en el excel
	 * 
	 * @param wb
	 * @param columnIndex
	 * @param listData
	 */
	private void loadDataValidation(Workbook wb, int columnIndex, String preLoadData) {
		Sheet sheet = wb.getSheetAt(0);
		int rowIndex = 7;// a partir de la fila 8 indice 7
		// crear el constraint
		XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
		// celdas donde estaran las listas
		CellRangeAddressList addressList = new CellRangeAddressList(rowIndex, MAX_ROW, columnIndex, columnIndex);
		XSSFDataValidation validation;

		// Toma los datos de la referencia indicada
		String formula = preLoadData;
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
				.createFormulaListConstraint(formula.toString());
		validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);

		validation.setShowErrorBox(true);
		validation.setSuppressDropDownArrow(true);
		sheet.addValidationData(validation);
	}
	
	private String[] buildArrayQuestionTypeValidator() {
		
		List<TemplateQuestionDTO> listQuestion = this.questionService.getListTemplateQuestionsService();
		List<String> labelObject = new ArrayList<String>();	
		
		for(TemplateQuestionDTO temp : listQuestion) {
			String label = temp.getQuestion().toString().split("\\.")[1];
			labelObject.add(getLabel(label, ESurveyLabelExcelTemplate.SURVEY));
		}
		

		return labelObject.toArray(new String[0]);

	}

	private String[] buildArrayRequiredValidator() {
		
		List<String> labelObject = new ArrayList<String>();	
		
		labelObject.add(getLabel(ESurveyLabelExcelTemplate.YES.toString().toLowerCase(), ESurveyLabelExcelTemplate.COMMON));
		labelObject.add(getLabel(ESurveyLabelExcelTemplate.NO.toString().toLowerCase(), ESurveyLabelExcelTemplate.COMMON));
			
		return labelObject.toArray(new String[0]);

	}
	
	/**
	 * Método que setea estilos base y nombre de una hoja
	 * @param workBook
	 * @param nameSheet
	 * @return
	 */
	private Sheet createBaseSheet(Workbook workBook, String nameSheet) {
		// Creamos la Hoja de Excel
		Sheet sheetOne = workBook.createSheet(nameSheet);
		// Anadimos el titulo de la hoja
		Row rowTitle = sheetOne.createRow(1);
		Cell cellTitle = rowTitle.createCell(0);
		cellTitle.setCellValue("acsendo");
		cellTitle.setCellStyle(getMainTitleStyle(workBook, sheetOne));
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("A2:D6"));
		return sheetOne;
	}
	
	/**
	 * Obtiene Labels de los headers de la tabla según el idioma de la empresa
	 * @param headers 
	 * @return
	 */
	private String[] getHeaderLabels(List<String> headers) {

		List<String> headerLabels = new ArrayList<String>();
		
		for(String temp : headers) {
			headerLabels.add(getLabel(temp ,ESurveyLabelExcelTemplate.SURVEY));
		}
		return headerLabels.toArray(new String[0]);

	}
	
	private void setNewSurveyKeys(Workbook wb, Sheet sh) {
		String[] keys = {"title","description", "question_type", "options_response", "requiered", "time"};
 		Row keyRow = sh.createRow(0);
		
 		int i = 0;
 		for(String key:keys) {
 			Cell cellKey = keyRow.createCell(i);
 			cellKey.setCellValue(key);
 			cellKey.setCellStyle(getStyleKeyCell(wb, sh));
 			i++;
 		}
	}
	
}
