package net.kvak.shibboleth.totpauth.authn.impl.seed;

import java.util.ArrayList;
import java.util.List;

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

@SuppressWarnings("deprecation")
public class LdapSeedFetcher implements SeedFetcher {

	/* Class logger */
	private final Logger log = LoggerFactory.getLogger(LdapSeedFetcher.class);

	/* LdapTemplate */
	private LdapTemplate ldapTemplate;

	/* seedToken attribute in ldap */
	private String seedAttribute;

	/* Username attribute in ldap */
	private String userAttribute;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public LdapSeedFetcher(String seedAttribute, String userAttribute) {
		log.debug("Construct LdapSeedFetcher with {} - {}", seedAttribute, userAttribute);
		this.seedAttribute = seedAttribute;
		this.userAttribute = userAttribute;
	}

	@Override
	public void getSeed(String username, TokenUserContext tokenUserCtx) {
		log.debug("Entering LdapSeedFetcher");

		try {
			ArrayList<String> list = getAllTokenCodes(username);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String fetchDn(String userName) {

		String dn = "";
		EqualsFilter filter = new EqualsFilter(userAttribute, userName);
		log.debug("Trying to find user {} dn from ldap with filter {}", userName, filter.encode());
		try {
			List result = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.toString(), new AbstractContextMapper() {
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
