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

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Value("${base-url}")
    private String baseURL;

    private static final String LOGIN = "/login";

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity security) throws Exception
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
