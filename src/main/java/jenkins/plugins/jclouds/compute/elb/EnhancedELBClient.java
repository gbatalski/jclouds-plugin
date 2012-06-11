/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;

import java.util.concurrent.TimeUnit;

import org.jclouds.concurrent.Timeout;
import org.jclouds.elb.ELBClient;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.annotations.Beta;

/**
 * @author gena
 *
 */
@Beta
// see ELBAsyncClient
@Timeout(duration = 30, timeUnit = TimeUnit.SECONDS)
public interface EnhancedELBClient extends ELBClient {
    /**
     * Generates a stickiness policy with sticky session on given load balancer
     *
     * @see <a href=
     *      "http://docs.amazonwebservices.com/ElasticLoadBalancing/2009-05-15/DeveloperGuide/"
     *
     * @param region
     * @param name
     *            Name of the load balancer
     * @param cookieName
     *            App Cookie name
     * @param policyName
     *            Policy name
     */
    void createAppCookieStickinessPolicy(@Nullable String region, String name, String cookieName, String policyName);

    /**
     * Enables policies on given load balancer for given port
     *
     * @see <a href=
     *      "http://docs.amazonwebservices.com/ElasticLoadBalancing/2009-05-15/DeveloperGuide/"
     *
     * @param region
     * @param name
     *            Name of the load balancer
     * @param loadBalancerPort
     *            Port to enable policies on
     * @param ppolicyNames
     *            Policies names
     */
    void setLoadBalancingPoliciesOfListener(@Nullable String region, String name, Integer loadBalancerPort, String... policyNames);
}
