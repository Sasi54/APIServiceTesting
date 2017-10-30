package com.solartis.test.apiPackage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.jayway.jsonpath.PathNotFoundException;
import com.solartis.test.Configuration.PropertiesHandle;
import com.solartis.test.exception.APIException;
import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.HTTPHandleException;
import com.solartis.test.exception.RequestFormatException;
import com.solartis.test.util.api.*;

public class BaseClass 
{
	protected RequestResponse sampleInput = null;
	protected RequestResponse request = null;
	protected RequestResponse response = null;
	protected LinkedHashMap<String, String> XmlElements = null;
	protected LinkedHashMap<String, String> jsonElements = null;
	protected PropertiesHandle config = null;
	protected LinkedHashMap<String, String> input = null;
	protected LinkedHashMap<String, String> output = null;
	protected HttpHandle http = null;
	protected DBColoumnVerify InputColVerify = null;
	protected DBColoumnVerify OutputColVerify = null;
	protected DBColoumnVerify StatusColVerify = null;
	protected ArrayList<String> errorParentname = new ArrayList<String>();
	protected ArrayList<String> errorMessage=new ArrayList<String>();

//---------------------------------------------------------------LOAD SAMPLE REQUEST--------------------------------------------------------------------	
	public void LoadSampleRequest(LinkedHashMap<String, String> InputData) throws APIException
	{
		this.input = InputData;
		sampleInput = new JsonHandle(config.getProperty("sample_request") + "request.json");
	}

//-----------------------------------------------------------PUMPING TEST DATA TO REQUEST--------------------------------------------------------------- 	
	public void PumpDataToRequest() throws APIException 
	{
		try
		{
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tableInputColVerify =  InputColVerify.GetDataObjects(config.getProperty("InputColQuery"));
					
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tableInputColVerify.entrySet())	
			{
				LinkedHashMap<String, String> rowInputColVerify = entry.getValue();
				request = new JsonHandle(config.getProperty("request_location")+input.get("Testdata")+".json");
				request.StringToFile(sampleInput.FileToString());
				if(InputColVerify.DbCol(rowInputColVerify) && (rowInputColVerify.get("Flag").equalsIgnoreCase("Y")))
				{
					if(!input.get(rowInputColVerify.get(config.getProperty("InputColumn"))).equals(""))
					{
	                  	request.write(rowInputColVerify.get(config.getProperty("InputJsonPath")), input.get(rowInputColVerify.get(config.getProperty("InputColumn"))));
					}
				}	
			}
		}
			
		catch(DatabaseException | RequestFormatException  e)
		{
			throw new APIException("ERROR OCCURS IN PUMPDATATOREQUEST FUNCTION -- BASE CLASS", e);
		}
	}

//------------------------------------------------------------CONVERTING REQUEST TO STRING--------------------------------------------------------------	
	public String RequestToString() throws APIException
	{
	  try 
	  {
		  return request.FileToString();
	  } 
	  catch (RequestFormatException e)
	  {
		  throw new APIException("ERROR OCCURS IN REQUEST TO STRING FUNCTION -- BASE CLASS", e);
	   }
	}
	
//-------------------------------------------------------------ADDING HEADER || TOKENS------------------------------------------------------------------	
	public void AddHeaders() throws APIException
	{
		try
		{
			http = new HttpHandle(config.getProperty("test_url"),"POST");
			http.AddHeader("Content-Type", config.getProperty("content_type"));
			http.AddHeader("Token", config.getProperty("token"));
		}
		catch(HTTPHandleException e)
		{
			throw new APIException("ERROR ADD HEADER FUNCTION -- BASE CLASS", e);
		}
	}

//------------------------------------------------------------STORING RESPONSE TO FOLDER----------------------------------------------------------------	
	public void SendAndReceiveData() throws APIException 
	{
		try
		{
			String input_data= null;
			input_data = request.FileToString();
		    http.SendData(input_data);
			String response_string = http.ReceiveData();	
			response = new JsonHandle(config.getProperty("response_location")+input.get("Testdata")+".json");
			response.StringToFile(response_string);
		}
		catch(RequestFormatException | HTTPHandleException e)
		{
			throw new APIException("ERROR IN SEND AND RECIEVE DATA FUNCTION -- BASE CLASS", e);
		}
	}
	
//-------------------------------------------------------------CONVERTING RESPONSE TO STRING------------------------------------------------------------
	public String ResponseToString() throws APIException 
	{
		try
		{
			return response.FileToString();
		}
		catch(RequestFormatException e)
		{
			throw new APIException("ERROR IN RESPONSE TO STRING FUNCTION -- BASE CLASS", e);
		}
	}
	
//-----------------------------------------------------------UPDATING RESPONSE DATA TO DATABASE---------------------------------------------------------	
	public LinkedHashMap<String, String> SendResponseDataToFile(LinkedHashMap<String, String> output) throws APIException
	{
		try
		{
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tableOutputColVerify = OutputColVerify.GetDataObjects(config.getProperty("OutputColQuery"));		
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tableOutputColVerify.entrySet())	
			{
				LinkedHashMap<String, String> rowOutputColVerify = entry.getValue();
				if(OutputColVerify.DbCol(rowOutputColVerify) && (rowOutputColVerify.get("Flag").equalsIgnoreCase("Y")))
				{
				try
				{
					System.out.println("Writing Response to Table");
					System.out.println(rowOutputColVerify.get(config.getProperty("OutputColumn")));
					String actual = (response.read(rowOutputColVerify.get(config.getProperty("OutputJsonPath"))).replaceAll("\\[\"", "")).replaceAll("\"\\]", "").replaceAll("\\\\","");
					output.put(rowOutputColVerify.get(config.getProperty("OutputColumn")), actual);
					System.out.println(actual);
					output.put("flag_for_execution", "Completed");
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
			throw new APIException("ERROR IN SEND RESPONSE TO FILE FUNCTION -- BASE CLASS", e);
		}
	}

//---------------------------------------------------------------COMAPRISION FUNCTION-------------------------------------------------------------------	
	public LinkedHashMap<String, String> CompareFunction(LinkedHashMap<String, String> outputrow) throws APIException
	{		
	    try
	    {
	    	LinkedHashMap<Integer, LinkedHashMap<String, String>> tableStatusColVerify = StatusColVerify.GetDataObjects(config.getProperty("OutputColQuery"));
	    	for (Entry<Integer, LinkedHashMap<String, String>> entry : tableStatusColVerify.entrySet()) 	
			{	
			    LinkedHashMap<String, String> rowStatusColVerify = entry.getValue();	
			    if(StatusColVerify.DbCol(rowStatusColVerify) && (rowStatusColVerify.get("Comaparision_Flag").equalsIgnoreCase("Y")))
				{
					String ExpectedColumn = rowStatusColVerify.get(config.getProperty("ExpectedColumn"));
					String ActualColumn = rowStatusColVerify.get(config.getProperty("OutputColumn"));
					String StatusColumn = rowStatusColVerify.get(config.getProperty("StatusColumn"));
					if(!(StatusColumn.equals("")) && !(ExpectedColumn.equals("")))
					{
						if(premium_comp(outputrow.get(ExpectedColumn),outputrow.get(ActualColumn)))
						{
							outputrow.put(StatusColumn, "Pass");
						}
						else
						{
							outputrow.put(StatusColumn, "Fail");
							//outputrow.UpdateRow();
							analyse(rowStatusColVerify,outputrow);
						}
					}
				}
			}
			 			
			String message = "";
			for(int i=0;i<errorMessage.size();i++)
			{
				message=message+errorMessage.get(i)+"; ";
			}
			outputrow.put("AnalyserResult", message);
			errorMessage.clear();
			errorParentname.clear();
			return outputrow;

	    }	
	    catch(DatabaseException e)
	    {
	    	System.out.println(e);
	    	throw new APIException("ERROR IN DB COMPARISON FUNCTION -- BASE CLASS", e);
	    }
	}
	
//-----------------------------------------------------PRIVATE FUNCTION FOR SUPPORTING COMPARISON FUNCTION---------------------------------------------------	
	protected static boolean premium_comp(String expected,String actual)
	{
		
		boolean status = false;
		if(actual == null||actual.equals(""))
		{
			if((expected == null || expected.equals("")||expected.equals("0") || expected.equals("0.0")))
			{
				status = true;
			}
		}
		if(expected == null||expected.equals(""))
		{
			if(actual == null|| actual.equals("")||actual.equals("0") || actual.equals("0.0"))
			{
				status = true;
			}
		}
		if(actual!=null && expected!=null)
		{
			expected = expected.replaceAll("\\[\"", "");
    		actual = actual.replaceAll("\\[\"", "");	
    		expected = expected.replaceAll("\"\\]", "");
    		actual = actual.replaceAll("\"\\]", "");
    		expected = expected.replaceAll("\\.[0-9]*", "");
    		actual = actual.replaceAll("\\.[0-9]*", "");
    		if(expected.equals(actual))
    		{
    			status = true;
    		}
		}

		return status;	
		
	}

	protected void analyse(LinkedHashMap<String, String> Conditiontablerow,LinkedHashMap<String, String> outputrow ) throws DatabaseException 
	{		
		boolean flag = false;
		
		if(outputrow.get(Conditiontablerow.get("StatusColumn")).equals("Pass"))
		{		

		}

		else if(outputrow.get(Conditiontablerow.get("StatusColumn")).equals("Fail"))
		{	
			String[] Parentname =Conditiontablerow.get("ParentName").split(";");
			int noOfParentname=Parentname.length;
			for(int i=0;i<noOfParentname;i++)
			{								
				if(!this.ifexist(Conditiontablerow.get("NodeName")))
				{
					errorParentname.add(Parentname[i]);
					if(flag == false)
					{
						errorMessage.add(Conditiontablerow.get("Message"));
						flag = true;
					}
				}
			}
						
		}

	}

	protected boolean ifexist (String NodeName)
	{
		boolean exist = false;
		int arraylength =errorParentname.size();
		for(int i = 0; i<arraylength;i++)
		{
			String existParentName =errorParentname.get(i);
			if(existParentName.equals(NodeName))
			{
				exist = true;
				break;
			}
		}
		return exist;	

	}
	
}
