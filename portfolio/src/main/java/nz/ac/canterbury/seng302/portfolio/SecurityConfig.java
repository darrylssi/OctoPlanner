package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.authentication.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Class that uses Spring security to prevent unauthorised access to the application.
 * Basically that means people who aren't logged in can only access the login and register pages,
 * and any attempt to access a page that requires authorisation will send them to the login screen.
 * Some more info is here: https://www.baeldung.com/spring-security-configuring-urls
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Value("${base-url}")
    private String baseURL;

    private static final String LOGIN = "/login";

    /**
     * This tells the security for the app what to do in certain scenarios, and
     * what pages require authorisation to access. See code comments for details.
     * @param security the application security configuration object
     * @return built security object with parameters set as in this method
     * @throws Exception when something goes wrong when setting security parameters
     * (the methods provide no details on why an exception might be thrown)
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity security) throws Exception {

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
            .formLogin().loginPage(LOGIN);

        // let the H2 console embed itself in a frame
        security.headers().frameOptions().sameOrigin();

        return security.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers(LOGIN, "/styles/**", "/img/**", "/register", "/");
    }
}
