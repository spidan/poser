package de.dfki.asr.poser.Converter;

import de.dfki.asr.poser.Namespace.JSON;
import de.dfki.asr.poser.exceptions.DataTypeException;
import de.dfki.asr.poser.util.InputDataReader;
import de.dfki.asr.poser.util.RDFModelUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RdfToJson {
	private static final Logger LOG = LoggerFactory.getLogger(RdfToJson.class);
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
		this.jsonModel = jsonModel;

		inputModel.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		inputModel.setNamespace("json", "http://some.json.ontology/");
		JSONObject jsonResult = new JSONObject();
		// check the RDF description of the JSON API model for the desired input type
		Model rootObject = RDFModelUtil.getRootObject(jsonModel);
		jsonResult = buildJsonObjectFromModel(rootObject, inputModel, jsonResult);
		return jsonResult.toString();
	}

	private JSONObject buildJsonObjectFromModel(Model objectModel, Model inputModel, JSONObject resultObject) {
		String jsonKey = RDFModelUtil.getKeyForObject(objectModel);
		Value jsonDataType = RDFModelUtil.getValueTypeForObject(objectModel);
		if (!RDFModelUtil.isLiteral(jsonDataType)) {
			Set<Value> childValues = getChildValuesOfJsonObject(objectModel, jsonKey);
			//if this object maps to a input data type, make sure to generate an object for each corresponding value set
			if(objectModel.predicates().contains(JSON.DATA_TYPE)) {
				String valueType = RDFModelUtil.getCorrespondingInputValueType(objectModel, jsonModel);
				Model dataTypeModel = InputDataReader.getModelForType(valueType, inputModel);
				for(Resource subj: dataTypeModel.subjects()) {
					Model subInputModel = InputDataReader.getSubInputModel(subj, inputModel);
					JSONObject childJSON = new JSONObject();
					for(Value child: childValues) {
						Model childObjectModel = RDFModelUtil.getModelForResource(child, jsonModel);
						childJSON = buildJsonObjectFromModel(childObjectModel, subInputModel, childJSON);
					}
					addToParentObject(jsonDataType, resultObject, jsonKey, childJSON);
				}
				return resultObject;
			}
			return createChildObjectsForValues(childValues, inputModel, jsonDataType, resultObject, jsonKey);
		}
		else {
			String valueType = RDFModelUtil.getCorrespondingInputValueType(objectModel, jsonModel);
			String propertyName = RDFModelUtil.getPredicateNameForTypeFromModel(valueType, jsonModel);
			String valueResult = InputDataReader.getValueForType(valueType, propertyName, inputModel);
			return addLiteralValueToKey(jsonDataType, resultObject, jsonKey, valueResult);
		}
	}

	private JSONObject createChildObjectsForValues(Set<Value> childValues, Model inputModel1, Value jsonDataType, JSONObject resultObject, String jsonKey) throws JSONException, DataTypeException {
		JSONObject childJSON = new JSONObject();
		for (Value child : childValues) {
			Model childObjectModel = RDFModelUtil.getModelForResource(child, jsonModel);
			childJSON = buildJsonObjectFromModel(childObjectModel, inputModel1, childJSON);
			addToParentObject(jsonDataType, resultObject, jsonKey, childJSON);
		}
		return resultObject;
	}

	private JSONObject addLiteralValueToKey(Value jsonDataType, JSONObject resultObject, String jsonKey, String valueResult) throws NumberFormatException {
		if (JSON.NUMBER.equals(jsonDataType)) {
			return addToResult(resultObject, jsonKey, Double.parseDouble(valueResult));
		}
		else if (JSON.BOOLEAN.equals(jsonDataType)) {
			Boolean resultValue = ("1".equals(valueResult) || "true".equalsIgnoreCase(valueResult));
			return addToResult(resultObject, jsonKey, resultValue);
		}
		else {
			return addToResult(resultObject, jsonKey, valueResult);
		}
	}

	private void addToParentObject(Value jsonDataType, JSONObject resultObject, String jsonKey, JSONObject childJSON) throws JSONException, DataTypeException {
		if (JSON.OBJECT.equals(jsonDataType)) {
			resultObject.put(jsonKey, childJSON);
		} else if (JSON.ARRAY.equals(jsonDataType)) {
			resultObject.append(jsonKey, childJSON);
		} else {
			throw new DataTypeException("Could not determine JSON collection type");
		}
	}

	private Set<Value> getChildValuesOfJsonObject(Model objectModel, String jsonKey) throws DataTypeException {
		Set<Value> childValues = objectModel.filter(null, JSON.VALUE, null).objects();
		if(childValues.isEmpty()) {
			throw new DataTypeException("No value found for key " + jsonKey);
		}
		return childValues;
	}

	private JSONObject addToResult(JSONObject resultObject, String jsonKey, Object jsonValue) {
		return resultObject.put(jsonKey, jsonValue);
	}

	public String buildJsonString(String inputModelAsString, String loweringTemplate) {
		Model parsedJsonModel;
		Model inputModel;
		String resultJson;
		LOG.info("Mapping body " + inputModelAsString + " with lowering template " + loweringTemplate);
		try {
			parsedJsonModel = parseToTurtle(loweringTemplate);
			inputModel = parseToTurtle(inputModelAsString);
			RdfToJson jsonConverter = new RdfToJson();
			resultJson = jsonConverter.buildJsonString(inputModel, parsedJsonModel);
			return resultJson;
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
			return "geht nit";
		}
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