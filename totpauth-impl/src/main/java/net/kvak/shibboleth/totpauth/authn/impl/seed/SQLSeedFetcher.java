package net.kvak.shibboleth.totpauth.authn.impl.seed;

import javax.annotation.Nonnull;

import net.kvak.shibboleth.totpauth.api.authn.SeedFetcher;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import org.springframework.jdbc.core.JdbcTemplate;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext.AuthState;
import net.kvak.shibboleth.totpauth.authn.impl.TotpUtils;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

public class SQLSeedFetcher implements SeedFetcher {

	@Nonnull
	@NotEmpty
	private final Logger log = LoggerFactory.getLogger(SQLSeedFetcher.class);

	/** Name of the table in the seed db **/
	@Nonnull
	@NotEmpty
	private String seedDbTableName;

	/** Name of the username column in the seed db **/
	@Nonnull
	@NotEmpty
	private String usernameColumnName;

	/** Name of the seed column in the seed db **/
	@Nonnull
	@NotEmpty
	private String seedColumnName;

	/** JdbcTemplate **/
	@Nonnull
 	@NotEmpty
	private JdbcTemplate jdbcTemplate;

	/** Inject JDBC Template */
	public void setjdbcTemplate(@Nonnull @NotEmpty final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public SQLSeedFetcher(String seedDbTableName, String usernameColumnName, String seedColumnName) {
		log.debug("Construct RegisterNewSeedSql with {} - {} - {}", seedDbTableName, usernameColumnName, seedColumnName);
		this.seedDbTableName = seedDbTableName;
		this.usernameColumnName = usernameColumnName;
		this.seedColumnName = seedColumnName;
	}

	@Override
	public void getSeed(String username, TokenUserContext tokenUserCtx) {

		try {
			String querysql = "SELECT " + seedColumnName + " FROM " + seedDbTableName + " WHERE " + usernameColumnName + " = ?";
			String seed = jdbcTemplate.queryForObject(
        		querysql,
        		new Object[] { username },
        		String.class);

			if (Strings.isNullOrEmpty(seed)) {
				tokenUserCtx.setState(AuthState.REGISTER);
				log.debug("Unable to fetch existing seed for user {}", username);
			} else {
				log.debug("Found seed for user {}", username);
				tokenUserCtx.setTokenSeed(seed);
				tokenUserCtx.setState(AuthState.OK);
			}
		} catch (Exception e) {
			tokenUserCtx.setState(AuthState.MISSING_SEED);
			log.debug("Error accessing seed datasource", e);
		}

	}

}
