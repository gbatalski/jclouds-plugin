package jenkins.plugins.jclouds.compute.internal;

import hudson.model.labels.LabelAtom;

import java.util.Set;

import org.jclouds.compute.domain.NodeMetadata;

import com.google.common.base.Supplier;

public class NodePlan {
   private final String cloudName;
   private final String templateName;
   private final int count;
   private final boolean suspendOrTerminate;
   private final boolean registerAsSlave;
   private final boolean keepAlive;
   private final Supplier<NodeMetadata> nodeSupplier;
   private final Set<LabelAtom> labelSet;

   public NodePlan(String cloud, String template, int count, boolean suspendOrTerminate,
            Supplier<NodeMetadata> nodeSupplier,Set<LabelAtom> labelSet,boolean registerAsSlave, boolean keepAlive) {
      this.cloudName = cloud;
      this.templateName = template;
      this.count = count;
      this.suspendOrTerminate = suspendOrTerminate;
      this.nodeSupplier = nodeSupplier;
      this.labelSet = labelSet;
      this.registerAsSlave = registerAsSlave;
	this.keepAlive = keepAlive;
   }

   public String getCloudName() {
      return cloudName;
   }

   public String getTemplateName() {
      return templateName;
   }

   public int getCount() {
      return count;
   }

   public boolean isSuspendOrTerminate() {
      return suspendOrTerminate;
   }

   public Supplier<NodeMetadata> getNodeSupplier() {
      return nodeSupplier;
   }

    public Set<LabelAtom> getLabelSet() {
	return labelSet;
    }

    public boolean isRegisterAsSlave() {
	return registerAsSlave;
    }

    public boolean isKeepAlive() {
	return keepAlive;
    }
}