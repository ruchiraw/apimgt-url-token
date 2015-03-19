# API Manager Access Token as a URL Param

Add this as the top most handler in the API.

`<handler class="org.wso2.carbon.apimgt.extensions.auth.URLTokenInjector"/>`

Token can be specified as `/facebook/1.0.0?access_token=123456`

If you don't like the param name, it can be changed by specifying "urlTokenParam" system property.

i.e. If following is used,

`sh bin/wso2server.sh -DurlTokenParam=token`

then API can be invoked as `/facebook/1.0.0?token=123456`
