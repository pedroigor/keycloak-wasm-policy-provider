package org.keycloak.authorization.policy;

import java.util.Map;

import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.provider.PolicyProvider;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class WasmPolicyProvider implements PolicyProvider {

    @Override
    public void evaluate(Evaluation evaluation) {
        EvaluationContext context = evaluation.getContext();
        Identity identity = context.getIdentity();
        Policy policy = evaluation.getPolicy();
        Map<String, String> config = policy.getConfig();

        if (config.containsKey("myconfig")) {
            System.out.println("There is a config for this policy");
        }

        // RBAC
        if (identity.hasRealmRole("myrole")) {
            evaluation.grant();
        }

        // ABAC
        if (identity.getAttributes().containsValue("myplan", "premium")) {
            evaluation.grant();
        }
    }

    @Override
    public void close() {
        // method called at the end of the request lifecycle
    }
}
