import java.nio.file.Path;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.keycloak.Keycloak;
import org.keycloak.admin.client.resource.AuthorizationResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

import static org.keycloak.authorization.policy.WasmPolicyProvider.WASM_FILE;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class Main {

    public static void main(String[] args) {
        Keycloak.builder()
                .setHomeDir(Path.of("kc-dev"))
                .setVersion("23.0.5")
                .addDependency("org.keycloak", "keycloak-wasm-policy-provider", "0.0.1-SNAPSHOT")
                .start("start-dev", "--import-realm", "--cache=local");

        try {
            org.keycloak.admin.client.Keycloak adminClient = createAdminClient();
            RealmResource realm = adminClient.realm("myrealm");
            ClientRepresentation client = realm.clients().findByClientId("myclient").get(0);
            AuthorizationResource authorization = realm.clients().get(client.getId()).authorization();
            configureWasmPolicy(authorization);
        } catch (Exception e) {
            System.out.println("Go to http://localhost:8080 and create the admin user");
        }
    }

    /**
     * Configure the WASM policy by adding key/value pairs to the config map.
     */
    private static void configureWasmPolicy(AuthorizationResource authorization) {
        PolicyRepresentation wasmPolicy = authorization.policies().findByName("My WASM Policy");
        Map<String, String> config = wasmPolicy.getConfig();

        // at the moment either rbac or abac
        // config.put(WASM_FILE, "rbac");
        config.put(WASM_FILE, "abac");

        authorization.policies().policy(wasmPolicy.getId()).update(wasmPolicy);
    }

    private static org.keycloak.admin.client.Keycloak createAdminClient() {
        return org.keycloak.admin.client.Keycloak.getInstance("http://localhost:8080", "master", "admin", "admin", "admin-cli");
    }
}