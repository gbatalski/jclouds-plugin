package jenkins.plugins.jclouds.compute.internal;

import hudson.model.labels.LabelAtom;

import java.util.Set;

import org.jclouds.compute.domain.NodeMetadata;

public class RunningNode {
   private final String cloud;
   private final String template;
   private final boolean suspendOrTerminate;
   private final NodeMetadata node;
    private final Set<LabelAtom> labelSet;

    public RunningNode(String cloud, String template, boolean suspendOrTerminate, NodeMetadata node, Set<LabelAtom> labelSet) {
      this.cloud = cloud;
      this.template = template;
      this.suspendOrTerminate = suspendOrTerminate;
      this.node = node;
	this.labelSet = labelSet;
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
}