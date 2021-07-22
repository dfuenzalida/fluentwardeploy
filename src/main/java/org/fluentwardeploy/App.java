package org.fluentwardeploy;

import java.io.File;

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

public class App {
    public static void main(String[] args) throws Exception {

        TokenCredential credential = new EnvironmentCredentialBuilder()
            .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
            .build();

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        AzureResourceManager azureResourceManager = AzureResourceManager.configure()
            .withLogLevel(HttpLogDetailLevel.BASIC)
            .authenticate(credential, profile)
            .withDefaultSubscription();

        final ResourceGroup resourceGroup = azureResourceManager.resourceGroups().define("fluentwardeploy-rg")
            .withRegion(Region.US_WEST2)
            .create();

        final WebApp webApp = azureResourceManager.webApps().define("fluentwardeploy-app")
            .withRegion(Region.US_WEST2)
            .withExistingResourceGroup(resourceGroup)
            .withNewLinuxPlan(PricingTier.PREMIUM_P1V2)
            .withBuiltInImage(RuntimeStack.TOMCAT_8_5_JAVA11)
            .create();
        
        // File to be deployed
        webApp.deploy(DeployType.WAR, new File("/temp/pet-clinic-war/target/spring-petclinic-2.3.1.BUILD-SNAPSHOT.war"));

        // System.out.println("Waiting before restart...");
        // Thread.sleep(3 * 1000);
        // System.out.println("Restarting...");

        webApp.restart();

        }
}
