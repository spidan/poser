package de.dfki.asr.poser.Converter;

import de.dfki.asr.poser.util.RDFModelUtil;
import java.util.ArrayList;
import org.eclipse.rdf4j.model.Model;
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
		String inputType = RDFModelUtil.getDesiredInputType(jsonModel);
		// get the json object description that maps to this input type from the JSON model
		jsonResult = buildJsonObjectFromDescriptionFile(inputType, jsonModel);
		return jsonResult.toString();
	}

	private JSONObject buildJsonObjectFromDescriptionFile(String jsonType, Model jsonModel) {
		Model jsonObjectModel = RDFModelUtil.getModelForJsonObject(jsonType, jsonModel);
		JSONObject resultObject = new JSONObject();
		String jsonKey = RDFModelUtil.getKeyForObject(jsonObjectModel);
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