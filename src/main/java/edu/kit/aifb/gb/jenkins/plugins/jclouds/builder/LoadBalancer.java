/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder;

/**
 * @author gena
 *
 */
public class LoadBalancer {
    public LoadBalancer(String cloudName, String loadBalancerName, String protocol, int loadBalancerPort, int instancePort) {
	super();
	this.loadBalancerName = loadBalancerName;
	this.protocol = protocol;
	this.loadBalancerPort = loadBalancerPort;
	this.instancePort = instancePort;
	this.cloudName = cloudName;
    }

    public final String cloudName;
    public final String loadBalancerName;
    public final String protocol;
    public final int loadBalancerPort;
    public final int instancePort;
}
