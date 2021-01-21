package de.dfki.asr.poser.util;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.junit.Test;



public class RDFModelUtilTest {

	@Test
	public void thereShouldBeExactlyOneEntryPoint() throws IOException {
		String jsonModelAsString = "@prefix ctd: <http://connectd.api/> .\n" +
			"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
			"@prefix iots: <http://iotschema.org/> .\n" +
			"@prefix json: <http://some.json.ontology/> .\n" +
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
			"\n" +
			"# Which inputs to expect and to start mapping from\n" +
			"json:EntryPoint a iots:TimeSeries;\n" +
			"	iots:providesTemperatureData iots:TemperatureData;\n" +
			"	iots:providesTimeData iots:TimeData . ";
		Model jsonModel = stringToModel(jsonModelAsString);
		String inputFromModel = RDFModelUtil.getDesiredInputType(jsonModel);
		assertEquals(inputFromModel, "http://iotschema.org/TimeSeries");
	}

	private Model stringToModel(String jsonModelAsString) throws IOException, RDFParseException, UnsupportedRDFormatException {
		InputStream inStream = new ByteArrayInputStream(jsonModelAsString.getBytes());
		Model jsonModel = Rio.parse(inStream, RDFFormat.TURTLE);
		return jsonModel;
	}

	@Test
	public void jsonTypeShouldRetrieveModel() throws IOException {
		String jsonModelAsString = "@prefix ctd: <http://connectd.api/> .\n" +
			"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
			"@prefix iots: <http://iotschema.org/> .\n" +
			"@prefix json: <http://some.json.ontology/> .\n" +
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." +
			"ctd:TimeStamp a json:object ;\n" +
			"	json:key \"timestamp\"^^xsd:string ;\n" +
			"	json:value iots:TimeData ;\n" +
			"	json:parent ctd:Node .\n" +
			"\n" +
			"ctd:Node a json:object	;\n" +
			"	json:key \"node\"^^xsd:string ;\n" +
			"	json:value iots:TimeSeries;\n" +
			"	json:children ctd:TimeStamp, ctd:TemperatureValue .";
		Model jsonModel = stringToModel(jsonModelAsString);
		String jsonType = "http://iotschema.org/TimeSeries";
		String expectedResultModelString = "@prefix ctd: <http://connectd.api/> .\n" +
			"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
			"@prefix iots: <http://iotschema.org/> .\n" +
			"@prefix json: <http://some.json.ontology/> .\n" +
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
			"\n" +
				"ctd:Node a json:object	;\n" +
			"	json:key \"node\"^^xsd:string ;\n" +
			"	json:value iots:TimeSeries;\n" +
			"	json:children ctd:TimeStamp, ctd:TemperatureValue .";
		Model resultModel = RDFModelUtil.getModelForJsonObject(jsonType, jsonModel);
		assertEquals(stringToModel(expectedResultModelString), resultModel);
	}

	@Test
	public void getKeyShouldGiveKeyFromObject() throws IOException {
		String objectRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
			"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
			"@prefix iots: <http://iotschema.org/> .\n" +
			"@prefix json: <http://some.json.ontology/> .\n" +
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
			"\n" +
				"ctd:Node a json:object	;\n" +
			"	json:key \"node\"^^xsd:string ;\n" +
			"	json:value iots:TimeSeries;\n" +
			"	json:children ctd:TimeStamp, ctd:TemperatureValue .";
		Model objectModel = stringToModel(objectRepresentationString);
		String jsonKey = RDFModelUtil.getKeyForObject(objectModel);
		assertEquals("node", jsonKey);
	}
}
