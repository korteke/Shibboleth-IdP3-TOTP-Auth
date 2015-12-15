[![License](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/korteke/Shibboleth-IdP3-TOTP-Auth/blob/master/LICENSE)

# Shibboleth-IdP3-TOTP-Auth
Google authenticator authentication module for Shibboleth IdP v3.

Requirements
------------

Shibboleth IdP v3.2.0
Java 7

Installing
----------

Compile, copy and extract totpauth-parent/totpauth-impl/target/totpauth-impl-0.5.0-bin.zip

Directory structure:
<pre>
├── conf
│   └── authn
├── edit-webapp
│   └── WEB-INF
│       └── lib
├── flows
│   └── authn
│       └── Totp
└── views
</pre>

Copy conf --> $IDP_HOME/conf  
Copy edit-webapp  --> $IDP_HOME/edit-webapp  
Copy flows  --> $IDP_HOME/flows  
Copy views  --> $IDP_HOME/views  

Modify $IDP_HOME/conf/idp.properties  

idp.authn.flows = Password --> idp.authn.flows = Totp

Add TOTP bean to $IDP_HOME/conf/authn/general-authn.xml, element "<util:list id="shibboleth.AvailableAuthenticationFlows">":

<pre>
```
<bean id="authn/Totp" parent="shibboleth.AuthenticationFlow"
    p:passiveAuthenticationSupported="false"
    p:forcedAuthenticationSupported="true" />
```
</pre>

Seed Fetching
-------------

TBD
