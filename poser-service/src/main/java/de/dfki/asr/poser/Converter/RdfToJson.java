package de.dfki.asr.poser.Converter;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONObject;

public class RdfToJson {

	/**
	 * Receives data in ttl format, finds the corresponding json objects to be generated
	 * in the semantic description of the target payload and generates the necessary objects
	 * including parent data structures to embed it in
	 * @param inputModel the data in semantic representation sent by the remote service
	 * @param jsonModel the semantic representation of the JSON to generate
	 * @return
	 */
	public String buildJsonString(final Model inputModel, final Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		inputModel.setNamespace("rdfs", "https://www.w3.org/TR/rdf-schema/");
		inputModel.setNamespace("json", "http://some.json.ontology/");
		JSONObject jsonResult = new JSONObject();
		// check the RDF description of the JSON API model for the desired input type
		String inputType = getDesiredInputType(jsonModel);
		// get the json object description that maps to this input type from the JSON model
		jsonResult = buildJsonObjectFromDescriptionFile(inputType, jsonModel);
		return jsonResult.toString();
	}

	/**
	 * Get the Input Type the JSON API is expecting from the JSON Model file
	 * @param jsonModel The RDF description of the JSON API
	 * @return The expected input type
	 */
	private String getDesiredInputType(Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI entryPoint = vf.createIRI("http://some.json.ontology/EntryPoint");
		for(Statement typeDescription: jsonModel.filter(entryPoint, null, null)) {
			Value typeObject = typeDescription.getObject();
			return typeObject.stringValue();
		}
		throw new NoSuchElementException("No entry point description found in JSON model");
	}

	private JSONObject buildJsonObjectFromDescriptionFile(String jsonType, Model jsonModel) {
		Model jsonObjectModel = getModelForJsonObject(jsonType, jsonModel);
		JSONObject resultObject = new JSONObject();
		String jsonKey = getKeyForObject(jsonObjectModel);
		ArrayList<Object> values = getValuesForObject(jsonObjectModel);
		for(Object value: values) {
			if (isLiteral(value)) {
				addToResult(resultObject, jsonKey);
			}
			else
			{
				String valueType = getTypeOfValue(value);
				addToResult(buildJsonObjectFromDescriptionFile(valueType, jsonModel), jsonKey);
			}
		}
		return resultObject;
	}

	/**
	 * Get the semantic representation of the JSON Object to be constructed from the JSON modelfile
	 * @param jsonType The type for which to build the JSON Object
	 * @return The model describing the Json Object for the given type
	 */
	private Model getModelForJsonObject(String jsonType, Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI predicateValueIri = vf.createIRI("http://some.json.ontology/value");
		IRI objectJsonTypeIri = vf.createIRI(jsonType);
		Model jsonObjectModel = jsonModel.filter(null, predicateValueIri, objectJsonTypeIri);
		return jsonObjectModel;
	}

	/**
	 * Given a semantic representation of a API JSONObject from the jsonModelFile, get the
	 * value of the resource describing the key
	 * @param jsonObjectModel The Model to query for the name of the JSON key
	 * @return The Json key as String
	 */
	private String getKeyForObject(Model jsonObjectModel) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private ArrayList<Object> getValuesForObject(Model jsonObjectModel) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private boolean isLiteral(Object value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private void addToResult(JSONObject resultObject, String jsonKey) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private String getTypeOfValue(Object value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}