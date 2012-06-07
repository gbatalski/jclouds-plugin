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
   private final Supplier<NodeMetadata> nodeSupplier;
    private final Set<LabelAtom> labelSet;

   public NodePlan(String cloud, String template, int count, boolean suspendOrTerminate,
            Supplier<NodeMetadata> nodeSupplier,Set<LabelAtom> labelSet) {
      this.cloudName = cloud;
      this.templateName = template;
      this.count = count;
      this.suspendOrTerminate = suspendOrTerminate;
      this.nodeSupplier = nodeSupplier;
	this.labelSet = labelSet;
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
}