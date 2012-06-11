/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;


import org.jclouds.elb.ELBAsyncClient;
import org.jclouds.elb.ELBClient;
import org.jclouds.elb.loadbalancer.strategy.ELBLoadBalanceNodesStrategy;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.LoadBalancerServiceContext;


import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author gena
 *
 */
public class EnhancedLoadBalancerServiceModule implements Module {

    /* (non-Javadoc)
     * @see com.google.inject.Module#configure(com.google.inject.Binder)
     */
    @Override
    public void configure(Binder binder) {
	binder.bind(LoadBalancerService.class).to(EnhancedLoadBalancerService.class);
	binder.bind(EnhancedLoadBalancerService.class).to(EnhancedBaseLoadBalancerService.class);
	binder.bind(ELBClient.class).to(EnhancedELBClient.class);
	binder.bind(ELBAsyncClient.class).to(EnhancedELBAsyncClient.class);
	binder.bind(LoadBalancerServiceContext.class).to(EnhancedLoadBalancerServiceContext.class);
	binder.bind(ELBLoadBalanceNodesStrategy.class).to(EnhancedLoadBalanceNodesStrategyImpl.class);

    }

}
