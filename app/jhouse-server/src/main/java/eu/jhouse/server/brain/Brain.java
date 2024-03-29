package eu.jhouse.server.brain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tomekk
 * @since 2010-10-07, 22:46:54
 */
public class Brain {
	Logger log = LoggerFactory.getLogger(Brain.class);

	private Map<Object, FactHandle> facts = new HashMap<Object, FactHandle>();

	private String pathToLogicFile;

	private StatefulKnowledgeSession knowledgeSession;

	public void setPathToLogicFile(String pathToLogicFile) {
		this.pathToLogicFile = pathToLogicFile;
	}

	public void init() {
		KnowledgeBase base = readKnowledgeBase();
		this.knowledgeSession = base.newStatefulKnowledgeSession();
	}

	public void thinkForNewFact(Object item) {
		addFact(item);
		knowledgeSession.fireAllRules();
	}

	private void addFact(Object item) {
		FactHandle factHandle = facts.get(item);
		if (factHandle != null) {
			knowledgeSession.update(factHandle, item);
		} else {
			FactHandle newFactHandle = knowledgeSession.insert(item);
			facts.put(item, newFactHandle);
		}
	}

	public void thinkForNewFacts(Collection list) {
		for (Object item : list) {
			addFact(item);
		}
		knowledgeSession.fireAllRules();
	}

	private KnowledgeBase readKnowledgeBase() {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		log.info("Loading configuration from file: '" + pathToLogicFile + "'");
		kbuilder.add(ResourceFactory.newClassPathResource(pathToLogicFile), ResourceType.DRL);
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		log.info("Configuration loaded successfully: '" + pathToLogicFile + "'");
		return kbase;
	}

}
