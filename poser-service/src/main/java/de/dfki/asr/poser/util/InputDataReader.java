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
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.sail.memory.MemoryStore;


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
		Repository repo = new SailRepository(new MemoryStore());
		String queryString = getSubgraphQueryString(subj);
		try (RepositoryConnection conn =  repo.getConnection()) {
			conn.add(inputModel);
		}
		Model triplesWithSubject = Repositories.graphQuery(repo, queryString, r -> QueryResults.asModel(r) );
		return triplesWithSubject;
	}

	private static String getSubgraphQueryString(Resource subj) {
		String subjectResource = subj.stringValue();
		String queryString = "PREFIX x: <http://pre.fix/> \n"
				+ "CONSTRUCT { ?s ?p ?o }\n" +
		"where { <"+ subjectResource +"> (x:|!x:)* ?s . ?s ?p ?o . }";
		return queryString;
	}
}