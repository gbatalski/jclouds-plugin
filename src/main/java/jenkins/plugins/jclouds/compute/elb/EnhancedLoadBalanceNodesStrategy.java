/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;

import org.jclouds.domain.Location;
import org.jclouds.loadbalancer.strategy.LoadBalanceNodesStrategy;

import com.google.inject.ImplementedBy;

/**
 * @author gena
 *
 */
@ImplementedBy(EnhancedLoadBalanceNodesStrategyImpl.class)
public interface EnhancedLoadBalanceNodesStrategy extends LoadBalanceNodesStrategy {
    void createAppCookieStickinessPolicy(Location location, String loadBalancerName, String cookieName, String policyName);

    void setLoadBalancingPoliciesOfListener(Location location, String loadBalancerName, Integer loadBalancerPort,
	    String... policyNames);

}
