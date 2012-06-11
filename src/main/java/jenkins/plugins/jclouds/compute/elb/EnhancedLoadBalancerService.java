/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;

import org.jclouds.domain.Location;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.loadbalancer.LoadBalancerService;

/**
 * @author gena
 *
 */
public interface EnhancedLoadBalancerService extends LoadBalancerService {

    void createAppCookieStickinessPolicy(@Nullable Location location, String loadBalancerName, String cookieName,
	    String policyName);

    void setLoadBalancingPoliciesOfListener(@Nullable Location location, String loadBalancerName, Integer loadBalancerPort,
	    String... policyNames);
}
