package core.aws.task.as;

import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import core.aws.client.AWS;
import core.aws.env.Context;
import core.aws.resource.as.ASGroup;
import core.aws.workflow.Action;
import core.aws.workflow.Task;

/**
 * @author neo
 */
@Action("update-asg")
public class UpdateASGroupTask extends Task<ASGroup> {
    public UpdateASGroupTask(ASGroup asGroup) {
        super(asGroup);
    }

    @Override
    public void execute(Context context) throws Exception {
        ASGroupHelper helper = new ASGroupHelper(context.env);
        String asGroupName = resource.remoteASGroup.getAutoScalingGroupName();

        String oldLaunchConfigName = null;
        if (resource.launchConfig.changed()) {
            oldLaunchConfigName = resource.remoteASGroup.getLaunchConfigurationName();
            helper.createLaunchConfig(resource);

            AWS.as.updateTag(asGroupName, helper.nameTag(resource));
        }

        AWS.as.updateASGroup(new UpdateAutoScalingGroupRequest()
            .withAutoScalingGroupName(asGroupName)
            .withLaunchConfigurationName(resource.launchConfig.remoteLaunchConfig.getLaunchConfigurationName())
            .withTerminationPolicies(ASGroup.TERMINATE_POLICY_OLDEST_INSTANCE)
            .withDesiredCapacity(resource.desiredSize)
            .withMinSize(resource.minSize)
            .withMaxSize(resource.maxSize));

        if (oldLaunchConfigName != null) {
            AWS.as.deleteLaunchConfig(oldLaunchConfigName);
        }
    }
}
