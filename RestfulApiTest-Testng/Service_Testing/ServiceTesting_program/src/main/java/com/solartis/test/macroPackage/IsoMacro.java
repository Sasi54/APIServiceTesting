package com.solartis.test.macroPackage;

import java.text.DateFormat;
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
import com.solartis.test.util.common.DatabaseOperation;
import com.solartis.test.util.common.ExcelOperationsPOI;

public class IsoMacro implements MacroInterface
{
	protected ExcelOperationsPOI sampleexcel=null;
	protected String Targetpath;
	protected IsoMacro trans;
	protected String Samplepath;
	protected DatabaseOperation configTable = null;
	protected PropertiesHandle configFile;
	
	
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
	public IsoMacro(PropertiesHandle configFile) throws MacroException
	{
		configTable = new DatabaseOperation();
		try 
		{
			configTable.GetDataObjects(configFile.getProperty("config_query"));
		}
		catch (DatabaseException e) 
		{
			throw new MacroException("ERROR OCCURS INITILIZE THE OBJECT OF ISOMACRO", e);
		}
		
	}
	
	public void LoadSampleRatingmodel(PropertiesHandle configFile,LinkedHashMap<String, String> inputData) throws MacroException
	{
		try
		{
			String RateingModelName = Lookup(inputData.get("RatingModel_version"),configFile);
			
			Samplepath= configFile.getProperty("Samplepath")+RateingModelName+".xls";
			sampleexcel= new ExcelOperationsPOI(Samplepath);
		}
		catch (POIException e)
		{
			throw new MacroException("ERROR OCCURS WHILE LOADING SAMPLE RATING MODEL", e);
		}
	}
	
	public void GenerateExpected(LinkedHashMap<String, String> inputData,PropertiesHandle configFile) throws MacroException
	{
		try
		{
			Targetpath =  configFile.getProperty("TargetPath")+inputData.get("testdata")+".xls";
			sampleexcel.Copy(Samplepath, Targetpath);
			sampleexcel.save();
		}
		catch(POIException e)
		{
			throw new MacroException("ERROR OCCURS WHILE GENERATING THE EXPECTED RATING MODEL", e);
		}
	}
	
	public void PumpinData(LinkedHashMap<String, String> inputData,PropertiesHandle configFile) throws MacroException
	{
		try
		{
			LinkedHashMap<Integer, LinkedHashMap<String, String>> tablePumpinData = configTable.GetDataObjects(configFile.getProperty("config_query"));
			ExcelOperationsPOI excel=new ExcelOperationsPOI(Targetpath);
			trans= new IsoMacro(configFile);
			for (Entry<Integer, LinkedHashMap<String, String>> entry : tablePumpinData.entrySet())	
			{		
				LinkedHashMap<String, String> rowPumpinData = entry.getValue();
				if (rowPumpinData.get("flag_for_execution").equalsIgnoreCase("Y"))
				{
					if (rowPumpinData.get("Type").equals("input"))
					{
						String Datacolumntowrite = rowPumpinData.get("Input_DB_column");
						String CellAddress = rowPumpinData.get("Cell_Address");
						
						String  Datatowrite = inputData.get(Datacolumntowrite);
						String[] part = CellAddress.split("(?<=\\D)(?=\\d)");
						int columnNum=Alphabet.getNum(part[0].toUpperCase());
						int rowNum = Integer.parseInt(part[1]);
						excel.getsheets(rowPumpinData.get("Sheet_Name"));
						excel.getcell(rowNum, columnNum);
						
						if(rowPumpinData.get("Translation_Flag").equals("Y"))
						{
							excel.write_data(rowNum-1, columnNum, trans.Translation1(Datatowrite, rowPumpinData, configFile));
						}
						else
						{
							if(trans.isInteger(Datatowrite))
							{
								int datadata =Integer.parseInt(Datatowrite);
								excel.write_data(rowNum-1, columnNum, datadata);	
							}
							else if(trans.isFloat(Datatowrite))
							{
								float floatdata = Float.parseFloat(Datatowrite);
								excel.write_data(rowNum-1, columnNum, floatdata);
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
			throw new MacroException("ERROR OCCURS WHILE PUMP-IN THE DATA TO RATING MODEL OF ISO MACRO", e);
		}
		catch(POIException e)
		{
			throw new MacroException("ERROR OCCURS WHILE OPENING AND CLOSING THE RATING MODEL OF ISO MACRO", e);
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
			if (rowPumpoutData.get("flag_for_execution").equals("Y"))
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
					outputData.put(Datacolumntowrite, Datatowrite);
				}
			}
			//outputData.UpdateRow();
		}
		excel.save();
		}
		catch(DatabaseException e)
		{
			throw new MacroException("ERROR OCCURS WHILE PUMPOUT THE OUTPUT FROM RATING MODEL OF ISO MACRO", e);
		}
		catch (POIException e)
		{
			throw new MacroException("ERROR OCCURS 	WHILE OPENING/CLOSING THE RATING MODEL OF ISO MACRO", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T Translation1(String Datatowrite, LinkedHashMap<String, String> configTable,  PropertiesHandle configFile) throws   MacroException
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
		case "PaddingZeros":
			String PaddingZeros = PaddingZeros(Datatowrite);
			outputdata = (T) PaddingZeros;
			break;
		case "ISOBOPWindhail":
			int ISOBOPWindhail = ISOBOPWindhail(Datatowrite);
			Integer windhail = new Integer(ISOBOPWindhail);
			outputdata =  (T) windhail;
			break;
		}
		return outputdata;
		
	}
	
	
	protected  Date Date(String Date,String InputFormat,String ExpectedFormat) throws MacroException
	{
		String value ="";
		Date Date1=null;
		
		
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
			try 
			{
				Date1=format.parse(value);
			} 
			catch (NumberFormatException | ParseException e) 
			{
				throw new MacroException("ERROR OCCURS 	IN DATE FORMAT OF ISO MACRO", e);
			}  			
		return Date1;
		
	}
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	protected String  Lookup(String Lookup1, PropertiesHandle configFile) throws  MacroException
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
		
		if (LookupMap.get(Lookup1)==null)
		{
			return "Other";
		}
		else
		{
			return LookupMap.get(Lookup1);
		}
	}
	
	protected String PaddingZeros(String Data)
	{
		String s = String.format("%%0%dd", 3);
		String f =String.format(s, Integer.valueOf(Data));
		return f;
		
	}
	
	
	protected int ISOBOPWindhail(String Data)
	{
		int percentageData=0;
		if(Data.equals("Not Applicable"))
		{
			percentageData = 0;
		}
		else
		{
			String[] windhail = Data.split("%");
			percentageData = Integer.valueOf(windhail[0]);			
			return percentageData;
		}
		return percentageData;		
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
