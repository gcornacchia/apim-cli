package com.axway.apim.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.api.export.lib.APIChangeParams;
import com.axway.apim.api.export.lib.APIExportCLIOptions;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.api.export.lib.ChangeAPICLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;

public class APIExportCLIOptionsTest {
	@Test
	public void testAPIExportParams() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-n", "*MyAPIName*", "-id", "412378923", "-policy", "*PolicyName*", "-vhost", "custom.host.com", "-state", "approved", "-backend", "backend.customer.com", "-tag", "*myTag*", "-t", "myTarget", "-o", "csv", "-useFEAPIDefinition", "-wide", "-deleteTarget"};
		APIExportCLIOptions options = new APIExportCLIOptions(args);
		APIExportParams params = options.getAPIExportParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		
		Assert.assertEquals(params.getWide(), Wide.wide);
		Assert.assertTrue(params.isDeleteTarget());
		Assert.assertEquals(params.getTarget(), "myTarget");
		Assert.assertEquals(params.getTag(), "*myTag*");
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.csv);
		
		Assert.assertTrue(params.isUseFEAPIDefinition());
		Assert.assertEquals(params.getApiPath(), "/api/v1/greet");
		Assert.assertEquals(params.getName(), "*MyAPIName*");
		Assert.assertEquals(params.getId(), "412378923");
		Assert.assertEquals(params.getPolicy(), "*PolicyName*");
		Assert.assertEquals(params.getVhost(), "custom.host.com");
		Assert.assertEquals(params.getState(), "approved");
		Assert.assertEquals(params.getBackend(), "backend.customer.com");
	}
	
	@Test
	public void testUltra() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-ultra"};
		APIExportCLIOptions options = new APIExportCLIOptions(args);
		APIExportParams params = options.getAPIExportParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		
		Assert.assertEquals(params.getWide(), Wide.ultra);
		// Validate target is current directory if not given
		Assert.assertEquals(params.getTarget(), ".");
	}
	
	@Test
	public void testChangeAPIParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-newBackend", "http://my.new.backend", "-oldBackend", "http://my.old.backend"};
		ChangeAPICLIOptions options = new ChangeAPICLIOptions(args);
		APIChangeParams params = options.getAPIChangeParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		
		// Validate wide is is using standard as default
		Assert.assertEquals(params.getWide(), Wide.standard);
		// Validate the output-format is Console as the default
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.console);
		
		// Validate an API-Export parameter is include
		Assert.assertEquals(params.getApiPath(), "/api/v1/greet");
		
		// Validate the change parameters are included
		Assert.assertEquals(params.getNewBackend(), "http://my.new.backend");
		Assert.assertEquals(params.getOldBackend(), "http://my.old.backend");
	}
}
