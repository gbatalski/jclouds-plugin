/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;

import static org.jclouds.aws.util.AWSUtils.indexStringArrayToFormValuesWithStringFormat;


import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;

/**
 * @author gena
 *
 */
public class BindPolicyNamesToIndexedFormParams implements Binder {

    @Override
    public <R extends HttpRequest> R bindToRequest(R request, Object input) {
	return indexStringArrayToFormValuesWithStringFormat(request, "PolicyNames.member.%s", input);
    }
}
