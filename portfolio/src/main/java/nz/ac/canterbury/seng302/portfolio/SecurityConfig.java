package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.authentication.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${base-url}")
    private String baseURL;

    private static final String LOGIN = "/login";

    @Override
    protected void configure(HttpSecurity security) throws Exception
    {

        // Force authentication for all endpoints except /login and /register
        security
            .addFilterBefore(new JwtAuthenticationFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, LOGIN, "/register", "/")    // The way of accessing /static is not great
                    .permitAll()
                    .and()
                .authorizeRequests()
                    .anyRequest()
                    .authenticated();

        security.cors();
        security.csrf().disable();
        security.logout()
                .permitAll()
                .invalidateHttpSession(true)
                .deleteCookies("lens-session-token")
                .logoutSuccessUrl(LOGIN);

        // Disable basic http security
        security
            .httpBasic().disable();
        // Redirect to login page if unauthenticated
        security
            .formLogin().loginPage(baseURL + "login");

        // let the H2 console embed itself in a frame
        security.headers().frameOptions().sameOrigin();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(LOGIN, "/styles/**", "/img/**", "/register", "/");
    }
}