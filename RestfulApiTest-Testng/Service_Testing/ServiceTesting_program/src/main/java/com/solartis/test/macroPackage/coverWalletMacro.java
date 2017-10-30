package com.solartis.test.macroPackage;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.solartis.test.Configuration.PropertiesHandle;
import com.solartis.test.exception.DatabaseException;
import com.solartis.test.exception.MacroException;
import com.solartis.test.exception.POIException;
import com.solartis.test.util.api.DBColoumnVerify;
import com.solartis.test.util.common.DatabaseOperation;
import com.solartis.test.util.common.ExcelOperationsPOI;

public class coverWalletMacro extends DBColoumnVerify implements MacroInterface
{	
	protected ExcelOperationsPOI sampleexcel=null;
	protected String Targetpath;
	protected coverWalletMacro trans;
	protected String Samplepath;
	protected DatabaseOperation configTable = null;
	protected PropertiesHandle configFile;
	protected DBColoumnVerify MacroCondVerify;
	
	
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public enum Alphabet 
	{
	    A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,AA,AB,AC,AD,AE,AF,AG,AH,AI,AJ,AK,AL,AM,AN,AO,AP,AQ,AR,AS,AT,AU,AV,AW,AX,AY,AZ;

	    public static int getNum(String targ) 
	    {
	        return valueOf(targ).ordinal();
	    }

	    public static int getNum(char targ)
	    {
	        return valueOf(String.valueOf(targ)).ordinal();
	    }
	}
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	public coverWalletMacro(PropertiesHandle configFile) throws MacroException
	{
		super(" ");
			configTable = new DatabaseOperation();
			//configFile = new PropertiesHandle("A:/1 Projects/18 CoverWallet/Rating/configuration_file/config_rating.properties");
		try
		{
			configTable.GetDataObjects(configFile.getProperty("config_query"));
		}
		catch (DatabaseException e) 
		{
			throw new MacroException("ERROR OCCURS INITILIZE THE OBJECT OF  COVERWALLET MACOR", e);
		}
		
	}
	public void LoadSampleRatingmodel(PropertiesHandle configFile,LinkedHashMap<String, String> inputData) throws MacroException
	{
		try
		{
			String RateingModelName ="coverWallet RatingModel_updated_06_17_2017";
			
			Samplepath= configFile.getProperty("Samplepath")+RateingModelName+".xls";
			sampleexcel= new ExcelOperationsPOI(Samplepath);
		}
		catch (POIException e)
		{
			throw new MacroException("ERROR OCCURS WHILE LOADING SAMPLE RATING MODEL OF COVERWALLET MACRO", e);
		}
	}
	
	public void GenerateExpected(LinkedHashMap<String, String> inputData,PropertiesHandle configFile) throws MacroException
	{
		try
		{
			Targetpath =  configFile.getProperty("TargetPath")+inputData.get("testdata")+".xls";
			sampleexcel.Copy(Samplepath, Targetpath);
			sampleexcel.save();
			System.out.println("generate expected rating over");
		}
		catch(POIException e)
		{
			throw new MacroException("ERROR OCCURS WHILE GENERATING THE EXPECTED RATING MODEL OF COVERWALLET MACRO", e);
		}
	}
	
	public void PumpinData(LinkedHashMap<String, String> inputData,PropertiesHandle configFile) throws MacroException
	{
		try
		{
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tablePumpinData = configTable.GetDataObjects(configFile.getProperty("config_query"));
			ExcelOperationsPOI excel=new ExcelOperationsPOI(Targetpath);
			trans= new coverWalletMacro(configFile);
			MacroCondVerify = new DBColoumnVerify("conditionChecking");
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tablePumpinData.entrySet())	
			{	
				LinkedHashMap<String, String> rowtablePumpinData = entry.getValue();
				String condition = rowtablePumpinData.get("Condition");
				if (rowtablePumpinData.get("flag_for_execution").equalsIgnoreCase("Y") && ConditionReading(condition,rowtablePumpinData))
				{
					if (rowtablePumpinData.get("Type").equals("input"))
					{
						String Datacolumntowrite = rowtablePumpinData.get("Input_DB_column");
						String CellAddress = rowtablePumpinData.get("Cell_Address");
						
						String  Datatowrite = inputData.get(Datacolumntowrite);
						String[] part = CellAddress.split("(?<=\\D)(?=\\d)");
						int columnNum=Alphabet.getNum(part[0].toUpperCase());
						int rowNum = Integer.parseInt(part[1]);
						//System.out.println(columnNum+"----"+rowNum+"-----"+rowtablePumpinData.get("Sheet_Name")+"-----"+Datatowrite);
						excel.getsheets(rowtablePumpinData.get("Sheet_Name"));
						excel.getcell(rowNum, columnNum);
						
						if(rowtablePumpinData.get("Translation_Flag").equals("Y"))
						{
							excel.write_data(rowNum-1, columnNum, trans.Translation1(Datatowrite, rowtablePumpinData, configFile));
						}
						else
						{
							if(trans.isInteger(Datatowrite))
							{
								int intdata =Integer.parseInt(Datatowrite);
								excel.write_data(rowNum-1, columnNum, intdata);	
							}
							else if(trans.isFloat(Datatowrite))
							{
								float floatdata = Float.valueOf(Datatowrite);							
								DecimalFormat df = new DecimalFormat("#.####");
								String flo = df.format(floatdata);		
								float floatvalue = Float.valueOf(flo);
								excel.write_data(rowNum-1, columnNum, floatvalue);
							}
							else
							{
								excel.write_data(rowNum-1, columnNum, Datatowrite);
							}
						}
					}
				}
			}
			excel.refresh();
			excel.save();	
		}
		catch(DatabaseException e)
		{
			throw new MacroException("ERROR OCCURS WHILE PUMP-IN THE DATA TO RATING MODEL OF COVERWALLET MACRO", e);
		}
		catch(POIException e)
		{
			throw new MacroException("ERROR OCCURS WHILE OPENING AND CLOSING THE RATING MODEL OF COVERWALLET MACRO", e);
		} 
	}
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public void PumpoutData(LinkedHashMap<String, String> outputData,LinkedHashMap<String, String> inputData,PropertiesHandle configFile) throws MacroException
	{
		try
		{
			ExcelOperationsPOI excel=new ExcelOperationsPOI(Targetpath);
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tablePumpoutData = configTable.GetDataObjects(configFile.getProperty("config_query"));
			excel.refresh();
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tablePumpoutData.entrySet())	
			{
				LinkedHashMap<String, String> rowPumpoutData = entry.getValue();
				String condition = rowPumpoutData.get("Condition");
				if (rowPumpoutData.get("flag_for_execution").equals("Y")&&ConditionReading(condition,inputData))
				{
					if (rowPumpoutData.get("Type").equals("output"))
					{
						String Datacolumntowrite = rowPumpoutData.get("Input_DB_column");
						String CellAddress = rowPumpoutData.get("Cell_Address");
						String[] part = CellAddress.split("(?<=\\D)(?=\\d)");
						int columnNum=Alphabet.getNum(part[0].toUpperCase());
						int rowNum = Integer.parseInt(part[1]);
						excel.getsheets(rowPumpoutData.get("Sheet_Name"));
						excel.getcell(rowNum-1, columnNum);
						String Datatowrite = excel.read_data(rowNum-1, columnNum);
						System.out.println(Datacolumntowrite+"----------" +Datatowrite+"--------"+rowNum+"-------"+columnNum);
						outputData.put(Datacolumntowrite, Datatowrite);
						//outputData.WriteData(Datacolumntowrite, "poda");
					}
				}
				//vickyoutputData.UpdateRow();
			}
			excel.save();
		}
		catch(DatabaseException e)
		{
			throw new MacroException("ERROR OCCURS WHILE PUMPOUT THE OUTPUT FROM RATING MODEL OF COVERWALLET MACRO", e);
		}
		catch (POIException e)
		{
			throw new MacroException("ERROR OCCURS 	WHILE OPENING/CLOSING THE RATING MODEL OF COVERWALLET MACRO", e);
		}
	
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T Translation1(String Datatowrite, LinkedHashMap<String, String> configTable,  PropertiesHandle configFile) throws MacroException
	{
		T outputdata = null;
		switch(configTable.get("Translation_Function"))
		{
		case "Date": 
			Date DateData = Date(Datatowrite,"yyyy-mm-dd",configTable.get("Translation_Format"));
			outputdata = (T) DateData;
			break;
		case "Lookup":
			String LookupData = Lookup(Datatowrite, configFile);
			outputdata = (T) LookupData;
			break;
		case "String":
			String Stringdata = IntegertoString(Datatowrite);
			outputdata = (T) Stringdata;
			break;		
		case "percentage":
			float percentagedata = percentage(Datatowrite);
			Float percent = new Float(percentagedata);
			outputdata = (T) percent;
		}
		return outputdata;
		
	}
	
	
	protected  Date Date(String Date,String InputFormat,String ExpectedFormat) throws MacroException
	{
		String value ="";
		Date Date1=null;
		try 
		{
			Pattern p = Pattern.compile("[^A-Za-z0-9 ]", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(InputFormat);
			String InputDelimiter="";
			if(m.find())
			{
				InputDelimiter=String.valueOf(InputFormat.charAt(m.start()));
			}
			
			Matcher m1 = p.matcher(ExpectedFormat);
			String ExpectedDelimiter="";
			if(m1.find())
			{
				ExpectedDelimiter=String.valueOf(ExpectedFormat.charAt(m1.start()));
			}
			
			String[] DateInputFormat = InputFormat.split(InputDelimiter);
			String[] DateOutputFormat = ExpectedFormat.split(ExpectedDelimiter);
			String[] date = Date.split(InputDelimiter); //yyyy-mm-dd
			
			HashMap<String,String> DateMaping = new HashMap<String,String>();
			DateMaping.put(DateInputFormat[0].toLowerCase(), date[0]);
			DateMaping.put(DateInputFormat[1].toLowerCase(), date[1]);
			DateMaping.put(DateInputFormat[2].toLowerCase(), date[2]);
			value =  DateMaping.get(DateOutputFormat[0].toLowerCase())+ExpectedDelimiter+DateMaping.get(DateOutputFormat[1].toLowerCase())+ExpectedDelimiter+DateMaping.get(DateOutputFormat[2].toLowerCase());     
			DateFormat format = new SimpleDateFormat(ExpectedFormat, Locale.ENGLISH);
			Date1=format.parse(value);  			
		   // System.out.println(value+"\t"+Date1);  						
		}
		
		catch (NumberFormatException | ParseException e) 
		{
			throw new MacroException("ERROR OCCURS 	IN DATE FORMAT OF COVERWALLET MACRO", e);
		}
		return Date1;
		
	}
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	protected String  Lookup(String Lookup1, PropertiesHandle configFile) throws MacroException
	{
		DatabaseOperation Lookup = new DatabaseOperation();
				
		HashMap<String,String> LookupMap = new HashMap<String,String>();
		try 
		{
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tableLookup = Lookup.GetDataObjects(configFile.getProperty("lookup_query"));
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tableLookup.entrySet())	
			{
				LinkedHashMap<String, String> rowLookup = entry.getValue();
				LookupMap.put(rowLookup.get("LookupData"), rowLookup.get("LookupValue"));
				
			}
		} 
		catch (DatabaseException e) 
		{
			throw new MacroException("ERROR OCCURS 	IN LOOKUP TABLE OF ISO MACRO", e);
		}
		System.out.println(LookupMap.get("new"));
		if (LookupMap.get(Lookup1)==null)
		{
			return "Other";
		}
		else
		{
			return LookupMap.get(Lookup1);
		}
	}
	
	
	protected float percentage(String s)
	{
		float value = Float.valueOf(s)/100;
		
		DecimalFormat df = new DecimalFormat("#.####");
		String flo = df.format(value);		
		float percentagevalue = Float.valueOf(flo);
		return percentagevalue;
		
	}

	
	protected String IntegertoString (String s)
	{
		return s;
		
	}
	
	
	protected boolean isInteger(String s) 
	{
	    try 
	    { 
	    	
	        Integer.parseInt(s); 
	    } 
	    catch(NumberFormatException e) 
	    { 
	        return false; 
	    } 
	    catch(NullPointerException e) 
	    {
	        return false;
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
	protected boolean isFloat(String s)
	{
		try
		{
			Float.parseFloat(s);
		}
		catch(NumberFormatException e) 
	    { 
	        return false; 
	    } 
	    catch(NullPointerException e) 
	    {
	        return false;
	    }
		 return true;
	}
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	
}
