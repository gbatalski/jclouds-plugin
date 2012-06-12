package jenkins.plugins.jclouds.compute.internal;

import hudson.model.labels.LabelAtom;

import java.util.Set;

import org.jclouds.compute.domain.NodeMetadata;

public class RunningNode {
   private final String cloud;
   private final String template;
   private final boolean suspendOrTerminate;
   private final NodeMetadata node;
    private final boolean registerAsSlave;
    private final boolean keepAlive;
    private final Set<LabelAtom> labelSet;

    public RunningNode(String cloud, String template, boolean suspendOrTerminate, NodeMetadata node, Set<LabelAtom> labelSet,
	    boolean registerAsSlave, boolean keepAlive) {
      this.cloud = cloud;
      this.template = template;
      this.suspendOrTerminate = suspendOrTerminate;
      this.node = node;
	this.labelSet = labelSet;
	this.registerAsSlave = registerAsSlave;
	this.keepAlive = keepAlive;
   }

   public String getCloudName() {
      return cloud;
   }

   public String getTemplateName() {
      return template;
   }

   public boolean isSuspendOrTerminate() {
      return suspendOrTerminate;
   }

   public NodeMetadata getNode() {
      return node;
   }

    public Set<LabelAtom> getLabelSet() {
	return labelSet;
    }

    public String getCloud() {
	return cloud;
    }

    public String getTemplate() {
	return template;
    }

    public boolean isRegisterAsSlave() {
	return registerAsSlave;
    }

    public boolean isKeepAlive() {
	return keepAlive;
    }
}