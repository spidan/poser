package de.dfki.asr.poser.Converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONException;
import org.json.JSONObject;
import static org.junit.Assert.*;
import org.junit.Test;

public class RdfToJsonConverterTest {

	@Test
	public void converterShouldReturnSingleObjectForLiteralInputWithNoParentOrChild () throws IOException, JSONException {
		Model input = buildLiteralInput();
		Model jsonModel = buildSimpleJsonModel();
		RdfToJson conv = new RdfToJson();
		String out = conv.buildJsonString(input, jsonModel);
		JSONObject expected = new JSONObject();
		expected.put("value", 126.38);
		assertEquals(expected.toString(), out);
	}

	@Test
	public void converterShouldReturnFittingHierarchyForInputWithNonLiteralParent () throws IOException {
		Model jsonModel = buildFullExampleModelFile();
		Model inputModel = buildInputExample();
		RdfToJson conv = new RdfToJson();
		String out = conv.buildJsonString(inputModel, jsonModel);
		JSONObject expectedNodeObject = new JSONObject();
		expectedNodeObject.put("value", 506.54);
		expectedNodeObject.put("timestamp", "2021-01-10T19:58:49.294909Z");
		JSONObject expectedResult = new JSONObject();
		expectedResult.put("node", expectedNodeObject);
		assertEquals(expectedResult.toString(), out);
	}

	private Model buildLiteralInput() throws IOException {
		String inputString = "<http://sense.mapping.example/measurement/Value2021-01-10T19%3a59%3a24.220966Z> a\n" +
			"    <http://iotschema.org/TestData> ; \n" +
			"  <http://iotschema.org/TestDataType> \"126.38\" .";
		InputStream input = new ByteArrayInputStream(inputString.getBytes());
		Model inputModel = Rio.parse(input, RDFFormat.TRIG);
		return inputModel;
	}

	private Model buildSimpleJsonModel() throws IOException {
		String jsonInputDataType = "@prefix ctd: <http://connectd.api/> .\n" +
			"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
			"@prefix iots: <http://iotschema.org/> .\n" +
			"@prefix json: <http://some.json.ontology/> .\n" +
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
			"\n" +
			"# Which inputs to expect and to start mapping from\n" +
			"json:InputDataType {\n" +
			"	json:EntryPoint a iots:TestData;\n" +
			"		iots:testDataType iots:TestData . \n" +
			"\n" +
			"	iots:TestData iots:TestDataType iots:Number .\n" +
			"}";
		String jsonObjectModel = "json:ApiDescription {\n" +
		"	ctd:TestValue a json:Number ;\n" +
		"		json:key \"value\"^^xsd:string ;\n" +
		"		json:dataType iots:TestData ;\n" +
		"		} \n";
		String jsonModelString = jsonInputDataType.concat(jsonObjectModel);
		InputStream modelStream = new ByteArrayInputStream(jsonModelString.getBytes());
		Model jsonModel = Rio.parse(modelStream, RDFFormat.TRIG);
		return jsonModel;
	}

	private Model buildFullExampleModelFile() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream modelStream = classLoader.getResource("jsonApiSingleValue.ttl").openStream();
		Model jsonModel = Rio.parse(modelStream, RDFFormat.TRIG);
		return jsonModel;
	}

	private Model buildInputExample() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream modelStream = classLoader.getResource("liftedExampleSingleValue.ttl").openStream();
		Model jsonModel = Rio.parse(modelStream, RDFFormat.TRIG);
		return jsonModel;
	}

	private Model buildMultipleInputExample() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream modelStream = classLoader.getResource("liftedExampleMultipleValues.ttl").openStream();
		Model jsonModel = Rio.parse(modelStream, RDFFormat.TRIG);
		return jsonModel;
	}
}
