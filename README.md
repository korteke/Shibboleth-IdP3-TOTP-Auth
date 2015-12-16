[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg)](https://github.com/korteke/Shibboleth-IdP3-TOTP-Auth/blob/master/LICENSE)

# Shibboleth-IdP3-TOTP-Auth
Google authenticator authentication module for Shibboleth IdP v3.  
Work in progress. This is the first "working" implementation. 

Uses External LDAP, MongoDB(EXPERIMENTAL!) or Static for seed fetching

Requirements
------------

Shibboleth IdP v3.2.x  
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

* Copy conf --> $IDP-HOME/conf  
* Copy edit-webapp  --> $IDP-HOME/edit-webapp  
* Copy flows  --> $IDP-HOME/flows  
* Copy views  --> $IDP-HOME/views  

Modify $IDP_HOME/conf/idp.properties  

idp.authn.flows = Password --> idp.authn.flows = Password|Totp

Add TOTP bean to $IDP_HOME/conf/authn/general-authn.xml, element
```
 "<util:list id="shibboleth.AvailableAuthenticationFlows">"
```
  
```
        <bean id="authn/Totp" parent="shibboleth.AuthenticationFlow"
                p:passiveAuthenticationSupported="true"
                p:forcedAuthenticationSupported="true">
            <property name="supportedPrincipals">
                <util:list>
                    <bean parent="shibboleth.SAML2AuthnContextClassRef"
                        c:classRef="urn:oasis:names:tc:SAML:2.0:ac:classes:TimeSyncToken" />
                </util:list>
            </property>
        </bean>
```

### Rebuild idp.war
* run $IDP-HOME/bin/build.sh
* If you need, move that war-file to  containers "webapps" directory (tomcat, jetty, etc)
* Restart container

Seed Fetching
-------------
From LDAP, MongoDB, SQL, File, REST, Dummy(static)

### From LDAP - External LDAP (IDM?)

* Modify LDAP properties - totp-authn-beans.xml (url, userDn, password, base)  
* Make sure that bean id "shibboleth.totp.seedfetcher" is pointing to "net.kvak.shibboleth.totpauth.authn.impl.seed.LdapSeedFetcher"  

### From MongoDB - Experimental, just testing, but it works

* Modify MongoDB properties - totp-authn-config.xml (mongoDbUrl, mongoDbName)  
* Make sure that bean id "shibboleth.totp.seedfetcher" is pointing to "net.kvak.shibboleth.totpauth.authn.impl.seed.MongoSeedFetcher"  

Adding new seed to user
----------------------

At the moment you need to add your token codes to the repository with external process. I will create some kind of registeration flow to the IdP.

TBD.  
Own registration flow / External process.


Requesting new Authentication Context Class with Shibboleth SP
-----------------------------------------------

(for testing purpose)
Add new Session Initiator

```
<SessionInitiator type="Chaining" Location="/totp" id="totp" entityID="https://IDP-ENTITY-ID">  
  <SessionInitiator type="SAML2" acsIndex="1" template="bindingTemplate.html" authnContextClassRef="urn:oasis:names:tc:SAML:2.0:ac:classes:TimeSyncToken"/>  
</SessionInitiator>  
```
