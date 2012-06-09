/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution;

import com.google.common.base.Function;

import jenkins.plugins.jclouds.compute.internal.RunningNode;

/**
 * @author gena
 *
 */
public interface ChefExecutorStrategy<T> extends Function<Iterable<RunningNode>, T> {

    T apply(Iterable<RunningNode> input);

}