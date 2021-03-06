package com.axway.apim.test.basic;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.apiimport.APIImportConfigAdapter;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorState;

public class APIImportConfigAdapterTest extends APIManagerMockBase {

	private static Logger LOG = LoggerFactory.getLogger(APIImportConfigAdapterTest.class);
	
	@BeforeClass
	private void initCommandParameters() throws AppException, IOException {
		setupMockData();
	}
	
	@BeforeMethod
	public void cleanSingletons() {
		ErrorState.deleteInstance();
	}
	
	@Test
	public void withoutStage() throws AppException, ParseException {
		try {
			// Create Environment properties without any stage (basically loads env.properties)
			EnvironmentProperties props = new EnvironmentProperties(null);
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomething");
		} catch (Exception e) {
			LOG.error("Error running test: withoutStage", e);
			throw e;
		}
	}
	
	@Test
	public void withStage() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties("variabletest");
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomethingElse");
		} catch (Exception e) {
			LOG.error("Error running test: withStage", e);
			throw e;
		}
	}
	
	@Test
	public void usingOSEnvVariable() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			String osArch = System.getProperty("os.arch");
			Assert.assertEquals(apiConfig.getState(), "notUsed "+osArch);
		} catch (Exception e) {
			LOG.error("Error running test: usingOSEnvVariable", e);
			throw e;
		}
	}
	
	@Test
	public void notDeclaredVariable() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "${notDeclared}");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}
	
	@Test
	public void configFileWithSpaces() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api config with spaces.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "${notDeclared}");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}
	
	@Test
	public void stageConfigInSubDirectory() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, "testStageProd", "notRelavantForThis Test", false);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "9.0.0");
			Assert.assertEquals(apiConfig.getName(), "API Config from testStageProd sub folder");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}
	
	@Test
	public void outboundOAuthValidConfig() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);  
			props.put("myOAuthProfileName", "Sample OAuth Client Profile");
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/outbound-oauth-config.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, "testStageProd", "petstore.json", false);
			adapter.getDesiredAPI();
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "kk1");
			Assert.assertEquals(apiConfig.getName(), "My OAuth API");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Cannot validate/fulfill configuration file.")
	public void outboundOAuthInValidConfig() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			props.put("myOAuthProfileName", "Invalid profile name");
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/outbound-oauth-config.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "petstore.json", false);
			adapter.getDesiredAPI();
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "kk1");
			Assert.assertEquals(apiConfig.getName(), "My OAuth API");
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Test
	public void emptyVHostTest() throws AppException, ParseException {
		try {
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/empty-vhost-api-config.json").getFile();
			
			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "petstore.json", false);
			adapter.getDesiredAPI();
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertNull(apiConfig.getVhost(), "Empty VHost should be considered as not set (null), as an empty VHost is logically not possible to have.");
		} catch (Exception e) {
			throw e;
		}
	}
}
