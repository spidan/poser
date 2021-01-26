package de.dfki.asr.poser.Converter;

import de.dfki.asr.poser.Namespace.JSON;
import de.dfki.asr.poser.util.InputDataReader;
import de.dfki.asr.poser.util.RDFModelUtil;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.json.JSONObject;

public class RdfToJson {

	private Model inputModel;
	private Model jsonModel;
	/**
	 * Receives data in ttl format, finds the corresponding json objects to be generated
	 * in the semantic description of the target payload and generates the necessary objects
	 * including parent data structures to embed it in
	 * @param inputModel the data in semantic representation sent by the remote service
	 * @param jsonModel the semantic representation of the JSON to generate
	 * @return
	 */
	public String buildJsonString(final Model inputModel, final Model jsonModel) {
		this.inputModel = inputModel;
		this.jsonModel = jsonModel;

		inputModel.setNamespace("rdfs", "https://www.w3.org/TR/rdf-schema/");
		inputModel.setNamespace("json", "http://some.json.ontology/");
		JSONObject jsonResult = new JSONObject();
		// check the RDF description of the JSON API model for the desired input type
		String inputType = RDFModelUtil.getDesiredInputType(jsonModel);
		// get the json object description that maps to this input type from the JSON model
		jsonResult = buildJsonObjectFromDescriptionFile(inputType);
		return jsonResult.toString();
	}

	private JSONObject buildJsonObjectFromDescriptionFile(String jsonType) {
		Model jsonObjectModel = RDFModelUtil.getModelForJsonObject(jsonType, jsonModel);
		JSONObject resultObject = new JSONObject();
		String jsonKey = RDFModelUtil.getKeyForObject(jsonObjectModel);
		Value jsonDataType = RDFModelUtil.getValueTypeForObject(jsonObjectModel);
		if (RDFModelUtil.isLiteral(jsonDataType)) {
			String valueType = RDFModelUtil.getCorrespondingInputValueType(jsonObjectModel, jsonModel);
			String propertyName = RDFModelUtil.getPredicateNameForTypeFromModel(valueType, jsonModel);
			String valueResult = InputDataReader.getValueForType(valueType, propertyName, inputModel);
			if (JSON.NUMBER.equals(jsonDataType)) {
				addToResult(resultObject, jsonKey, Double.parseDouble(valueResult));
			}
			else if (JSON.BOOLEAN.equals(jsonDataType)) {
				Boolean resultValue = ("1".equals(valueResult) || "true".equalsIgnoreCase(valueResult));
				addToResult(resultObject, jsonKey, resultValue);
			}
			else addToResult(resultObject, jsonKey, valueResult);
		}
		return resultObject;
	}

	private void addToResult(JSONObject resultObject, String jsonKey, Object jsonValue) {
		resultObject.accumulate(jsonKey, jsonValue);
	}
}