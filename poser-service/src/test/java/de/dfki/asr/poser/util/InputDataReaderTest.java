
package de.dfki.asr.poser.util;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.junit.Test;

public class InputDataReaderTest {

	@Test
	public void getValueForTypeShouldReturnTheLiteralAsString() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream testFileStream = classLoader.getResourceAsStream("liftedExampleSingleValue.ttl");
		String inputDataString = IOUtils.toString(testFileStream, "UTF-8");
		Model inputModel = triplesStringToModel(inputDataString);
		String valueString = InputDataReader.getValueForType("http://iotschema.org/TemperatureData",
															"http://iotschema.org/numberDataType",
															inputModel);
		assertEquals("506.54", valueString);
	}

	@Test
	public void getSubInputModelShouldReturnAllTriplesRelatedToSubject () throws IOException {
		Model inputDataModel = readModelFromFile("liftedExampleMultipleValues.ttl");
		Resource subject = SimpleValueFactory.getInstance().createIRI("http://sense.mapping.example/measurement/2021-01-10T19%3a58%3a49.294909Z");
		Model resultModel = InputDataReader.getSubInputModel(subject, inputDataModel);
		Model expectedModel = readModelFromFile("liftedExampleSingleValue.ttl");
		assertEquals(expectedModel, resultModel);
	}

	private Model triplesStringToModel(String inputModelAsString) throws IOException, RDFParseException, UnsupportedRDFormatException {
		InputStream inStream = new ByteArrayInputStream(inputModelAsString.getBytes());
		Model inputModel = Rio.parse(inStream, RDFFormat.TURTLE);
		return inputModel;
	}

	private Model readModelFromFile(String filename) throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream modelStream = classLoader.getResourceAsStream(filename);
		Model jsonModel = Rio.parse(modelStream, RDFFormat.TRIG);
		return jsonModel;
	}
}
