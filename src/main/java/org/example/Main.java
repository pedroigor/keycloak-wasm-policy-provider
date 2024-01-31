package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.Keycloak;
import org.keycloak.admin.client.resource.AuthorizationResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.util.JsonSerialization;

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
            org.keycloak.admin.client.Keycloak adminClient = org.keycloak.admin.client.Keycloak.getInstance("http://localhost:8080", "master", "admin", "admin", "admin-cli");
            RealmResource realm = adminClient.realm("myrealm");
            ClientRepresentation myclient = realm.clients().findByClientId("myclient").get(0);
            AuthorizationResource authorization = realm.clients().get(myclient.getId()).authorization();
            configureWasmPolicy(authorization);
        } catch (Exception e) {
            System.out.println("Go to http://localhost:8080 and create the admin user");
        }
    }

    private static void configureWasmPolicy(AuthorizationResource authorization) {
        PolicyRepresentation wasmPolicy = authorization.policies().findByName("My WASM Policy");
        Map<String, String> config = wasmPolicy.getConfig();

        config.put("myconfig", "somevalue");

        authorization.policies().policy(wasmPolicy.getId()).update(wasmPolicy);
    }

    private static RealmResource createRealm(org.keycloak.admin.client.Keycloak adminClient) {
        RealmsResource realms = adminClient.realms();

        try {
            RealmResource realm = realms.realm("myrealm");
            realm.toRepresentation();
            realm.remove();
        } catch (NotFoundException nfe) {
            createRealm(adminClient);
        }

        try {
            realms.create(JsonSerialization.readValue(Main.class.getClassLoader().getResourceAsStream("realms.json"), RealmRepresentation.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create realm");
        }

        return realms.realm("myrealm");
    }
}