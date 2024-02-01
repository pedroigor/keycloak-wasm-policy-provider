package org.keycloak.authorization.policy;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.runtime.WasmFunctionHandle;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.wasm.types.ValueType;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.provider.PolicyProvider;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class WasmPolicyProvider implements PolicyProvider {

    public final static String WASM_FILE = "wasm-file";
    public final static String MODULE_NAME = "env";

    private static final Value[] TRUE_RES = new Value[]{Value.TRUE};
    private static final Value[] FALSE_RES = new Value[]{Value.FALSE};

    @Override
    public void evaluate(Evaluation evaluation) {
        EvaluationContext context = evaluation.getContext();
        Identity identity = context.getIdentity();
        Policy policy = evaluation.getPolicy();
        Map<String, String> config = policy.getConfig();

        if (config.containsKey(WASM_FILE)) {
            System.out.println("The selected wasm module is " + config.get(WASM_FILE));
        }

        // From Classpath:
        // Module module = Module.builder("wasm/" + config.get(WASM_FILE) + ".wasm").build();
        // From file to have fully dynamic loading:
        Module module = Module.builder(new File("src/main/resources/wasm/" + config.get(WASM_FILE) + ".wasm")).build();
        HostImports imports = new HostImports(
          new HostFunction[]{
                  new HostFunction(
                          evaluationGrant(evaluation),
                          MODULE_NAME,
                          "evaluation_grant",
                          List.of(),
                          List.of()),
                  new HostFunction(
                          evaluationDeny(evaluation),
                          MODULE_NAME,
                          "evaluation_deny",
                          List.of(),
                          List.of()),
                  new HostFunction(
                          identityHasRealmRole(identity),
                          MODULE_NAME,
                          "identity_has_realm_role",
                          List.of(ValueType.I32, ValueType.I32),
                          List.of(ValueType.I32)),
                  new HostFunction(
                          identityHasAttributeValue(identity),
                          MODULE_NAME,
                          "identity_has_attribute_value",
                          List.of(ValueType.I32, ValueType.I32),
                          List.of(ValueType.I32))
          }
        );
        Instance instance = module.instantiate(imports);
        ExportFunction evaluate = instance.export("evaluate");

        evaluate.apply();

//        // RBAC
//        if (identity.hasRealmRole("myrole")) {
//            evaluation.grant();
//        }
//
//        // ABAC
//        if (identity.getAttributes().containsValue("myplan", "premium")) {
//            evaluation.grant();
//        }
    }

    private WasmFunctionHandle evaluationGrant(Evaluation evaluation) {
        return (Instance instance, Value ... args) -> {
            System.out.println("FUNCTION: GRANT");
            evaluation.grant();
            return null;
        };
    }

    private WasmFunctionHandle evaluationDeny(Evaluation evaluation) {
        return (Instance instance, Value ... args) -> {
            System.out.println("FUNCTION: DENY");
            evaluation.deny();
            return null;
        };
    }

    private WasmFunctionHandle identityHasRealmRole(Identity identity) {
        return (Instance instance, Value ... args) -> {
            final int addr = args[0].asInt();
            final int size = args[1].asInt();

            String realmRole = instance.memory().readString(addr, size);
            System.out.println("FUNCTION: HAS REALM ROLE " + realmRole);
            if (identity.hasRealmRole(realmRole)) {
                return TRUE_RES;
            } else {
                return FALSE_RES;
            }
        };
    }

    private WasmFunctionHandle identityHasAttributeValue(Identity identity) {
        return (Instance instance, Value ... args) -> {
            final int keyAddr = args[0].asInt();
            final int keySize = args[1].asInt();
            final int valueAddr = args[2].asInt();
            final int valueSize = args[3].asInt();

            String key = instance.memory().readString(keyAddr, keySize);
            String value = instance.memory().readString(valueAddr, valueSize);
            System.out.println("FUNCTION: HAS ATTRIBUTE VALUE " + key + " : " + value);
            for (var x: identity.getAttributes().toMap().entrySet()) {
                System.out.println("DEBUG: " + x.getKey() + " - " + x.getValue());
            }

            if (identity.getAttributes().containsValue(key, value)) {
                return TRUE_RES;
            } else {
                return FALSE_RES;
            }
        };
    }

    @Override
    public void close() {
        // method called at the end of the request lifecycle
    }
}
