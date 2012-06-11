/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.aws.util.AWSUtils.getRegionFromLocationOrNull;
import org.jclouds.domain.Location;
import org.jclouds.elb.domain.LoadBalancer;
import org.jclouds.elb.loadbalancer.strategy.ELBLoadBalanceNodesStrategy;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author gena
 *
 */
@Singleton
public class EnhancedLoadBalanceNodesStrategyImpl extends ELBLoadBalanceNodesStrategy implements EnhancedLoadBalanceNodesStrategy {


    /**
     * @param client
     * @param converter
     */
    @Inject
    protected EnhancedLoadBalanceNodesStrategyImpl(EnhancedELBClient client, Function<LoadBalancer, LoadBalancerMetadata> converter) {
	super(client, converter);
    }

    /* (non-Javadoc)
     * @see jenkins.plugins.jclouds.compute.elb.EnhancedLoadBalanceNodesStrategy#createAppCookieStickinessPolicy(org.jclouds.domain.Location, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void createAppCookieStickinessPolicy(Location location, String loadBalancerName, String cookieName, String policyName) {
	checkNotNull(location, "location");
	String region = getRegionFromLocationOrNull(location);
	((EnhancedELBClient) client).createAppCookieStickinessPolicy(region, loadBalancerName, cookieName, policyName);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jenkins.plugins.jclouds.compute.elb.EnhancedLoadBalanceNodesStrategy#
     * setLoadBalancingPoliciesOfListener(org.jclouds.domain.Location,
     * java.lang.String, java.lang.Integer, java.lang.String[])
     */
    @Override
    public void setLoadBalancingPoliciesOfListener(Location location, String loadBalancerName, Integer loadBalancerPort,
	    String... policyNames) {
	checkNotNull(location, "location");
	String region = getRegionFromLocationOrNull(location);
	((EnhancedELBClient) client).setLoadBalancingPoliciesOfListener(region, loadBalancerName, loadBalancerPort, policyNames);

    }

}
