package nz.ac.canterbury.seng302.portfolio.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    User principal;
    String token;

    public JwtAuthenticationToken(String token, User principal, Collection<? extends GrantedAuthority> authorities ) {
        super(authorities);
        this.token = token;
        this.principal = principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == this) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        JwtAuthenticationToken jwtObj = (JwtAuthenticationToken) obj;
        return (jwtObj.getToken() == this.token && jwtObj.getPrincipal() == this.principal);
    }

    @Override
    /**
     * This is required to be overwritten since equals is overwritten; however,
     * this is not required to return different hash codes or expected to be
     * used in a large hash table, so a constant return value is used.
     */
    public int hashCode() {
        return 0;
    }

    public void setToken( String token ) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}