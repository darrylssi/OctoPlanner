package nz.ac.canterbury.seng302.portfolio.customthymeleaf.expression;

import java.util.Collections;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;

/**
 * Dialect class for adding a #roles.[method]() dialect to the Thymeleaf template.
 */
@Component
public class DatesExpressionDialect extends AbstractDialect implements IExpressionObjectDialect {

    static final String DIALECT_NAME = "lensfolioDates";
    static final DateUtils DATE_UTILS_OBJECT = DateUtils.getInstance();

    public DatesExpressionDialect() {
        super(DIALECT_NAME);
    }

    /**
     * The factory provided to Thymeleaf's expression parsing.
     * 
     * <p>You should <b>never</b> have to call this</p>
     */
    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        /* 
          NOTE: The comments below are NOT JavaDoc, because they're inner methods.
          Also so you can hover over them in an IDE to see Thymeleaf's docs.
        */        
        return new IExpressionObjectFactory() {

            // Sets the thymeleaf expression name(s) (e.g. #strings, #dates, #lists)
            // we can match. In our case, we can only provide one: 'lensfolioDates' -> DateUtils.
            @Override
            public Set<String> getAllExpressionObjectNames() {
                return Collections.singleton(DIALECT_NAME);
            }

            // If the expression name is the dialect name, return the object.
            // They ask us again in case this factory matches many names to 
            // many objects, but we only give one.
            @Override
            public Object buildObject(IExpressionContext context, String expressionObjectName) {
                if (expressionObjectName.equals(DIALECT_NAME)) {
                    return DATE_UTILS_OBJECT;
                }
                return null;
            }

            // Because DateUtils only has static pure functions, it's safe to cache.
            @Override
            public boolean isCacheable(String expressionObjectName) {
                return true;
            }
            
        };
    }
    
}
