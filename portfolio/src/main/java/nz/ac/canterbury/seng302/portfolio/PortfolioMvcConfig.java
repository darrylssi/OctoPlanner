package nz.ac.canterbury.seng302.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for implementing WebMvcConfigurer methods.
 * 
 * Currently being used to add interceptors
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
    
}