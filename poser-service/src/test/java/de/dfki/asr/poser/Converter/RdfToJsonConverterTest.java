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
		String inputString = "@prefix ctd: <http://connectd.api/> .\n" +
							"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
							"@prefix iots: <http://iotschema.org/> .\n" +
							"@prefix json: <http://some.json.ontology/> .\n" +
							"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
							"@prefix time: <https://www.w3.org/TR/2020/CR-owl-time-20200326/> .\n" +
							"\n" +
							"# Which inputs to expect and to start mapping from\n" +
							"json:InputDataType {\n" +
							"	json:EntryPoint a iots:TimeSeries;\n" +
							"		iots:providesTemperatureData iots:TemperatureData;\n" +
							"		iots:providesTimeData iots:TimeData .\n" +
							"\n" +
							"	iots:TemperatureData iots:numberDataType iots:Number .\n" +
							"	iots:TimeData  time:dateTime iots:Number .\n" +
							"}\n" +
							"\n" +
							"#Semantic description of the json objects to be found in the expected API\n" +
							"\n" +
							"json:ApiDescription {\n" +
							"	ctd:TemperatureValue a json:Number ;\n" +
							"		json:key \"value\"^^xsd:string ;\n" +
							"		json:dataType iots:TemperatureData ;\n" +
							"		json:parent ctd:Node .\n" +
							"		\n" +
							"	ctd:TimeStamp a json:String ;\n" +
							"		json:key \"timestamp\"^^xsd:string ;\n" +
							"		json:dataType iots:TimeData ;\n" +
							"		json:parent ctd:Node .\n" +
							"\n" +
							"	ctd:Node a json:Object;\n" +
							"		json:key \"node\"^^xsd:string ;\n" +
							"		json:dataType iots:TimeSeries;\n" +
							"		json:value ctd:TimeStamp, ctd:TemperatureValue ;\n" +
							"		json:parent ctd:Edges .\n" +
							"\n" +
							"	ctd:Edges a json:Array	;\n" +
							"		json:key \"edges\"^^xsd:string ;\n" +
							"		json:value ctd:Node .\n" +
							"}";
		InputStream modelStream = new ByteArrayInputStream(inputString.getBytes());
		Model jsonModel = Rio.parse(modelStream, RDFFormat.TRIG);
		return jsonModel;
	}

	private Model buildInputExample() throws IOException {
		String dataInput = "<http://sense.mapping.example/measurement/2021-01-10T19%3a58%3a49.294909Z> a <http://iotschema.org/Timeseries>;\n" +
					"  <http://iotschema.org/providesTemperatureData> <http://sense.mapping.example/measurement/Value2021-01-10T19%3a58%3a49.294909Z>;\n" +
					"  <http://iotschema.org/providesTimeData> <http://sense.mapping.example/measurement/Timestamp2021-01-10T19%3a58%3a49.294909Z> . \n"
				+ "<http://sense.mapping.example/measurement/Value2021-01-10T19%3a58%3a49.294909Z> a\n" +
				"    <http://iotschema.org/TemperatureData>;\n" +
				"  <http://iotschema.org/numberDataType> \"506.54\" . \n"
				+ "<http://sense.mapping.example/measurement/Timestamp2021-01-10T19%3a58%3a49.294909Z>\n" +
				"  a <http://iotschema.org/TimeData>;\n" +
				"  <https://www.w3.org/TR/2020/CR-owl-time-20200326/dateTime> \"2021-01-10T19:58:49.294909Z\" ." ;
		InputStream modelStream = new ByteArrayInputStream(dataInput.getBytes());
		Model jsonModel = Rio.parse(modelStream, RDFFormat.TRIG);
		return jsonModel;
	}
}
