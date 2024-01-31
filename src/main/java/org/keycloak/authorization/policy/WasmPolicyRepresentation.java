package org.keycloak.authorization.policy;

import org.keycloak.representations.idm.authorization.AbstractPolicyRepresentation;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class WasmPolicyRepresentation extends AbstractPolicyRepresentation {

    private String code;

    @Override
    public String getType() {
        return "wasm-policy-provider";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
