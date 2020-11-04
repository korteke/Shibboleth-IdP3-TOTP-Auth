package net.kvak.shibboleth.totpauth.authn.impl.seed;

import javax.annotation.Nonnull;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.EqualsFilter;

import net.kvak.shibboleth.totpauth.api.authn.SeedFetcher;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext.AuthState;

import static org.apache.http.HttpHeaders.USER_AGENT;

@SuppressWarnings("deprecation")
public class LdapSeedFetcher implements SeedFetcher {

    /* Class logger */
    private final Logger log = LoggerFactory.getLogger(LdapSeedFetcher.class);

    /* LdapTemplate */
    private LdapTemplate ldapTemplate;

    /* seedToken attribute in ldap */
    private String seedAttribute;

    /* mobile attribute in ldap */
    private String phoneAttribute;

    /* smsc.ru user */
    private String smsGatewayUser;

    /* smsc.ru password */
    private String smsGatewayPassword;

    /* Username attribute in ldap */
    private String userAttribute;

    private DistinguishedName path;

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setBaseDn(String baseDn) {
        this.path = new DistinguishedName(baseDn);
    }

    public LdapSeedFetcher(String seedAttribute, String userAttribute, String phoneAttribute, String smsGatewayUser, String smsGatewayPassword) {
        log.debug("Construct LdapSeedFetcher with {} - {} - {}", seedAttribute, userAttribute, phoneAttribute);
        this.seedAttribute = seedAttribute;
        this.phoneAttribute = phoneAttribute;
        this.userAttribute = userAttribute;
        this.smsGatewayUser = smsGatewayUser;
        this.smsGatewayPassword = smsGatewayPassword;
    }

    @Override
    public void getSeed(String username, TokenUserContext tokenUserCtx) {
        log.debug("Entering LdapSeedFetcher");

        try {
            ArrayList<String> list = getAllTokenCodes(username);
            DirContextOperations context = ldapTemplate.lookupContext(fetchDn(username));
            String[] phones = context.getStringAttributes(phoneAttribute);
            if (phones != null && phones.length >= 1) {
                sendCode(phones[0], tokenUserCtx.getTokenCode());
            }
            if (list.isEmpty() || list.get(0) == null) {
                tokenUserCtx.setState(AuthState.REGISTER);
                log.debug("List with token seeds was empty");
            } else {
                log.debug("Token seed list size is: {} first: {}", list.size(), list.get(0));

                for (String seed : list) {
                    log.debug("Adding seed {} for user {}", seed, username);
                    tokenUserCtx.setTokenSeed(seed);
                }
                tokenUserCtx.setState(AuthState.OK);
            }
        } catch (Exception e) {
            tokenUserCtx.setState(AuthState.MISSING_SEED);
            log.debug("Encountered problems with LDAP", e);
        }

    }

    private void sendCode(String phone, int tokenCode) {
        try {
            log.debug("Sending SMS to {} with code {}", phone, tokenCode);
            String url = String.format("http://smsc.ru/sys/send.php?login=%s&psw=%s&phones=%s&mes=%s",
                    smsGatewayUser, smsGatewayPassword, phone, "Your_auth_code:" + tokenCode);
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            HttpResponse response = client.execute(request);

            log.debug("smsc.ru response Code : "
                    + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            log.debug("Error with sendCode: ", e);
        }
    }

    public ArrayList<String> getAllTokenCodes(String user) {
        log.debug("Entering getAllTokenCodes");
        ArrayList<String> tokenList = new ArrayList<String>();

        try {
            DirContextOperations context = ldapTemplate.lookupContext(fetchDn(user));
            String[] values = context.getStringAttributes(seedAttribute);

            if (values.length > 0) {
                for (String value : values) {
                    if (log.isDebugEnabled()) {
                        log.debug("Token value {}", value);
                    }
                    tokenList.add(value);
                }
            }

        } catch (Exception e) {
            log.debug("Error with getAllTokenCodes", e);
        }

        return tokenList;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String fetchDn(String userName) {

        String dn = "";
        EqualsFilter filter = new EqualsFilter(userAttribute, userName);
        log.debug("Trying to find user {} dn from ldap with filter {}", userName, filter.encode());
        try {
                        /* Search base in ldap */
            List result = ldapTemplate.search(path, filter.toString(), new AbstractContextMapper() {
                protected Object doMapFromContext(DirContextOperations ctx) {
                    return ctx.getDn().toString();
                }
            });
            log.debug("DN size: {}", result.size());
            if (result.size() == 1) {
                log.debug("User {} relative DN is: {}", userName, (String) result.get(0));
                dn = (String) result.get(0);
                return dn;
            }
        } catch (Exception e) {
            log.debug("Error with fetchDn: ", e);

        }
        throw new RuntimeException("User not found or not unique");
    }
}

