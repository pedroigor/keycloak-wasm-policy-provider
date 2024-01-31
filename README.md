## Keycloak WASM Policy provider

This repository provives an example about how to use WASM to write a policy.

## Running

After cloning the repository, you can run the [Main](src/main/java/Main.java) application to start the server.

When starting the server for the first time, make sure to create the admin user first as:

* Username: `admin`
* Password: `admin`

Then re-start the application.

You can also set the `KEYCLOAK_ADMIN` and `KEYCLOAK_ADMIN_PASSWORD` environment variables to automatically create the admin user.

## Testing the WASM Policy Provider

Open the administration console at `http://localhost:8080/admin/master/console/#/myrealm/clients` and then:

* Select the `myclient` client
* Click on the `Authorization` tab
* Click on the `Evaluate` sub-tab
* Select the `alice` users in the `Users` field
* Click the `Evaluate` button at the bottom of the page

By doing that, you'll be running the [WasmPolicyProvider](src/main/java/org/keycloak/authorization/policy/WasmPolicyProvider.java) policy provider.

## Configuring the WASM Policy Provider

In the application class, add whatever configuration you want to the policy config map.

## Debugging

If you are starting the application in debug mode from your IDE, you should be able to debug the [WasmPolicyProvider](src/main/java/org/keycloak/authorization/policy/WasmPolicyProvider.java) policy provider.
