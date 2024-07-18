package com.acsendo.api.survey.climate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.acsendo.api.climate.dto.ClimateEvaluationTemplateContentDTO;
import com.acsendo.api.survey.climate.service.ClimateTemplateService;
import com.amazonaws.xray.spring.aop.XRayEnabled;

import io.swagger.annotations.Api;

@RestController
@XRayEnabled
@RequestMapping(ClimateTemplateController.MAIN_PATH)
@Api(value = "Creacion de evaluacion de clima a partir de excel")
public class ClimateTemplateController {

	public static final String MAIN_PATH = "/survey/climate/{companyId}/template";
	public static final String DOWNLOAD_TEMPLATE = "/download/{companyLanguage}";
	public static final String UPLOAD_TEMPLATE = "/upload/{companyLanguage}";

	@Autowired
	private ClimateTemplateService climateTemplateSvc;

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(DOWNLOAD_TEMPLATE)
	public ResponseEntity<byte[]> getChargeExcelTemplate(@PathVariable Long companyId,
			@PathVariable String companyLanguage) {
		try {
			byte[] excelFile = climateTemplateSvc.getExcelTemplate(companyLanguage);
			return new ResponseEntity<byte[]>(excelFile, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping(value = UPLOAD_TEMPLATE, method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ClimateEvaluationTemplateContentDTO uploadClimateTemplate(@RequestPart("file") MultipartFile file, @PathVariable Long companyId,
			@PathVariable String companyLanguage) {
		try {
			return this.climateTemplateSvc.createClimateEvaluationFromTemplate(file, companyId,companyLanguage);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
	}
}
