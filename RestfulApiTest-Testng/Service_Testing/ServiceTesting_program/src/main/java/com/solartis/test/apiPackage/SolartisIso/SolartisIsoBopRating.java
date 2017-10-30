package com.solartis.test.apiPackage.SolartisIso;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import com.solartis.test.Configuration.PropertiesHandle;
import com.solartis.test.apiPackage.API;
import com.solartis.test.apiPackage.BaseClass;
import com.solartis.test.util.api.DBColoumnVerify;

public class SolartisIsoBopRating extends BaseClass implements API 
{
	public SolartisIsoBopRating(PropertiesHandle config) throws SQLException
	{
		this.config = config;
		jsonElements = new LinkedHashMap<String, String>();

		InputColVerify = new DBColoumnVerify(config.getProperty("InputCondColumn"));
		OutputColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));	
		StatusColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));
	}
}