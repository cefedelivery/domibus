package eu.domibus.api.party;

import org.springframework.expression.spel.ast.Identifier;

import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class Party {

    protected Set<Identifier> identifiers; //NOSONAR

    protected String name;

    protected String userName;

    protected String password;

    protected String endpoint;
}
