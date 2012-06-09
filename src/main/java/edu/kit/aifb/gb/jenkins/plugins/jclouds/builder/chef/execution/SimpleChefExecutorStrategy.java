/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution;

import hudson.model.labels.LabelAtom;

import java.util.Set;

import jenkins.plugins.jclouds.compute.internal.RunningNode;

import org.jclouds.logging.Logger;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListeningExecutorService;

import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.ChefRecipeExecutorFunction;
import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.SimpleParallelExecutor;

/**
 * @author gena
 *
 */
@Strategy("Simple Chef cookbooks")
public class SimpleChefExecutorStrategy implements ChefExecutorStrategy<Void> {
    protected final ListeningExecutorService executor;
    protected final Logger logger;
    protected final Integer[] portsToCheck;
    protected final Set<LabelAtom> labelsToRunOn;
    protected final Iterable<String> cookbooks;
    protected final String chefJson;

    public SimpleChefExecutorStrategy(ListeningExecutorService executor, Iterable<String> cookbooks, String chefJson,
	    Logger logger, Set<LabelAtom> labelsToRunOn, Integer... portsToCheck) {
	super();
	this.executor = executor;
	this.cookbooks = cookbooks;
	this.chefJson = chefJson;
	this.logger = logger;
	this.portsToCheck = portsToCheck;
	this.labelsToRunOn = labelsToRunOn;
    }

    /* (non-Javadoc)
     * @see edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution.ChefExecutorStrategy#apply(java.lang.Iterable)
     */

    @Override
    public Void apply(Iterable<RunningNode> input) {

	Function<RunningNode, Boolean> chefExecFunction = new ChefRecipeExecutorFunction(cookbooks, chefJson, logger,
		labelsToRunOn, portsToCheck);
	SimpleParallelExecutor<Boolean> parallelExecutor = new SimpleParallelExecutor<Boolean>(executor, logger,
		new SingleFunctionExecutorSupplier<Boolean>(input, chefExecFunction));
	parallelExecutor.apply(input);
	return null;
    }

}
