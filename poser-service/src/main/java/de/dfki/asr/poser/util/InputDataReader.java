package de.dfki.asr.poser.util;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class InputDataReader {

	public static String getValueForType(String dataType, String propertyName, Model data) {
		IRI dataTypeIri = SimpleValueFactory.getInstance().createIRI(dataType);
		IRI propertyNameIri = SimpleValueFactory.getInstance().createIRI(propertyName);
		Model dataModel = data.filter(null, RDF.TYPE, dataTypeIri);
		Set<Resource> subjectOfGivenType = dataModel.subjects();
		if(subjectOfGivenType.isEmpty()) {
			throw new NoSuchElementException("No resource of the given type found in input data");
		}
		Optional<Literal> dataValue = Models.objectLiteral(data.filter(subjectOfGivenType.iterator().next(), propertyNameIri, null));
		if (dataValue.isEmpty()) {
			throw new NoSuchElementException("No iteral value for datatype " + dataType + " with property "
								+ propertyName +" found.");
		}
		return dataValue.get().stringValue();
	}
}
