package com.acsendo.api.survey.util;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.acsendo.api.climate.dto.ClimatePartakerDTO;
import com.acsendo.api.enumerations.ELanguageCodes;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.hcm.service.CompanyService;

/**
 * Gestiona el reporte excel que se genera en la gestión de participantes de una evaluación de clima *
 */
@Component
public class PartakersClimateExcelHandler {

	private String language;
	DecimalFormat df = new DecimalFormat("0.00");

	@Autowired
	private CompanyService companyService;

	@Autowired
	private LabelRepository labelRepository;
	

	// Códigos para etiquetas de las hojas del reporte
	public static final String COLLABORATORS_SHEET = "collaborators";
	// Códigos para etiquetas de los títulos de las tablas del reporte
	public static final String CONTRIBUTOR_TITLE = "contributor_name";
	public static final String CHARGE_TITLE = "charge";
	public static final String SURVEY_PREVIEW_TITLE = "survey_preview";
	public static final String STATE_TITLE = "state";
	

	/**
	 * Método principal de la creación del reporte excel de clima
	 * 
	 * @param partakersData     Información de participantes
	 * @param companyId         Identificador de la compañía
	 */
	public byte[] getClimateExcelReport(List<ClimatePartakerDTO> partakersData, Long companyId) {
		
		byte[] result = null;
		setLanguage(companyId);
		
		// Crea el libro de excel con las respectivas hojas
		Workbook workBook = new XSSFWorkbook();
		if (partakersData != null && partakersData.size() > 0) {
			createPartakersResults(partakersData, workBook, getLabel("COMMON", COLLABORATORS_SHEET));
		}			
	 

		// Crea el flujo del reporte
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

	/**
	 * Crea la hoja de información de los participantes
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createPartakersResults(List<ClimatePartakerDTO> rawData, Workbook workBook, String title) {
		
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(CONTRIBUTOR_TITLE);
		headers.add(CHARGE_TITLE);
		headers.add(SURVEY_PREVIEW_TITLE);
		headers.add(STATE_TITLE);

		String[] headersNames = this.getHeaderLabels(headers);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setPartakersResults(rawData, rowNumber, sheetOne);
	}
	
	/**
	 * Método que inserta los datos en la hoja de resultados de participantes
	 * 
	 * @param satisfaction Datos a mostrarse en la hoja
	 * @param row          Fila de la hoja de excel
	 * @param sheetOne     Representación de una hoja de cálculo de Excel
	 */
	private void setPartakersResults(List<ClimatePartakerDTO> partakers, int row, Sheet sheetOne) {
		
		Map<String, String> map=new HashMap<String, String>();
		map.put("CREATED", getLabel("CLIMATE", "participant_state_CREATED"));
		map.put("SENDED", getLabel("CLIMATE", "participant_state_SENDED"));
		map.put("IN_PROGRESS", getLabel("CLIMATE", "participant_state_IN_PROGRESS"));
		map.put("FINISHED", getLabel("CLIMATE", "participant_state_FINISHED"));
		
		
		for (ClimatePartakerDTO data : partakers) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellContributorName = newRow.createCell(celIdex);
			Cell cellJobName = newRow.createCell(celIdex + 1);
			Cell cellSurvey = newRow.createCell(celIdex + 2);
			Cell cellState = newRow.createCell(celIdex + 3);
			
			cellContributorName.setCellValue(data.getName());
			cellJobName.setCellValue(data.getJobName());
			Double percentage = data.getPercentageAnswered().doubleValue();
			cellSurvey.setCellValue(df.format(new Double(percentage.toString())));
			String labelState = map.get(data.getPartakerState().toString()) ;
			cellState.setCellValue(labelState);
			celIdex++;
		}
	}
	

	/**
	 * Método que setea estilos base y nombre de una hoja
	 * 
	 * @param workBook  Representación del libro de Excel
	 * @param nameSheet Nombre de la hoja de cálculo
	 */
	private Sheet createBaseSheet(Workbook workBook, String nameSheet) {
		// Creamos la Hoja de Excel
		Sheet sheetOne = workBook.createSheet(nameSheet);
		// Añadimos el titulo de la hoja
		Row rowTitle = sheetOne.createRow(1);
		Cell cellTitle = rowTitle.createCell(0);
		cellTitle.setCellValue(getLabel("COMMON", "title_climate_partakers"));
		cellTitle.setCellStyle(getMainTitleStyle(workBook, sheetOne));
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("A2:D6"));
		return sheetOne;
	}

	private CellStyle getMainTitleStyle(Workbook workBook, Sheet sheet) {

		Font headerFont = workBook.createFont();
		headerFont.setColor(IndexedColors.GREY_80_PERCENT.index);
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerFont.setFontHeightInPoints((short) 35);
		headerFont.setFontName("Poppins");

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFont(headerFont);

		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		return cellStyle;
	}

	/**
	 * Obtiene labels de los headers de la tabla según el idioma de la empresa
	 * 
	 * @param headers Títulos de las tablas
	 */
	private String[] getHeaderLabels(List<String> headers) {
		List<String> headerLabels = new ArrayList<String>();

		for (String code : headers) {
			headerLabels.add(getLabel("CLIMATE", code));
		}
		return headerLabels.toArray(new String[0]);
	}

	/**
	 * Aplica los estillos a los headers del reporte
	 * 
	 * @param workBook Representación del libro de Excel
	 * @param sheet    Representación de una hoja de cálculo de Excel
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
	
	/**
	 * Obtiene un label según el idioma de la empresa
	 * 
	 * @param module    Módulo al que pertenece la etiqueta
	 * @param labelCode Código de la etiqueta
	 */
	public String getLabel(String module, String labelCode) {
		Label labelTemp = null;
		labelTemp = labelRepository.findByModuleCode(module.toString().toLowerCase(),
				labelCode.toString().toLowerCase());
		if (labelTemp == null) labelTemp = labelRepository.findByModuleCode(module.toString().toLowerCase(),
				labelCode.toString());
		
		if (labelTemp != null) {
			if (this.language.equals(ELanguageCodes.es.toString())) {
				return labelTemp.getSpanish();
			} else if (this.language.equals(ELanguageCodes.en.toString())) {
				return labelTemp.getEnglish();
			} else {
				return labelTemp.getPortuguese();
			}
		}
		return labelCode;
	}

	/**
	 * Establece el idioma con el que se genera el reporte
	 * 
	 * @param companyId Identificador de la compañía
	 */
	public void setLanguage(Long companyId) {
		this.language = ELanguageCodes.es.toString();
		Company company = this.companyService.getCompanyById(companyId);
		if (company.getLanguageCode() != null) {
			this.language = company.getLanguageCode();
		}

	}
}
