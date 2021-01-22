package de.dfki.asr.poser.util;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;

public class RDFModelUtil {

	/**
	 * Get the semantic representation of the JSON Object to be constructed from the JSON modelfile
	 * @param jsonType The type for which to build the JSON Object
	 * @return The model describing the Json Object for the given type
	 */
	public static Model getModelForJsonObject(String jsonType, Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI predicateValueIri = vf.createIRI("http://some.json.ontology/value");
		IRI objectJsonTypeIri = vf.createIRI(jsonType);
		// get the subject of the model representing the data
		Resource jsonObjectDescription = jsonModel.filter(null, predicateValueIri, objectJsonTypeIri).subjects().iterator().next();
		Model jsonObjectModel = jsonModel.filter(jsonObjectDescription, null, null);
		return jsonObjectModel;
	}

	/**
	 * Get the Input Type the JSON API is expecting from the JSON Model file
	 * @param jsonModel The RDF description of the JSON API
	 * @return The expected input type
	 */
	public static String getDesiredInputType(Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI entryPoint = vf.createIRI("http://some.json.ontology/EntryPoint");
		for(Statement typeDescription: jsonModel.filter(entryPoint, null, null)) {
			Value typeObject = typeDescription.getObject();
			return typeObject.stringValue();
		}
		throw new NoSuchElementException("No entry point description found in JSON model");
	}

	/**
	 * Given a semantic representation of a API JSONObject from the jsonModelFile, get the
	 * value of the resource describing the key
	 * @param jsonObjectModel The Model to query for the name of the JSON key
	 * @return The Json key as String
	 */
	public static String getKeyForObject(Model jsonObjectModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI keyIri = vf.createIRI( "http://some.json.ontology/key");
		Optional<Literal> key = Models.objectLiteral(jsonObjectModel.filter(null, keyIri, null));
		if(key.isEmpty()) {
			throw new NoSuchElementException("JSON Object " + jsonObjectModel.toString() + " is missing a key");
		}
		return key.get().stringValue();
	}

	/**
	 * Get the values from the provided json object model
	 * @param jsonObjectModel
	 * @return A list of values
	 */
	public static Set<Value> getValuesForObject(Model jsonObjectModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI dataTypeIri = vf.createIRI("http://some.json.ontology/value");
		Set<Value> valueModel = jsonObjectModel.filter(null, dataTypeIri, null).objects();
		return valueModel;
	}

	/**
	 * Check whether the given value is a literal, according to the json description model provided
	 * @param value the IRI of the value to be checked for being a literal
	 * @param jsonModel the model representing the JSON payload description
	 * @return true if the value is a literal, false otherwise
	 */
	public static boolean isLiteral(Value value, Model jsonModel) {
		IRI objectIri = SimpleValueFactory.getInstance().createIRI(value.stringValue());
		Model valueModel = jsonModel.filter(objectIri, null, null);
		IRI valueIri = SimpleValueFactory.getInstance().createIRI("http://some.json.ontology/value");
		Model valueOfThisObject = valueModel.filter(null, valueIri, null);
		Set<Value> values = valueOfThisObject.objects();
		if (values.isEmpty()) {
			throw new NoSuchElementException("JSON object is missing a value definition");
		}
		if(values.size() > 1) {
			return false; // result is a list of values, not a single literal, so we can safely assume this is not a literal
		}
		return (values.iterator().next().stringValue().equals("http://some.json.ontology/literal"));
	}
}
