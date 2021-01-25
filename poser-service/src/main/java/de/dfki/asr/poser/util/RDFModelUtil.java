package de.dfki.asr.poser.util;

import de.dfki.asr.poser.exceptions.DataTypeException;
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
	 * @param jsonModel The RDF model of the expected JSON API
	 * @return The model describing the Json Object for the given type
	 */
	public static Model getModelForJsonObject(String jsonType, Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI predicateValueIri = vf.createIRI("http://some.json.ontology/dataType");
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
	 * @return true if the value is a literal, false otherwise
	 */
	public static boolean isLiteral(Value value) {
		return (value.stringValue().equals("http://some.json.ontology/literal"));
	}

	/**
	 * Get the type of a value from the JSON descriptionModel
	 * @param value The JSON value for which the corresponding semantic type description is needed
	 * @param jsonModel The model representing the JSON payload description
	 * @return The value in string representation
	 */
	public static String getTypeOfValue(Value value, Model jsonModel) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI valueObjectIri = vf.createIRI(value.stringValue());
		IRI dataTypeIri = vf.createIRI("http://some.json.ontology/dataType");
		Set<Value> datatypes = jsonModel.filter(valueObjectIri, dataTypeIri, null).objects();
		if (datatypes.isEmpty()) {
			throw new NoSuchElementException("JSON object contains no reference to a semantic datatype");
		}
		return datatypes.iterator().next().stringValue();
	}

	public static String getPredicateNameForTypeFromModel(String dataType, Model jsonModel ) {
		IRI dataTypeIri = SimpleValueFactory.getInstance().createIRI(dataType);
		IRI dataTypeContext = SimpleValueFactory.getInstance().createIRI("http://some.json.ontology/InputDataType");
		Set<IRI> predicates = jsonModel.filter(dataTypeIri, null, null, dataTypeContext).predicates();
		if (predicates.size() != 1) {
			throw new DataTypeException("No unique data type definition found");
		}
		return predicates.iterator().next().stringValue();
	}
}
