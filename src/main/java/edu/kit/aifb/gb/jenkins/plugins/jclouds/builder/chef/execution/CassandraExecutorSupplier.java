/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution;

import hudson.Util;
import hudson.model.labels.LabelAtom;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jclouds.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListeningExecutorService;

import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.ChefRecipeExecutorFunction;

import static com.google.common.collect.Iterables.*;

import jenkins.plugins.jclouds.compute.internal.RunningNode;

/**
 * @author gena
 *
 */
public class CassandraExecutorSupplier extends ExecutorSupplier<CassandraNode> {

    protected final ListeningExecutorService executor;
    protected final Logger logger;
    protected final Iterable<Integer> portsToCheck;
    protected final Set<LabelAtom> labelsToRunOn;
    protected final Iterable<String> cookbooks;
    protected final String chefJson;
    private Map<RunningNode, Function<RunningNode, CassandraNode>> suppliers;

    public CassandraExecutorSupplier(Iterable<RunningNode> runningNodes, ListeningExecutorService executor,
	    Iterable<String> cookbooks, String chefJson, Logger logger, Set<LabelAtom> labelsToRunOn, Integer... portsToCheck) {
	super(runningNodes);
	this.executor = executor;
	this.cookbooks = cookbooks;
	this.chefJson = chefJson;
	this.logger = logger;
	this.portsToCheck = ImmutableList.copyOf(portsToCheck);
	this.labelsToRunOn = labelsToRunOn;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.common.base.Supplier#get()
     */
    @Override
    public Map<RunningNode, Function<RunningNode, CassandraNode>> get() {
	if(suppliers==null){
	ImmutableMap.Builder<RunningNode, Function<RunningNode, CassandraNode>> builder = new ImmutableMap.Builder<RunningNode, Function<RunningNode, CassandraNode>>();
	List<RunningNode> runningNodeList = ImmutableList.copyOf(runningNodes);
	    final String seeds = Joiner.on(", ").join(transform(runningNodeList, new Function<RunningNode, String>() {

	    @Override
	    public String apply(RunningNode input) {

		return getFirst(input.getNode().getPublicAddresses(), "localhost");
	    }
	}));
	String[] tokens = calcTokens(runningNodeList.size());

	for (int i = 0; i < runningNodeList.size(); i++) {
	    final RunningNode runningNode = runningNodeList.get(i);
		final CassandraNode cassandraNode = new CassandraNode(getFirst(runningNode.getNode().getPrivateAddresses(),
			"localhost"), getFirst(runningNode.getNode().getPrivateAddresses(), "localhost"), seeds, runningNode,
			i > 0, tokens[i]);

		final Map<String, String> environment = ImmutableMap.<String, String> of("SEEDS", cassandraNode.getSeeds(),
			"INITIAL_TOKEN",
		    cassandraNode.getInitialToken(), "LISTEN_ADDR", cassandraNode.getListenAddress(), "RPC_ADDR",
		    cassandraNode.getRpcAddress(), "AUTO_BOOTSTRAP", Boolean.toString(cassandraNode.isAutoBootstap()));

	    builder.put(runningNode,

	    new Function<RunningNode, CassandraNode>() {

		@Override
		public CassandraNode apply(RunningNode input) {
		    boolean result = new ChefRecipeExecutorFunction(cookbooks, Util.replaceMacro(chefJson, environment), logger,
			    labelsToRunOn, toArray(portsToCheck, Integer.class)).apply(input);
		    return result ? cassandraNode : null;
		}

	    });
	}
	    suppliers = builder.build();
    }
	return suppliers;
    }

    private String[] calcTokens(long size) {
	BigDecimal RING_SIZE = BigDecimal.valueOf(2).pow(127);
	ImmutableList.Builder<String> tokens = ImmutableList.<String> builder();
	for (int i = 0; i < size; i++)
	    tokens.add(RING_SIZE.divide(BigDecimal.valueOf(size)).multiply(BigDecimal.valueOf(i)).toPlainString());

	return Iterables.toArray(tokens.build(), String.class);
    }

}
