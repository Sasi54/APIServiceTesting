package com.solartis.test.apiPackage.Chic;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import com.jayway.jsonpath.PathNotFoundException;
import com.solartis.test.Configuration.PropertiesHandle;
import com.solartis.test.apiPackage.API;
import com.solartis.test.apiPackage.BaseClass;
import com.solartis.test.exception.APIException;
import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.HTTPHandleException;
import com.solartis.test.exception.RequestFormatException;
import com.solartis.test.util.api.*;

public class ChicRating extends BaseClass implements API
{
	public ChicRating(PropertiesHandle config) throws APIException 
	{
		this.config = config;
		XmlElements = new LinkedHashMap<String, String>();
		//XmlElements.GetDataObjects(config.getProperty("json_query"));
		InputColVerify = new DBColoumnVerify(config.getProperty("InputCondColumn"));
		OutputColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));	
		StatusColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));
	}
	
	@Override
	public void LoadSampleRequest(LinkedHashMap<String, String> InputData)
	{
		this.input = InputData;
		sampleInput = new XmlHandle(config.getProperty("sample_request"));
		
	}
	
	@Override
	public void PumpDataToRequest() throws APIException 
	{
		try
		{
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tableInputColVerify = InputColVerify.GetDataObjects(config.getProperty("InputColQuery"));
			request = new XmlHandle(config.getProperty("request_location")+input.get("testdata")+"_request"+".xml");
			request.StringToFile(sampleInput.FileToString());
			
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tableInputColVerify.entrySet())	
			{
				LinkedHashMap<String, String> rowInputColVerify = entry.getValue();
				if(InputColVerify.DbCol(rowInputColVerify))
				{
					if(!input.get(rowInputColVerify.get(config.getProperty("InputColumn"))).equals(""))
					{
						request.write(jsonElements.get(rowInputColVerify.get(config.getProperty("InputColumn"))), input.get(rowInputColVerify.get(config.getProperty("InputColumn"))));
					}
				}	
			}
		}
		catch(DatabaseException | RequestFormatException e)
		{
			throw new APIException("ERROR OCCURS IN PUMPDATATOREQUEST FUNCTION -- CHIC FORM CLASS", e);
		}
	}

	@Override
	public void SendAndReceiveData() throws APIException
	{
		try
		{
			String input_data= null;
		    input_data = request.FileToString();
			http.SendData(input_data);	
			String response_string = null;
			response_string = http.ReceiveData();
			response = new XmlHandle(config.getProperty("response_location")+input.get("testdata")+"_response"+".xml");
			response.StringToFile(response_string);
		}
		catch(RequestFormatException | HTTPHandleException e)
		{
			throw new APIException("ERROR IN SEND AND RECIEVE DATA FUNCTION -- CHIC RATING CLASS", e);
		}
	}

	@Override
	public LinkedHashMap<String, String> SendResponseDataToFile(LinkedHashMap<String, String> output) throws APIException
	{
		try
		{
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tableOutputColVerify = OutputColVerify.GetDataObjects(config.getProperty("OutputColQuery"));		
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tableOutputColVerify.entrySet())	
			{
				LinkedHashMap<String, String> rowOutputColVerify = entry.getValue();
			if(OutputColVerify.DbCol(rowOutputColVerify))
			{
				 try
			      {	
				
				String actual = (response.read(XmlElements.get(rowOutputColVerify.get(config.getProperty("OutputColumn")))).replaceAll("\\[\"", "")).replaceAll("\"\\]", "");
				output.put(rowOutputColVerify.get(config.getProperty("OutputColumn")), actual);
			      }
					catch(PathNotFoundException e)
					{
						output.put(rowOutputColVerify.get(config.getProperty("OutputColumn")), "Path not Found");
					}
					}
				}
	
				return output;	
		}
		catch(DatabaseException | RequestFormatException e)
		{
			throw new APIException("ERROR IN SEND RESPONSE TO FILE FUNCTION -- CHIC RATING CLASS", e);
		}
		
	}
}
