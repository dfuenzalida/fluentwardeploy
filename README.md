### Fluent WAR deploy

Programatically deploy a WAR file to Azure App Service to run in Tomcat.

Based on https://docs.microsoft.com/en-us/azure/developer/java/sdk/get-started

### Usage

Select the subscription you will use:

```
az account set --subscription 7eabdee9-c6b5-4546-8ca8-3a42303f9972
```

Create a service principal with

```
az ad sp create-for-rbac --name FluentWarDeploy
```

the response will be like the following:

```javascript
{
  "appId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "displayName": "FluentWarDeploy",
  "name": "http://FluentWarDeploy",
  "password": "SOME-RANDOM-STRING",
  "tenant": "tttttttt-tttt-tttt-tttt-tttttttttttt"
}
```

Set the environment variables for authentication. In PowerShell, use the following syntax:

```
$env:AZURE_CLIENT_ID='aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
$env:AZURE_TENANT_ID='tttttttt-tttt-tttt-tttt-tttttttttttt'
$env:AZURE_CLIENT_SECRET='SOME-RANDOM-STRING'
```

Now you can run the example with:

```
mvn compile exec:java
```

### License

MIT