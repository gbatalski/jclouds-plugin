/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.BuildableItemWithBuildWrappers;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.util.DescribableList;

import java.io.IOException;
import java.util.Collections;

import jenkins.plugins.jclouds.compute.JCloudsBuildWrapper;
import jenkins.plugins.jclouds.compute.JCloudsSlave;
import jenkins.plugins.jclouds.compute.internal.RunningNode;
import jenkins.plugins.jclouds.internal.BuildListenerLogger;

import org.jclouds.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * @author gena
 *
 */
public abstract class JCloudsEnabledBuilder<T> extends Builder {


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException,
	    IOException {
	// converting to a logger as it is an interface and easier to test
	final Logger logger = new BuildListenerLogger(listener);
	// find jclouds nodes
	if (build.getProject() instanceof BuildableItemWithBuildWrappers) {
	    BuildableItemWithBuildWrappers currentBuild = (BuildableItemWithBuildWrappers) build.getProject();
	    DescribableList<BuildWrapper, Descriptor<BuildWrapper>> buildWrappers = currentBuild.getBuildWrappersList();
	    JCloudsBuildWrapper jCloudsBuildWrapper = buildWrappers.get(JCloudsBuildWrapper.class);
	    Iterable<RunningNode> runningNodes = Collections.emptyList();
	    if (jCloudsBuildWrapper == null) {
		Node node = Computer.currentComputer().getNode();
		if (node instanceof JCloudsSlave) {
		    JCloudsSlave jcloudsSlave = (JCloudsSlave) node;
		    runningNodes = ImmutableList.<RunningNode> of(new RunningNode(jcloudsSlave.getCloudName(), jcloudsSlave
			    .getNodeMetaData().getName(), false, jcloudsSlave.getNodeMetaData()));
		}

	    } else {
		runningNodes = jCloudsBuildWrapper.getRunningNodes();
	    }
	    if (runningNodes.iterator().hasNext()) {
		build(MoreExecutors.listeningDecorator(Computer.threadPoolForRemoting), runningNodes, logger);
	    }
	}

	return true;
    }

    /**
     * @param listeningDecorator
     * @param runningNodes
     * @param logger
     */
    protected abstract void build(ListeningExecutorService listeningDecorator, Iterable<RunningNode> runningNodes, Logger logger);

}
