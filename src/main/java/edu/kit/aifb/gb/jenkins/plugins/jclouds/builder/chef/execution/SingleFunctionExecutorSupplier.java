/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import jenkins.plugins.jclouds.compute.internal.RunningNode;

/**
 * @author gena
 *
 */
public class SingleFunctionExecutorSupplier<T> extends ExecutorSupplier<T> {
    protected final Function<RunningNode, T> singleFunction;

    /**
     * @param runningNodes
     */
    public SingleFunctionExecutorSupplier(Iterable<RunningNode> runningNodes, Function<RunningNode, T> singleFunction) {
	super(runningNodes);
	this.singleFunction = singleFunction;
    }

    /* (non-Javadoc)
     * @see com.google.common.base.Supplier#get()
     */
    @Override
    public Map<RunningNode, Function<RunningNode, T>> get() {
	ImmutableMap.Builder<RunningNode, Function<RunningNode, T>> builder = new ImmutableMap.Builder<RunningNode, Function<RunningNode, T>>();
	for (RunningNode runningNode : runningNodes)
	    builder.put(runningNode, singleFunction);

	return builder.build();
    }

}
