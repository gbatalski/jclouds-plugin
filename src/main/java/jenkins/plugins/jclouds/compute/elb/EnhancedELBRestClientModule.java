/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;

import org.jclouds.aws.config.FormSigningRestClientModule;
import org.jclouds.rest.ConfiguresRestClient;

/**
 * @author gena
 *
 */
@ConfiguresRestClient
public class EnhancedELBRestClientModule extends FormSigningRestClientModule<EnhancedELBClient, EnhancedELBAsyncClient> {

}
