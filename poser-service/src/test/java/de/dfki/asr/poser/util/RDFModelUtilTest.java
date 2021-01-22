package de.dfki.asr.poser.util;

import de.dfki.asr.poser.exceptions.DataTypeException;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
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

	@Test
	public void jsonTypeShouldRetrieveModel() throws IOException {
		String jsonModelAsString = "@prefix ctd: <http://connectd.api/> .\n" +
			"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
			"@prefix iots: <http://iotschema.org/> .\n" +
			"@prefix json: <http://some.json.ontology/> .\n" +
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." +
			"ctd:TimeStamp a json:object ;\n" +
			"	json:key \"timestamp\"^^xsd:string ;\n" +
			"	json:dataType iots:TimeData ;\n" +
			"	json:parent ctd:Node .\n" +
			"\n" +
			"ctd:Node a json:object	;\n" +
			"	json:key \"node\"^^xsd:string ;\n" +
			"	json:dataType iots:TimeSeries;\n" +
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
			"	json:dataType iots:TimeSeries;\n" +
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
			"	json:dataType iots:TimeSeries;\n" +
			"	json:children ctd:TimeStamp, ctd:TemperatureValue .";
		Model objectModel = stringToModel(objectRepresentationString);
		String jsonKey = RDFModelUtil.getKeyForObject(objectModel);
		assertEquals("node", jsonKey);
	}

	@Test
	public void getValuesShouldGetValueFromObject() throws IOException {
		String objectRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
			"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
			"@prefix iots: <http://iotschema.org/> .\n" +
			"@prefix json: <http://some.json.ontology/> .\n" +
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
			"\n" +
		"ctd:Node a json:object	;\n" +
"		json:key \"node\"^^xsd:string ;\n" +
"		json:dataType iots:TimeSeries;\n" +
"		json:value ctd:TempTimePair;\n" +
"		json:children ctd:TimeStamp, ctd:TemperatureValue .";
		Model objectModel = stringToModel(objectRepresentationString);
		Set<Value> resultSet = RDFModelUtil.getValuesForObject(objectModel);
		IRI resultIri = SimpleValueFactory.getInstance().createIRI("http://connectd.api/TempTimePair");
		assertEquals(1, resultSet.size());
		assertTrue(resultSet.contains(resultIri));
	}

	@Test
	public void isLiteralShouldBeFalseForNonLiteralValues() throws IOException {
		String modelRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
				"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
				"@prefix iots: <http://iotschema.org/> .\n" +
				"@prefix json: <http://some.json.ontology/> .\n" +
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
				"	ctd:Node a json:object	;\n" +
				"		json:key \"node\"^^xsd:string ;\n" +
				"		json:dataType iots:TimeSeries;\n" +
				"		json:value ctd:TempTimePair;\n" +
				"		json:children ctd:TimeStamp, ctd:TemperatureValue .\n" +
				"\n" +
				"	ctd:TempTimePair a json:object;\n" +
				"		json:value ctd:TemperatureValue;\n" +
				"		json:value ctd:TimeStamp . \n" +
				"		\n" +
				"	ctd:NodeArray a json:array ;\n" +
				"		json:arrayType ctd:Node .\n" +
				"\n" +
				"	ctd:Edges a json:object	;\n" +
				"		json:key \"edges\"^^xsd:string ;\n" +
				"		json:value ctd:NodeArray .\n";
		Model jsonModel = stringToModel(modelRepresentationString);
		Value valueToCheck = SimpleValueFactory.getInstance().createIRI("http://connectd.api/TempTimePair");
		assertFalse(RDFModelUtil.isLiteral(valueToCheck, jsonModel));
	}

	@Test
	public void isLiteralShouldBeTrueForLiteralValues() throws IOException {
		String modelRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
				"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
				"@prefix iots: <http://iotschema.org/> .\n" +
				"@prefix json: <http://some.json.ontology/> .\n" +
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
				"	ctd:TemperatureValue a json:object ;\n" +
				"		json:key \"value\"^^xsd:string ;\n" +
				"		json:dataType iots:TemperatureData ;\n" +
				"		json:value json:literal;\n" +
				"		json:parent ctd:Node .";
		Model jsonModel = stringToModel(modelRepresentationString);
		Value valueToCheck = SimpleValueFactory.getInstance().createIRI("http://connectd.api/TemperatureValue");
		assertTrue(RDFModelUtil.isLiteral(valueToCheck, jsonModel));
	}

	@Test(expected = NoSuchElementException.class)
	public void isLiteralShouldThrowExceptionWhenValueIsMissing() throws IOException {
		String modelRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
				"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
				"@prefix iots: <http://iotschema.org/> .\n" +
				"@prefix json: <http://some.json.ontology/> .\n" +
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
				"	ctd:TemperatureValue a json:object ;\n" +
				"		json:dataType iots:TemperatureData ;\n" +
				"		json:parent ctd:Node .";
		Model jsonModel = stringToModel(modelRepresentationString);
		Value valueToCheck = SimpleValueFactory.getInstance().createIRI("http://connectd.api/TemperatureValue");
		assertFalse(RDFModelUtil.isLiteral(valueToCheck, jsonModel));
	}

	@Test
	public void getTypeOfValueShouldReturnDatatype() throws IOException {
		String modelRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
				"@prefix iots: <http://iotschema.org/> .\n" +
				"@prefix json: <http://some.json.ontology/> .\n" +
				"ctd:TemperatureValue a json:object ;\n" +
				"		json:key \"value\"^^xsd:string ;\n" +
				"		json:dataType iots:TemperatureData ;\n" +
				"		json:value json:literal;\n" +
				"		json:parent ctd:Node .";
		Model jsonModel = stringToModel(modelRepresentationString);
		Value valueIri = SimpleValueFactory.getInstance().createIRI("http://connectd.api/TemperatureValue");
		String typeStringResult = RDFModelUtil.getTypeOfValue(valueIri, jsonModel);
		assertEquals("http://iotschema.org/TemperatureData", typeStringResult);
	}

	@Test(expected = NoSuchElementException.class)
	public void getTypeOfValueShouldThrowExceptionWhenNoDatatypePresent() throws IOException {
		String modelRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
				"@prefix iots: <http://iotschema.org/> .\n" +
				"@prefix json: <http://some.json.ontology/> .\n" +
				"ctd:TemperatureValue a json:object ;\n" +
				"		json:key \"value\"^^xsd:string ;\n" +
				"		json:value json:literal;\n" +
				"		json:parent ctd:Node .";
		Model jsonModel = stringToModel(modelRepresentationString);
		Value valueIri = SimpleValueFactory.getInstance().createIRI("http://connectd.api/TemperatureValue");
		String typeStringResult = RDFModelUtil.getTypeOfValue(valueIri, jsonModel);
		assertNotEquals("http://iotschema.org/TemperatureData", typeStringResult);
	}

	@Test
	public void getPredicateNameForTypeFromModelShouldReturnPredicateName() throws IOException {
		String modelRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
					"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
					"@prefix iots: <http://iotschema.org/> .\n" +
					"@prefix json: <http://some.json.ontology/> .\n" +
					"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
					"\n" +
					"# Which inputs to expect and to start mapping from\n" +
					"json:InputDataType {\n" +
					"	json:EntryPoint a iots:TimeSeries;\n" +
					"		iots:providesTemperatureData iots:TemperatureData;\n" +
					"		iots:providesTimeData iots:TimeData .\n" +
					"	\n" +
					"	iots:TemperatureData iots:numberDataType iots:Number .\n" +
					"}";
		Model dataTypeModel = stringToContextedModel(modelRepresentationString);
		String predicateName = RDFModelUtil.getPredicateNameForTypeFromModel("http://iotschema.org/TemperatureData", dataTypeModel);
		assertEquals("http://iotschema.org/numberDataType", predicateName);
	}

	@Test(expected = DataTypeException.class)
	public void getPredicateNameForTypeFromModelShouldFailWithMissingDatatype() throws IOException {
		String modelRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
					"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
					"@prefix iots: <http://iotschema.org/> .\n" +
					"@prefix json: <http://some.json.ontology/> .\n" +
					"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
					"\n" +
					"# Which inputs to expect and to start mapping from\n" +
					"json:InputDataType {\n" +
					"	json:EntryPoint a iots:TimeSeries;\n" +
					"		iots:providesTemperatureData iots:TemperatureData;\n" +
					"		iots:providesTimeData iots:TimeData .\n" +
					"	\n" +
					"}";
		Model dataTypeModel = stringToContextedModel(modelRepresentationString);
		String predicateName = RDFModelUtil.getPredicateNameForTypeFromModel("http://iotschema.org/TemperatureData", dataTypeModel);
		assertNotEquals("http://iotschema.org/numberDataType", predicateName);
	}

	@Test(expected = DataTypeException.class)
	public void getPredicateNameForTypeFromModelShouldFailWithMultipleDatatypes() throws IOException {
		String modelRepresentationString = "@prefix ctd: <http://connectd.api/> .\n" +
					"@prefix onto: <http://ontodm.com/OntoDT#> .\n" +
					"@prefix iots: <http://iotschema.org/> .\n" +
					"@prefix json: <http://some.json.ontology/> .\n" +
					"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
					"\n" +
					"# Which inputs to expect and to start mapping from\n" +
					"json:InputDataType {\n" +
					"	json:EntryPoint a iots:TimeSeries;\n" +
					"		iots:providesTemperatureData iots:TemperatureData;\n" +
					"		iots:providesTimeData iots:TimeData .\n" +
					"	\n" +
					"	iots:TemperatureData iots:numberDataType iots:Number .\n" +
					"	iots:TemperatureData iots:secondDataType iots:Number .\n" +
					"}";
		Model dataTypeModel = stringToContextedModel(modelRepresentationString);
		String predicateName = RDFModelUtil.getPredicateNameForTypeFromModel("http://iotschema.org/TemperatureData", dataTypeModel);
		assertNotEquals("http://iotschema.org/numberDataType", predicateName);
	}

	private Model stringToModel(String jsonModelAsString) throws IOException, RDFParseException, UnsupportedRDFormatException {
		InputStream inStream = new ByteArrayInputStream(jsonModelAsString.getBytes());
		Model jsonModel = Rio.parse(inStream, RDFFormat.TURTLE);
		return jsonModel;
	}

	private Model stringToContextedModel(String jsonModelAsString) throws IOException, RDFParseException, UnsupportedRDFormatException {
		InputStream inStream = new ByteArrayInputStream(jsonModelAsString.getBytes());
		Model jsonModel = Rio.parse(inStream, RDFFormat.TRIG);
		return jsonModel;
	}

}
