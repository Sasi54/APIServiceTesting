package com.solartis.test.apiPackage.StarrISOBOP;

import java.util.LinkedHashMap;
import com.solartis.test.Configuration.PropertiesHandle;
import com.solartis.test.apiPackage.API;
import com.solartis.test.apiPackage.BaseClass;
import com.solartis.test.exception.APIException;
import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.HTTPHandleException;
import com.solartis.test.exception.MacroException;
import com.solartis.test.exception.POIException;
import com.solartis.test.macroPackage.IsoMacro;
import com.solartis.test.macroPackage.MacroInterface;
import com.solartis.test.util.api.DBColoumnVerify;
import com.solartis.test.util.api.HttpHandle;

public class IsoBoprating extends BaseClass implements API
{
	MacroInterface macro = null;
	public IsoBoprating(PropertiesHandle config) throws APIException
	{
	    try
	    {
			this.config = config;
			jsonElements = new LinkedHashMap<String, String>();
		
			InputColVerify = new DBColoumnVerify(config.getProperty("InputCondColumn"));
			OutputColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));	
			StatusColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));
			if(config.getProperty("status").equals("Y"))
			{
			macro=new IsoMacro(config);	
			}
	    }
	    catch(MacroException e)
	    {
	    	throw new APIException("ERROR INITATING MACRO- ISO CLASS", e);
	    }
		
	}
	
	public void AddHeaders() throws APIException
	{
		try
		{
			http = new HttpHandle(config.getProperty("test_url"),"POST");
			http.AddHeader("Content-Type", config.getProperty("content_type"));
			http.AddHeader("Token", config.getProperty("token"));
			http.AddHeader("EventName", config.getProperty("EventName"));
		}
    	catch (HTTPHandleException e) 
		{
			throw new APIException("ERROR ADD HEADER FUNCTION -- ISO-RATING CLASS", e);
		}
	}
	
	public void LoadSampleRequest(LinkedHashMap<String, String> InputData) throws APIException
	{
		if(config.getProperty("status").equals("Y"))
		{
			try 
			{
				macro.LoadSampleRatingmodel(config, InputData);		
				macro.GenerateExpected(InputData, config);
			} catch (MacroException e) 
			{
				throw new APIException("ERROR LoadSampleRequest FUNCTION -- ISO-RATING CLASS", e);
			}
		}
		super.LoadSampleRequest(InputData);
	}
	
	public void PumpDataToRequest() throws  APIException
	{			
		if(config.getProperty("status").equals("Y"))
		{
			try 
			{
				macro.PumpinData(input, config);
			} 
			catch (DatabaseException | POIException | MacroException e) 
			{
				throw new APIException("ERROR PumpDataToRequest FUNCTION -- ISO-RATING CLASS", e);
			}
		}
		super.PumpDataToRequest();
	}
	
	public LinkedHashMap<String, String> SendResponseDataToFile(LinkedHashMap<String, String> output) throws APIException
	{
		if(config.getProperty("status").equals("Y"))
		{
			try 
			{
				macro.PumpoutData(output, input, config);
			} 
			catch (POIException | MacroException | DatabaseException e) 
			{
				throw new APIException("ERROR SendResponseDataToFile FUNCTION -- ISO-RATING CLASS", e);
			}
		}
		return super.SendResponseDataToFile(output);
				
	}
	
}