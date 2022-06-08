package nz.ac.canterbury.seng302.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

import nz.ac.canterbury.seng302.portfolio.customthymeleaf.expression.RolesExpressionDialect;

/**
 * Configuration class for implementing WebMvcConfigurer methods, customizing the program.
 */
@Configuration
public class PortfolioMvcConfig implements WebMvcConfigurer {

    /** Interceptor - For adding values to every template's model */
    @Autowired
    ModelAttributeInterceptor modelAttributeInterceptor;

    @Override
    public void addInterceptors (InterceptorRegistry registry) {
        registry.addInterceptor(modelAttributeInterceptor);
    }
    
    /**
     * <p>Returns the Template Engine used by Spring.</p>
     * 
     * If we want to add new features to the template engine, we add them here.
     */
    @Bean
    public SpringTemplateEngine templateEngine(
        @Autowired SpringResourceTemplateResolver templateResolver,
        // * Add your dialects below, as autowires
        @Autowired RolesExpressionDialect rolesExpressions
    ) {
        // Uses the template engine provided by `spring-boot-starter-thymeleaf`,
        // because this method overwrites it.
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        
        // A dialect encapsulates most Thymeleaf features (th:*, #strings...)
        templateEngine.addDialect(rolesExpressions);
        

        return templateEngine;

    }
}