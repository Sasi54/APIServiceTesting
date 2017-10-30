package com.solartis.test.apiPackage.Dtc;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import com.jayway.jsonpath.PathNotFoundException;
import com.solartis.test.Configuration.PropertiesHandle;
import com.solartis.test.apiPackage.API;
import com.solartis.test.apiPackage.BaseClass;
import com.solartis.test.exception.APIException;
import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.HTTPHandleException;
import com.solartis.test.exception.MacroException;
import com.solartis.test.exception.POIException;
import com.solartis.test.exception.RequestFormatException;
import com.solartis.test.macroPackage.DtcMacro;
import com.solartis.test.macroPackage.MacroInterface;
import com.solartis.test.util.api.*;

public class DtcRatingService extends BaseClass implements API 
{
 protected String Planname[] = null;
 protected String Plancode[] = null;
 MacroInterface macro = null;
 public DtcRatingService(PropertiesHandle config) throws SQLException, MacroException
 {
  this.config = config;
  jsonElements = new LinkedHashMap<String, String>();
  
     InputColVerify = new DBColoumnVerify(config.getProperty("InputCondColumn"));
	OutputColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));	
	StatusColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));
	if(config.getProperty("status").equals("Y"))
	{
	macro=new DtcMacro(config);	
	}
 }
 
 @Override
 public void LoadSampleRequest(LinkedHashMap<String, String> InputData) throws  APIException
 {
	try
	{
	 	  this.input = InputData;
		  input = InputData;
		  
		  Planname=InputData.get("Plan_name").split(";");
		  Plancode=InputData.get("Plan_code").split(";");
		  int numofplan = Planname.length;
		  switch(numofplan)
		   
		   {
		    case 1:   sampleInput = new JsonHandle(config.getProperty("sample_request")+"request1.json"); break;
		    case 2:   sampleInput = new JsonHandle(config.getProperty("sample_request")+"request2.json"); break;
		    case 3:   sampleInput = new JsonHandle(config.getProperty("sample_request")+"request3.json"); break;
		    case 4:   sampleInput = new JsonHandle(config.getProperty("sample_request")+"request4.json"); break;
		    case 5:   sampleInput = new JsonHandle(config.getProperty("sample_request")+"request5.json"); break;
		    case 6:   sampleInput = new JsonHandle(config.getProperty("sample_request")+"request6.json"); break;
		    case 7:   sampleInput = new JsonHandle(config.getProperty("sample_request")+"request7.json"); break;
		   
		    default:
		   }
		  if(config.getProperty("status").equals("Y"))
			{
				  macro.LoadSampleRatingmodel(config, InputData);  //Load sample rating model 
				  macro.GenerateExpected(InputData, config);        //Generate expected rating models  
			}
	}
	catch(MacroException e)
	{
		 throw new APIException("ERROR LoadSampleRequest FUNCTION -- DTC-RatingService CLASS", e);
	}
 }

    @Override
 public void PumpDataToRequest() throws APIException
 {
	  try
	  {
		 LinkedHashMap<Integer, LinkedHashMap<String, String>> tableInputColVerify = InputColVerify.GetDataObjects(config.getProperty("InputColQuery"));
		 request = new JsonHandle(config.getProperty("request_location")+input.get("testdata")+"_request_"+input.get("State_code")+"_"+input.get("Plan_name")+".json");
		 request.StringToFile(sampleInput.FileToString());
	  
		 for (Entry<Integer, LinkedHashMap<String, String>> entry : tableInputColVerify.entrySet())	
			{
				LinkedHashMap<String, String> rowInputColVerify = entry.getValue();
				if(InputColVerify.DbCol(rowInputColVerify)&& (rowInputColVerify.get("Flag").equalsIgnoreCase("Y")))
				{
				  if(!input.get(rowInputColVerify.get(config.getProperty("InputColumn"))).equals(""))
				  {
					if(rowInputColVerify.get(config.getProperty("InputColumn")).equals("Plan_name"))
				    {
				     for(int j=0;j<Planname.length;j++)
				     {
				      String DynamicPlannameJson = rowInputColVerify.get(config.getProperty("InputJsonPath"));
				      String SplitPlanJson[] = DynamicPlannameJson.split("##");
				      request.write(SplitPlanJson[0]+j+SplitPlanJson[1], Planname[j]);
				     }
				    }
				    else if(rowInputColVerify.get(config.getProperty("InputColumn")).equals("Plan_code"))
				    {
				    	System.out.println(Plancode.length);
				        for(int j=0;j<Plancode.length;j++)
				        {
				         String DynamicPlanCodeJson = rowInputColVerify.get(config.getProperty("InputJsonPath"));
				         String SplitCodeJson[] = DynamicPlanCodeJson.split("##"); 
				         System.out.println(SplitCodeJson[0]+j+SplitCodeJson[1]);
				         request.write(SplitCodeJson[0]+j+SplitCodeJson[1], Plancode[j]);
				        }
				     }
				    else
				    {
				     request.write(rowInputColVerify.get(config.getProperty("InputJsonPath")), input.get(rowInputColVerify.get(config.getProperty("InputColumn"))));
				    }
				   }
				  }
				}
			if(config.getProperty("status").equals("Y"))
			{
				macro.PumpinData(input, config);    //Data feed into Sample Rating model
			}
	
	  }
	  catch(DatabaseException | RequestFormatException | POIException | MacroException e)
	  {
		  throw new APIException("ERROR PumpDataToRequest FUNCTION -- DTC-RatingService CLASS", e);
	  }
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
		  throw new APIException("ERROR ADD HEADER FUNCTION -- DTC-RatingService CLASS", e);
	  }
 }

 @Override
 public void SendAndReceiveData() throws APIException
 {
  String input_data= null;
  
  try
  {
    input_data = request.FileToString();
    http.SendData(input_data);  
    String response_string = null; 
    response_string = http.ReceiveData();  
    response = new JsonHandle(config.getProperty("response_location")+input.get("testdata")+"_response_"+input.get("State_code")+"_"+input.get("Plan_name")+".json");  
    response.StringToFile(response_string);
  }
  catch(HTTPHandleException | RequestFormatException e)
  {
	  throw new APIException("ERROR SendAndReceiveData FUNCTION -- DTC-RatingService CLASS", e);
  }
   
  
 }

 @Override
 public LinkedHashMap<String, String> SendResponseDataToFile(LinkedHashMap<String, String> output)   throws APIException
 {
	try
	{
		LinkedHashMap<Integer, LinkedHashMap<String, String>> tableOutputColVerify = OutputColVerify.GetDataObjects(config.getProperty("OutputColQuery"));		
		for (Entry<Integer, LinkedHashMap<String, String>> entry : tableOutputColVerify.entrySet())	
		{
			LinkedHashMap<String, String> rowOutputColVerify = entry.getValue();
			  if(OutputColVerify.DbCol(rowOutputColVerify)&& (rowOutputColVerify.get("Flag").equalsIgnoreCase("Y")))
				{
				try
					{
					System.out.println(rowOutputColVerify.get(config.getProperty("OutputColumn")));
					System.out.println(config.getProperty("OutputColumn")); 
					System.out.println(rowOutputColVerify.get(config.getProperty("OutputColumn")));
					String actual = (response.read(rowOutputColVerify.get(config.getProperty("OutputJsonPath"))).replaceAll("\\[\"", "")).replaceAll("\"\\]", "").replaceAll("\\\\","");
	
					System.out.println(actual);
					output.put(rowOutputColVerify.get(config.getProperty("OutputColumn")), actual);
					System.out.println(actual);
					output.put("flag_for_execution", "Completed");
					}
					catch(PathNotFoundException | RequestFormatException e)
					{
						output.put(rowOutputColVerify.get(config.getProperty("OutputColumn")), "Path not Found");
					}
				}
			}
		
			macro.PumpoutData(output, input, config);   //	data pumped out from expected rating model to db table
	}
	catch(DatabaseException | POIException | MacroException e)
	{
		 throw new APIException("ERROR SendResponseDataToFile FUNCTION -- DTC-RatingService CLASS", e);
	}
	return output;
}
}