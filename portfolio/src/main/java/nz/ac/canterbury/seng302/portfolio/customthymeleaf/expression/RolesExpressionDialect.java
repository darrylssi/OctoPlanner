package nz.ac.canterbury.seng302.portfolio.customthymeleaf.expression;

import java.util.Collections;
import java.util.Set;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import nz.ac.canterbury.seng302.portfolio.utils.RoleUtils;

public class RolesExpressionDialect extends AbstractDialect implements IExpressionObjectDialect {

    static final String DIALECT_NAME = "roles";
    static final RoleUtils roleUtils = RoleUtils.getInstance();

    public RolesExpressionDialect() {
        super(DIALECT_NAME);
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return new IExpressionObjectFactory() {

            @Override
            public Set<String> getAllExpressionObjectNames() {
                return Collections.singleton(DIALECT_NAME);
            }

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
