package net.kvak.shibboleth.totpauth.authn.impl.seed;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import net.kvak.shibboleth.totpauth.api.authn.SeedFetcher;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext.AuthState;

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
			List<String> list = getAllTokenCodes(username);
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

	/*
	 * TODO Get multivalue attribute with all values
	 */
	@SuppressWarnings("rawtypes")
	public List<String> getAllTokenCodes(String user) {
		log.debug("Entering getAllTokenCodes with username: {}", user);
		return ldapTemplate.search(query().where(userAttribute).is(user),

		new AttributesMapper<String>() {

			public String mapFromAttributes(Attributes attrs) throws NamingException, javax.naming.NamingException {

				log.debug("attrs size: {}.", attrs.size());

				NamingEnumeration e = attrs.getIDs();
				boolean resu = false;

				while (e.hasMore()) {
					String attri = e.next().toString();
					log.debug("Attribute {}", attri);
					if (attri.toLowerCase().equals(seedAttribute.toString().toLowerCase())) {
						resu = true;
						break;
					}
				}

				if (resu) {
					return (String) attrs.get(seedAttribute).get();
				} else {
					return null;
				}
			}
		});
	}

}
