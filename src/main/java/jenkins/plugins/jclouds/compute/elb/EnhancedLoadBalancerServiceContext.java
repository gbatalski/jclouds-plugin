/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;


import org.jclouds.loadbalancer.LoadBalancerServiceContext;

import com.google.inject.ImplementedBy;

/**
 * @author gena
 *
 */
@ImplementedBy(EnhancedLoadBalancerServiceContextImpl.class)
public interface EnhancedLoadBalancerServiceContext extends LoadBalancerServiceContext {
    EnhancedLoadBalancerService getLoadBalancerService();
}
