/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution;

import java.util.Map;

import jenkins.plugins.jclouds.compute.internal.RunningNode;

import com.google.common.base.Function;
import com.google.common.base.Supplier;



/**
 * @author gena
 *
 */
public abstract class ExecutorSupplier<T> implements Supplier<Map<RunningNode, Function<RunningNode, T>>> {
    protected final Iterable<RunningNode> runningNodes;

    public ExecutorSupplier(Iterable<RunningNode> runningNodes) {
	super();
	this.runningNodes = runningNodes;
    }

}
