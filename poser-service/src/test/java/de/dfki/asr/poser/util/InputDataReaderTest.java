
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

public class InputDataReaderTest {

	@Test
	public void getValueForTypeShouldReturnTheLiteralAsString() throws IOException {
		String inputDataString = "<http://sense.mapping.example/measurement/2021-01-10T19%3a58%3a49.294909Z> a <http://iotschema.org/Timeseries>;\n" +
									"  <http://iotschema.org/providesTemperatureData> <http://sense.mapping.example/measurement/Value2021-01-10T19%3a58%3a49.294909Z>;\n" +
									"  <http://iotschema.org/providesTimeData> <http://sense.mapping.example/measurement/Timestamp2021-01-10T19%3a58%3a49.294909Z> . "
									+ "	<http://sense.mapping.example/measurement/Timestamp2021-01-11T19%3a46%3a14.338753Z>\n" +
									"  a <http://iotschema.org/TimeData>;\n" +
									"  <https://www.w3.org/TR/2020/CR-owl-time-20200326/dateTime> \"2021-01-11T19:46:14.338753Z\" .\n" +
									"\n" +
									"<http://sense.mapping.example/measurement/Value2021-01-10T19%3a58%3a49.294909Z> a\n" +
									"    <http://iotschema.org/TemperatureData>;\n" +
									"  <http://iotschema.org/numberDataType> \"506.54\" .";
		Model inputModel = triplesStringToModel(inputDataString);
		String valueString = InputDataReader.getValueForType("http://iotschema.org/TemperatureData",
															"http://iotschema.org/numberDataType",
															inputModel);
		assertEquals("506.54", valueString);
		}

	private Model triplesStringToModel(String inputModelAsString) throws IOException, RDFParseException, UnsupportedRDFormatException {
		InputStream inStream = new ByteArrayInputStream(inputModelAsString.getBytes());
		Model inputModel = Rio.parse(inStream, RDFFormat.TURTLE);
		return inputModel;
	}
}
