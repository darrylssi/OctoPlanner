package nz.ac.canterbury.seng302.portfolio.customthymeleaf.expression;

import java.util.Collections;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import nz.ac.canterbury.seng302.portfolio.utils.RoleUtils;

/**
 * Dialect class for adding #roles.<...> methods to the Thymeleaf template.
 */
@Component
public class RolesExpressionDialect extends AbstractDialect implements IExpressionObjectDialect {

    static final String DIALECT_NAME = "roles";
    static final RoleUtils roleUtils = RoleUtils.getInstance();

    public RolesExpressionDialect() {
        super(DIALECT_NAME);
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return new IExpressionObjectFactory() {

            // Sets the thymeleaf expression name(s) (e.g. #strings, #roles, #lists)
            @Override
            public Set<String> getAllExpressionObjectNames() {
                return Collections.singleton(DIALECT_NAME);
            }

            // If the expression name is the dialect name, return the object.
            // I don't know why they ask us again when we've already provided the
            // name above, but whatever.
            @Override
            public Object buildObject(IExpressionContext context, String expressionObjectName) {
                if (expressionObjectName.equals(DIALECT_NAME)) {
                    return roleUtils;
                }
                return null;
            }

            @Override
            public boolean isCacheable(String expressionObjectName) {
                return true;
            }
            
        };
    }
    
}
