/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.chef;

import static com.google.common.base.Preconditions.checkArgument;
import jenkins.plugins.jclouds.compute.JCloudsCloud;

import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.scriptbuilder.domain.Statements;

import com.google.common.base.Predicate;
import static java.lang.String.*;
/**
 * @author gena
 *
 */
public class ListeningOnPort extends Object implements Predicate<Integer> {

    public ListeningOnPort(NodeMetadata nodeMetadata, String cloudName) {
	super();
	this.nodeMetadata = nodeMetadata;
	this.cloudName = cloudName;
    }

    private final NodeMetadata nodeMetadata;

    private final String cloudName;

    public boolean apply(Integer port) {
	checkArgument(isValidPort(port));
	String launchString = format("netstat -ant | grep %d", port);

	ExecResponse response = JCloudsCloud.getByName(cloudName).getCompute()
		.runScriptOnNode(nodeMetadata.getId(), Statements.exec(launchString));
	return response.getOutput().contains("LISTEN");

    }

    /** Return true for valid port numbers. */
    private static boolean isValidPort(int port) {
	return port >= 0 && port <= 65535;
    }
}
