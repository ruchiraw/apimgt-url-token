# API Manager Access Token as a URL Param

This handler allows you to specify the OAuth 2 access token of an API invocation as a URL parameter.

1. Copy libs/org.wso2.carbon.apimgt.extensions-1.0.0-SNAPSHOT.jar into <API_MANAGER>/repository/components/lib directory
2. Restart the server
3. Add following handler as the top most handler in the API or API template
	`<handler class="org.wso2.carbon.apimgt.extensions.auth.URLTokenInjector"/>`

## Sending the Token

Token can be specified as `/facebook/1.0.0?access_token=123456`.

If you don't like the param name, it can be changed by specifying "urlTokenParam" system property.

i.e. If `sh bin/wso2server.sh -DurlTokenParam=token` is used, then API can be invoked as `/facebook/1.0.0?token=123456`
