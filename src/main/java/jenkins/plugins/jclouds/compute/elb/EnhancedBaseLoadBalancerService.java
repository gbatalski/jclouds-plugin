/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.jclouds.collect.Memoized;
import org.jclouds.domain.Location;
import org.jclouds.loadbalancer.LoadBalancerServiceContext;
import org.jclouds.loadbalancer.internal.BaseLoadBalancerService;
import org.jclouds.loadbalancer.strategy.DestroyLoadBalancerStrategy;
import org.jclouds.loadbalancer.strategy.GetLoadBalancerMetadataStrategy;
import org.jclouds.loadbalancer.strategy.ListLoadBalancersStrategy;
import org.jclouds.loadbalancer.strategy.LoadBalanceNodesStrategy;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author gena
 *
 */
@Singleton
public class EnhancedBaseLoadBalancerService extends BaseLoadBalancerService implements EnhancedLoadBalancerService {

    /**
     * @param defaultLocationSupplier
     * @param context
     * @param loadBalancerStrategy
     * @param getLoadBalancerMetadataStrategy
     * @param destroyLoadBalancerStrategy
     * @param listLoadBalancersStrategy
     * @param locations
     */
    @Inject
    protected EnhancedBaseLoadBalancerService(Supplier<Location> defaultLocationSupplier, LoadBalancerServiceContext context,
	    LoadBalanceNodesStrategy loadBalancerStrategy, GetLoadBalancerMetadataStrategy getLoadBalancerMetadataStrategy,
	    DestroyLoadBalancerStrategy destroyLoadBalancerStrategy, ListLoadBalancersStrategy listLoadBalancersStrategy,
	    @Memoized Supplier<Set<? extends Location>> locations) {
	super(defaultLocationSupplier, context, loadBalancerStrategy, getLoadBalancerMetadataStrategy, destroyLoadBalancerStrategy,
		listLoadBalancersStrategy, locations);

    }

    /*
     * (non-Javadoc)
     *
     * @see jenkins.plugins.jclouds.compute.elb.EnhancedLoadBalancerService#
     * createAppCookieStickinessPolicy(org.jclouds.domain.Location,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void createAppCookieStickinessPolicy(Location location, String loadBalancerName, String cookieName, String policyName) {

	if (location == null)
	    location = defaultLocationSupplier.get();

	checkNotNull(loadBalancerName, "loadBalancerName");
	checkNotNull(cookieName, "cookieName");

	logger.debug(">> adding application cookie stickness policy to load balancer (%s) with cookie (%s)", loadBalancerName,
		cookieName);
	((EnhancedLoadBalanceNodesStrategy) loadBalancerStrategy).createAppCookieStickinessPolicy(location, loadBalancerName,
		cookieName, policyName);

	logger.debug("<< application stickness policy added to load balancer (%s)", loadBalancerName);

    }

    /*
     * (non-Javadoc)
     *
     * @see jenkins.plugins.jclouds.compute.elb.EnhancedLoadBalancerService#
     * setLoadBalancingPoliciesOfListener(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public void setLoadBalancingPoliciesOfListener(Location location, String loadBalancerName, Integer loadBalancerPort,
	    String... policyNames) {
	if (location == null)
	    location = defaultLocationSupplier.get();

	checkNotNull(loadBalancerName, "loadBalancerName");
	checkNotNull(loadBalancerPort, "loadBalancerPort");

	logger.debug(">> setting up policies %s of load balancer %s ", Joiner.on(", ").join(policyNames), loadBalancerName);
	((EnhancedLoadBalanceNodesStrategy) loadBalancerStrategy).setLoadBalancingPoliciesOfListener(location, loadBalancerName,
		loadBalancerPort, policyNames);

	logger.debug("<< policies %s set up on load balancer %s", Joiner.on(", ").join(policyNames), loadBalancerName);

    }

}
