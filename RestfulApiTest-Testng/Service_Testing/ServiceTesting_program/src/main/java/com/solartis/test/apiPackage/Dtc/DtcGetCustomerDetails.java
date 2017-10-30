package com.solartis.test.apiPackage.Dtc;

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

public class DtcGetCustomerDetails  extends BaseClass implements API
{
	public DtcGetCustomerDetails(PropertiesHandle config)
	{
		this.config=config;
		jsonElements = new LinkedHashMap<String, String>();
		
		InputColVerify = new DBColoumnVerify(config.getProperty("InputCondColumn"));
		OutputColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));	
		StatusColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));
	}
	
	@Override
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
			throw new APIException("ERROR ADD HEADER FUNCTION -- DTCGETCUSTOMERDETAILS CLASS", e);
		}
	}
	
	@Override
	public LinkedHashMap<String, String> SendResponseDataToFile(LinkedHashMap<String, String> output) throws APIException
	{
		try
		{
			String StatusCode=(response.read("..RequestStatus").replaceAll("\\[\"", "")).replaceAll("\"\\]", "");
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tableOutputColVerify = OutputColVerify.GetDataObjects(config.getProperty("OutputColQuery"));		
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tableOutputColVerify.entrySet())	
			{
				LinkedHashMap<String, String> rowOutputColVerify = entry.getValue();
				if(OutputColVerify.DbCol(rowOutputColVerify))
				{
					 try
				      {	
							if(StatusCode.equals("SUCCESS"))
							{
								System.out.println(rowOutputColVerify.get(config.getProperty("OutputColumn")));
								String actual = (response.read(rowOutputColVerify.get(config.getProperty("OutputJsonPath"))).replaceAll("\\[\"", "")).replaceAll("\"\\]", "").replaceAll("\\\\","");
								output.put(rowOutputColVerify.get(config.getProperty("OutputColumn")), actual);
								System.out.println(actual);
								output.put("Flag_for_execution", StatusCode);
								
							}
							else
							{
								String MessageCode=(response.read("..messageCode").replaceAll("\\[\"", "")).replaceAll("\"\\]", "");
								String UserMessage=(response.read("..UserMessage").replaceAll("\\[\"", "")).replaceAll("\"\\]", "");
								output.put("Flag_for_execution", "Error response");
								output.put("Message_code", MessageCode);
								output.put("User_message", UserMessage);
								
							}
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
			throw new APIException("ERROR IN SEND RESPONSE TO FILE FUNCTION -- 	DTCGETCUSTOMERDETAILS CLASS", e);
		}
	}
}
