/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder;

/**
 * @author gena
 *
 */
public class LoadBalancer {
    public LoadBalancer(String cloudName, String loadBalancerName, String protocol, int loadBalancerPort, int instancePort,
	    boolean sticky, String cookieName) {
	super();
	this.loadBalancerName = loadBalancerName;
	this.protocol = protocol;
	this.loadBalancerPort = loadBalancerPort;
	this.instancePort = instancePort;
	this.cloudName = cloudName;
	this.sticky = sticky;
	this.cookieName = cookieName;
    }

    public final String cloudName;
    public final String loadBalancerName;
    public final String protocol;
    public final int loadBalancerPort;
    public final int instancePort;
    public final boolean sticky;
    public final String cookieName;
}
