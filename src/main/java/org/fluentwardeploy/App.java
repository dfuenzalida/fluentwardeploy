package org.fluentwardeploy;

import java.io.File;
import java.util.Date;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import reactor.core.publisher.Mono;

public class App {
    public static void main(String[] args) throws Exception {

        // Configuration
        String warFilePath = "/temp/pet-clinic-war/target/spring-petclinic-2.3.1.BUILD-SNAPSHOT.war";
        Region myRegion = Region.CANADA_CENTRAL;
        String suffix = "" + (100 + (int) (Math.random() * 1000)); // 3-digit suffix to avoid name collisions
        String resourceGroupName = "fluentwardeploy" + suffix + "-rg";
        String webAppName = "fluentwardeploy-app" + suffix;

        // Create resource group and web app
        TokenCredential credential = new EnvironmentCredentialBuilder()
            .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
            .build();

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        AzureResourceManager azureResourceManager = AzureResourceManager.configure()
            .withLogLevel(HttpLogDetailLevel.BASIC)
            .authenticate(credential, profile)
            .withDefaultSubscription();

        final ResourceGroup resourceGroup = azureResourceManager.resourceGroups().define(resourceGroupName)
            .withRegion(myRegion)
            .create();

        final WebApp webApp = azureResourceManager.webApps().define(webAppName)
            .withRegion(myRegion)
            .withExistingResourceGroup(resourceGroup)
            .withNewLinuxPlan(PricingTier.PREMIUM_P1V3)       // This SKU is required to deploy JBoss
            .withBuiltInImage(RuntimeStack.JBOSS_EAP_7_JAVA8) // NOTE: Currently maps to JBoss 7.4.2
            .create();
        
        // Async deployment API
        Mono<Void> deployment = webApp.deployAsync(DeployType.WAR, new File(warFilePath));

        // Deploy and block, then restart and block
        System.out.println("*** Deployment started at " + new Date());
        deployment.block();
        System.out.println("*** Deployment finished at " + new Date());

        System.out.println("*** Restarting at " + new Date());
        webApp.restartAsync().block();
        System.out.println("*** Restarting finished at " + new Date());
        System.out.println("*** Browse the app at https://" + webAppName + ".azurewebsites.net/");

        }
}
