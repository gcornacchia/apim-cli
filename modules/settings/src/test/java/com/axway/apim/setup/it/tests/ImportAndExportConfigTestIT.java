package com.axway.apim.setup.it.tests;

import java.io.File;

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.setup.it.ExportManagerConfigTestAction;
import com.axway.apim.setup.it.ImportManagerConfigTestAction;
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Test
public class ImportAndExportConfigTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private static String PACKAGE = "/com/axway/apim/setup/it/tests/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void runConfigImportAndExport(@Optional @CitrusResource TestContext context) throws Exception {
		description("Export/Import configuration from and into the API-Manager");
		ImportManagerConfigTestAction configImport = new ImportManagerConfigTestAction(context);
		ExportManagerConfigTestAction configExport = new ExportManagerConfigTestAction(context);
		
		echo("####### Export the configuration #######");
		createVariable(PARAM_EXPECTED_RC, "0");
		createVariable(PARAM_TARGET, configExport.getTestDirectory().getPath());
		createVariable(PARAM_OUTPUT_FORMAT, "json");
		configExport.doExecute(context);
		
		String exportedConfig = (String)configExport.getLastResult().getExportedFiles().get(0);
		
		JsonNode config = mapper.readTree(new File(exportedConfig));
		
		Assert.assertEquals(config.get("config").get("registrationEnabled").asBoolean(), true);
		Assert.assertEquals(config.get("config").get("apiDefaultVirtualHost").asText(), "");
		Assert.assertTrue(config.get("config").get("apiRoutingKeyLocation").isEmpty());
		
		echo("####### Re-Import unchanged exported configuration: "+exportedConfig+" #######");
		createVariable(PARAM_CONFIGFILE, exportedConfig);
		createVariable(PARAM_EXPECTED_RC, "0");
		configImport.doExecute(context);
	}
	
	@CitrusTest
	@Test @Parameters("context")
	public void runUpdateConfiguration(@Optional @CitrusResource TestContext context) throws AppException {
		description("Update API-Configuration with custom config file");
		ImportManagerConfigTestAction configImport = new ImportManagerConfigTestAction(context);

		echo("####### Import configuration #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "apimanager-config.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		createVariable("portalName", "MY API-MANAGER NAME");
		configImport.doExecute(context);
		
		echo("####### Validate configuration has been applied #######");
		http(builder -> builder.client("apiManager").send().get("/config").header("Content-Type", "application/json"));
		if(APIManagerAdapter.hasAPIManagerVersion("7.7.20200530")) {
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
					.validate("$.portalName", "${portalName}")
					.validate("$.apiImportEditable", "true")
					);
		} else {
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
					.validate("$.portalName", "${portalName}"));
		}

		
		echo("####### Import configuration #######");
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "apimanager-config.json");
		createVariable(PARAM_EXPECTED_RC, "17");
		createVariable(PARAM_IGNORE_ADMIN_ACC, "true");
		configImport.doExecute(context);
	}
}
