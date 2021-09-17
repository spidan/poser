package de.dfki.asr.poser.Controller;

import de.dfki.asr.poser.Converter.RdfToJson;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PoserControllers {

	@GetMapping("/isAlive")
	public String testService()
	{
		return "ok";
	}

	@PostMapping(path = "/rdfToJson",
				consumes = "text/turtle",
				produces = "application/json")
	public ResponseEntity<?> getJsonFromRDF(@RequestParam final String loweringTemplateName,
											@RequestBody final String turtleInput) {
		Model jsonModel;
		Model inputModel;
		String resultJson;
		try {
			InputStream templateStream = this.getClass().getResourceAsStream("/".concat(loweringTemplateName));
			String loweringTemplate = IOUtils.toString(templateStream, Charset.forName("utf-8"));
			jsonModel = parseToTurtle(loweringTemplate);
			inputModel = parseToTurtle(turtleInput);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		try {
			RdfToJson jsonConverter = new RdfToJson();
			resultJson = jsonConverter.buildJsonString(inputModel, jsonModel);
		} catch (Exception e) {
			return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(resultJson, HttpStatus.OK);
	}

	private Model parseToTurtle(final String response) throws RDFHandlerException,
	    UnsupportedRDFormatException, UnsupportedEncodingException, IOException, RDFParseException {
	InputStream rdfStream = new ByteArrayInputStream(response.getBytes("utf-8"));
	RDFParser parser = Rio.createParser(RDFFormat.TRIG);
	Model model = new LinkedHashModel();
	parser.setRDFHandler(new StatementCollector(model));
	parser.parse(rdfStream);
	return model;
    }
}
