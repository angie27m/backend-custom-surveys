package com.acsendo.api.survey.climate.dto;

import java.util.List;

public class ClimateOptionTypeDTO {
	

	private List<ClimateResponseOptionDTO> optionsClimate;
	
	private List<ClimateResponseOptionDTO> optionsEnps;
	
	private List<ClimateResponseOptionDTO> optionsDemographics;
	
	

	public List<ClimateResponseOptionDTO> getOptionsClimate() {
		return optionsClimate;
	}
	
	public void setOptionsClimate(List<ClimateResponseOptionDTO> optionsClimate) {
		this.optionsClimate = optionsClimate;
	}
	
	public List<ClimateResponseOptionDTO> getOptionsEnps() {
		return optionsEnps;
	}
	
	public void setOptionsEnps(List<ClimateResponseOptionDTO> optionsEnps) {
		this.optionsEnps = optionsEnps;
	}
	
	public List<ClimateResponseOptionDTO> getOptionsDemographics() {
		return optionsDemographics;
	}
	
	public void setOptionsDemographics(List<ClimateResponseOptionDTO> optionsDemographics) {
		this.optionsDemographics = optionsDemographics;
	}
	
	
	

}
