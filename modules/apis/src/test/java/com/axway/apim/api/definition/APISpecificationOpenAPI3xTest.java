package com.axway.apim.api.definition;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class APISpecificationOpenAPI3xTest {
	
	private static final String testPackage = "/com/axway/apim/api/definition";
	
	ObjectMapper mapper = new ObjectMapper();
	ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
	
	@BeforeClass
	private void initTestIndicator() {
		APIImportParams params = new APIImportParams();
		params.setReplaceHostInSwagger(true);
	}
	
	@Test
	public void backendHostAndBasePath() throws AppException, IOException {

		byte[] content = getSwaggerContent(testPackage + "/openapi30.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).size(), 1, "Expected to get only one server url");
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).get(0).get("url").asText(), "https://myhost.customer.com:8767/api/v1/myAPI");
	}
	
	@Test
	public void testBerlinGroupYAM_API() throws AppException, IOException {
		APIManagerAdapter.apiManagerVersion="7.7.0";
		byte[] content = getSwaggerContent(testPackage + "/psd2-api_1.3.6_errata20200327.yaml");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		JsonNode swagger = ymlMapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).size(), 1, "Expected to get only one server url");
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).get(0).get("url").asText(), "https://myhost.customer.com:8767/api/v1/myAPI");
	}
	
	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
