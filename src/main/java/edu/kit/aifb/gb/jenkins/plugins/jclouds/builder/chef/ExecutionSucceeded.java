/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef;

import org.jclouds.compute.domain.ExecResponse;

import com.google.common.base.Predicate;
import static com.google.common.base.Preconditions.*;


/**
 * @author gena
 *
 */
public class ExecutionSucceeded implements Predicate<ExecResponse> {

    @Override
    public boolean apply(ExecResponse input) {
	checkNotNull(input, "Result shouldn't be null, something went wrong...");
	return input.getExitStatus() == 0;
    }

}
