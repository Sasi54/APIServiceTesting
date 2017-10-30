package com.solartis.test.macroPackage;

import java.util.LinkedHashMap;
import com.solartis.test.Configuration.PropertiesHandle;
import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.MacroException;
import com.solartis.test.exception.POIException;

public interface MacroInterface 
{
	public void LoadSampleRatingmodel(PropertiesHandle configFile,LinkedHashMap<String, String> inputData) throws MacroException;
	public void GenerateExpected(LinkedHashMap<String, String> inputData,PropertiesHandle configFile) throws MacroException;
	public void PumpinData(LinkedHashMap<String, String> inputData,PropertiesHandle configFile) throws DatabaseException, POIException, MacroException;
	public void PumpoutData(LinkedHashMap<String, String> outputData,LinkedHashMap<String, String> inputData,PropertiesHandle configFile) throws POIException, MacroException, DatabaseException;
	
}
