package com.acsendo.api.survey.climate.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
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
import org.springframework.stereotype.Component;

@Component
public class ClimateExcelTemplateHandler {

	private Workbook workBook;
	private String templateName;
	// private String languageCode = ELanguageCodes.es.toString();

	private List<Sheet> sheets = new ArrayList<>();

	byte[] result = null;

	private String[] buildArrayOfHeader(List<String> headers) {
		String[] buildArrayOfHeadLabel = headers.toArray(new String[0]);
		return buildArrayOfHeadLabel;
	}

	public void generateExcelTemplate() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			this.workBook.write(out);
			out.close();
			this.sheets = new ArrayList<>();
			this.result = out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Sheet generateTitleAndDescription(Sheet currentSheet, String desc) {
		Row rowHeader = currentSheet.getRow(0) != null ? currentSheet.getRow(0) : currentSheet.createRow(0);
		String[] headersNames = { this.templateName, desc };
		String descMerge = "B1:" + (this.sheets.isEmpty() ? "G" : "C") + "8";
		String[] cellRangeAddress = { "A1:A8", descMerge };
		int[] range = { 0, 1 };

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {

			Cell cellHeader = rowHeader.createCell(range[i]);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getMainTitleStyle(currentSheet, (short)(i == 0 ? 18 : 11)));
			currentSheet.setColumnWidth(i, 200 * 256);
			currentSheet.addMergedRegion(CellRangeAddress.valueOf(cellRangeAddress[i]));

		}
		return currentSheet;
	}

	private CellStyle getMainTitleStyle(Sheet sheet, short fontHeight) {

		Font headerFont = this.workBook.createFont();
		headerFont.setColor(IndexedColors.GREY_80_PERCENT.index);
		headerFont.setFontHeightInPoints(fontHeight);
		headerFont.setFontName(fontHeight == 11?"Calibri":"Arial Black");

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFont(headerFont);
		cellStyle.setWrapText(true);  
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

		return cellStyle;
	}

	private CellStyle getStyleHeaders(Sheet sheet) {

		Font headerFont = this.workBook.createFont();
		headerFont.setColor(IndexedColors.WHITE.index);
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFont(headerFont);

		cellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.index);
		cellStyle.setFillPattern((short) 1);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		return cellStyle;
	}

	public byte[] generateClimateExcelWorkBookTemplate(String templateName, List<List<String>> sheetNames,
			List<List<String>> workBookHeaders, List<List<String>> workBookHeadersTips) {
		// Crea un libro de excel
		this.workBook = new XSSFWorkbook();
		this.templateName = templateName;
		for (int i = 0; i < sheetNames.size(); i++) {
			List<String> currentHeaders = workBookHeaders.get(i);
			List<String> currentHeadersTips = workBookHeadersTips.get(i);
			this.generateSheet(sheetNames.get(i).get(0), buildArrayOfHeader(currentHeaders),
					buildArrayOfHeader(currentHeadersTips), sheetNames.get(i).get(1));
		}
		this.generateExcelTemplate();
		return this.result;

	}

	private void generateSheet(String titleSheet, String[] headersNames, String[] headersTips, String sheetDesc) {
		Sheet currentSheet = this.workBook.createSheet(titleSheet);
		if (sheetDesc != null && !sheetDesc.isEmpty())
			currentSheet = this.generateTitleAndDescription(currentSheet, sheetDesc);
		currentSheet = this.generateHeaders(currentSheet, headersNames, headersTips);
		sheets.add(currentSheet);

	}

	private Sheet generateHeaders(Sheet currentSheet, String[] headersNames, String[] headersTips) {
		Row rowHeader = currentSheet.createRow((this.sheets.isEmpty() || this.sheets.size() == 2 ? 8 : 0));
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {

			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(currentSheet));
			if (!headersTips[i].isEmpty())
				cellHeader.setCellComment(this.addComment(cellHeader, headersTips[i], i));
			// ancho para la columna actual fijo a 36 puntos
			currentSheet.setColumnWidth(i, 36 * 256);
		}
		return currentSheet;
	}

	private Comment addComment(Cell cellHeader, String commentText, Integer colNumber) {
		Drawing drawing = cellHeader.getSheet().createDrawingPatriarch();
		CreationHelper factory = this.workBook.getCreationHelper();
		ClientAnchor anchor = factory.createClientAnchor();
		anchor.setCol1(colNumber);
		anchor.setCol2(colNumber + 3);
		anchor.setRow1((this.sheets.isEmpty() ? 6 : 0));
		anchor.setRow2((this.sheets.isEmpty() ? 6 : 0) + 4);
		Comment comment = drawing.createCellComment(anchor);
		comment.setString(factory.createRichTextString(commentText));
		return comment;
	}

	public <T extends Enum<T>> byte[] createValidatorsEnum(byte[] workBookTemplate, String nameSheet,
			List<T> enumValidatorKey, List<String> enumValidatorTraslate, int indexColumn) {
		try {
			InputStream inputStream = new ByteArrayInputStream(workBookTemplate);
			this.workBook = new XSSFWorkbook(inputStream);

			Sheet currentSheet = this.workBook.getSheet(nameSheet);
			if (currentSheet == null)
				currentSheet = this.workBook.createSheet(nameSheet);

			// llenado de datos
			for (int i = 0; i < enumValidatorTraslate.size(); i++) {
				Row row = currentSheet.createRow(i);

				Cell cellEnum = row.createCell(0);
				cellEnum.setCellValue(enumValidatorKey.get(i).name());

				Cell cellEnumTraslate = row.createCell(1);
				cellEnumTraslate.setCellValue(enumValidatorTraslate.get(i));
			}
			currentSheet.setColumnWidth(0, 36 * 256);
			nameSheet = "'" + nameSheet + "'!$B$1:$B$" + enumValidatorTraslate.size();
			inputStream.close();
			loadDataValidation(this.workBook, indexColumn, nameSheet);
			this.generateExcelTemplate();
			return this.result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workBookTemplate;
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
		CellRangeAddressList addressList = new CellRangeAddressList(rowIndex, 200, columnIndex, columnIndex);
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

	public byte[] addDefaultValues(byte[] workBookTemplate, String nameSheet, List<String> defaultValues,
			int indexColumn) {
		try {
			InputStream inputStream = new ByteArrayInputStream(workBookTemplate);
			this.workBook = new XSSFWorkbook(inputStream);
			Sheet currentSheet = this.workBook.getSheet(nameSheet);
			if (currentSheet == null)
				return workBookTemplate;
			Row rowDefaultValues = currentSheet.createRow(9);
			for (int i = 0, col = 0, row = 9; i < defaultValues.size(); i++, col++) {
				if (col == indexColumn) {
					row++;
					rowDefaultValues = currentSheet.createRow(row);
					col = 0;
				}
				Cell cellValueDefault = rowDefaultValues.createCell(col);
				String defaultVal = defaultValues.get(i);
				if (StringUtils.isNumeric(defaultVal))
					cellValueDefault.setCellValue(Integer.valueOf(defaultVal));
				else
					cellValueDefault.setCellValue(defaultVal);

			}

			inputStream.close();

			this.generateExcelTemplate();
			return this.result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workBookTemplate;
	}

	public byte[] hideSheet(byte[] workBookTemplate, String nameSheet) {
		try {
			InputStream inputStream = new ByteArrayInputStream(workBookTemplate);
			this.workBook = new XSSFWorkbook(inputStream);
			this.workBook.setSheetHidden(this.workBook.getSheetIndex(nameSheet), HSSFWorkbook.SHEET_STATE_HIDDEN);
			inputStream.close();
			this.generateExcelTemplate();
			return this.result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workBookTemplate;
	}

}
