package org.keycloak.authorization.policy;

import org.keycloak.Config.Scope;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class WasmPolicyProviderFactory implements PolicyProviderFactory<WasmPolicyRepresentation> {

    @Override
    public PolicyProvider create(AuthorizationProvider authorizationProvider) {
        return new WasmPolicyProvider();
    }

    @Override
    public Class<WasmPolicyRepresentation> getRepresentationType() {
        return WasmPolicyRepresentation.class;
    }

    @Override
    public WasmPolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        return new WasmPolicyRepresentation();
    }

    @Override
    public void init(Scope scope) {
        // called during startup
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // called during startup, after init
    }

    @Override
    public void close() {
        // called on shutdown
    }

    @Override
    public String getId() {
        return "wasm-policy-provider";
    }

    @Override
    public String getName() {
        return "WASM";
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public PolicyProvider create(KeycloakSession keycloakSession) {
        return null;
    }
}
