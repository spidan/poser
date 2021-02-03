package de.dfki.asr.poser.util;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;


public class InputDataReader {

	public static Model getModelForType(String dataType, Model data) {
		IRI dataTypeIri = SimpleValueFactory.getInstance().createIRI(dataType);
		return data.filter(null, RDF.TYPE, dataTypeIri);
	}

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

	public static Model getSubInputModel(Resource subj, Model inputModel) {
		DynamicModelFactory mf = new DynamicModelFactory();
		Model resultModel = mf.createEmptyModel();
		for(Namespace ns: inputModel.getNamespaces()) {
			resultModel.setNamespace(ns);
		}
		resultModel = accumulateTriplesFromSubject(subj, inputModel, resultModel);
		return resultModel;
	}

	private static Model accumulateTriplesFromSubject(Resource subj, Model inputModel, Model resultModel) {
		Model subModel = inputModel.filter(subj, null, null);
		for(Statement st: subModel) {
			resultModel.add(st);
		}
		if (subModel.objects().isEmpty()) {
			return resultModel;
		}
		for (Value obj: subModel.objects()) {
			if(!obj.isLiteral()) {
				resultModel = accumulateTriplesFromSubject((Resource) obj, inputModel, resultModel);
			}
		}
		return resultModel;
	}
}