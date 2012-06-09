/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution;

import hudson.model.labels.LabelAtom;

import java.util.Set;

import jenkins.plugins.jclouds.compute.internal.RunningNode;

import org.jclouds.logging.Logger;


import com.google.common.util.concurrent.ListeningExecutorService;

import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.SimpleParallelExecutor;

/**
 * @author gena
 *
 */
@Strategy("Cassandra multi node cluster")
public class CassandraChefExecutorStrategy extends SimpleChefExecutorStrategy {

    public CassandraChefExecutorStrategy(ListeningExecutorService executor, Iterable<String> cookbooks, String chefJson,
	    Logger logger, Set<LabelAtom> labelsToRunOn, Integer... portsToCheck) {
	super(executor, cookbooks, chefJson, logger, labelsToRunOn, portsToCheck);

    }

    @Override
    public Void apply(Iterable<RunningNode> input) {

	SimpleParallelExecutor<CassandraNode> parallelExecutor = new SimpleParallelExecutor<CassandraNode>(executor, logger,
		new CassandraExecutorSupplier(input, executor, cookbooks, chefJson, logger, labelsToRunOn, portsToCheck));
	@SuppressWarnings("unused")
	Iterable<CassandraNode> cassandraNodes = parallelExecutor.apply(input);
	return null;
    }

}
