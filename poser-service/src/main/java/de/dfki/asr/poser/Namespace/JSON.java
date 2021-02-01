package de.dfki.asr.poser.Namespace;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class JSON {
	public static final String NAMESPACE = "http://some.json.ontology/";
	public static final String PREFIX = "json";
	public static final IRI NUMBER = getIri("Number");
	public static final IRI STRING = getIri("String");
	public static final IRI OBJECT = getIri("Object");
	public static final IRI ARRAY = getIri("Array");
	public static final IRI BOOLEAN = getIri("Boolean");
	public static final IRI VALUE = getIri("value");
	public static final IRI DATA_TYPE = getIri("dataType");
	public static final IRI INPUT_DATA_TYPE = getIri("InputDataType");
	public static final IRI ROOT = getIri("hasRoot");

	private static IRI getIri(String localName) {
		return SimpleValueFactory.getInstance().createIRI(NAMESPACE, localName);
	}
}
