/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution;

import jenkins.plugins.jclouds.compute.internal.RunningNode;

/**
 *
 * DTO representing a single cassandra node
 *
 * @author gena
 *
 */
public class CassandraNode {

	public CassandraNode(String listenAddress, String rpcAddress, String seeds,
 RunningNode delegate, boolean autoBootstrap,
	    String initialToken) {
		super();
		this.listenAddress = listenAddress;
		this.rpcAddress = rpcAddress;
		this.seeds = seeds;
		this.delegate = delegate;
	this.autoBootstap = autoBootstrap;
	this.initialToken = initialToken;
	}

    private final String listenAddress;
    private final String rpcAddress;
    private final String seeds;
    private final RunningNode delegate;
    private final boolean autoBootstap;
    private final String initialToken;

    public String getListenAddress() {
	return listenAddress;
    }

    public String getRpcAddress() {
	return rpcAddress;
    }

    public String getSeeds() {
	return seeds;
    }

    public RunningNode getDelegate() {
	return delegate;
    }

    public boolean isAutoBootstap() {
	return autoBootstap;
    }

    public String getInitialToken() {
	return initialToken;
    }

}
