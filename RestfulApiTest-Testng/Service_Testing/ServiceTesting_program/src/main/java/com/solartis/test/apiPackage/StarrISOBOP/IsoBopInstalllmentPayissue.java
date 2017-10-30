package com.solartis.test.apiPackage.StarrISOBOP;

import java.sql.SQLException;

import com.solartis.test.Configuration.PropertiesHandle;
import com.solartis.test.apiPackage.API;
import com.solartis.test.apiPackage.BaseClass;
import com.solartis.test.util.api.*;

public class IsoBopInstalllmentPayissue extends BaseClass implements API 
{
	public IsoBopInstalllmentPayissue(PropertiesHandle config) throws SQLException
	{
		this.config = config;

		InputColVerify = new DBColoumnVerify(config.getProperty("InputCondColumn"));
		OutputColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));	
		StatusColVerify = new DBColoumnVerify(config.getProperty("OutputCondColumn"));
		
	}
}