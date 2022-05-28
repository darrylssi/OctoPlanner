package nz.ac.canterbury.seng302.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

import nz.ac.canterbury.seng302.portfolio.customthymeleaf.expression.RolesExpressionDialect;

/**
 * Configuration class for implementing WebMvcConfigurer methods.
 * 
 * Currently being used to add a Thymeleaf dialect
 */
@Configuration
public class PortfolioMvcConfig implements WebMvcConfigurer {

    @Bean
    public SpringTemplateEngine templateEngine(
        @Autowired SpringResourceTemplateResolver templateResolver
    ) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.addDialect(new RolesExpressionDialect());

        return templateEngine;

    }
}