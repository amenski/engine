package it.aman.ruleengine.component;

import java.io.Serializable;
import java.util.Map;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(RuleEvaluator.class);

    /**
     *
     * @param expression
     * @param inputObjects
     * @return Return false if evaluation fails or if `unresolvable property or identifier` exception occurs for a missing fact(object)
     */
    public static boolean evalExpressionToBoolean(String expression, Map<String, Object> inputObjects) {
        try {
            Serializable compiled =  compile(expression);
            return MVEL.executeExpression(compiled, inputObjects, Boolean.class);
        } catch (Exception e) {
            //do nothing
        }
        return false;
    }

    /**
     * Execute specific actions corresponding to a rule
     *
     * @param expression
     * @param inputObjects
     * @throws Exception
     */
    public static RuleExecutionResult executeExpression(String expression, Map<String, Object> inputObjects) {
        try {
            Serializable compiled =  compile(expression);
            return MVEL.executeExpression(compiled, inputObjects, RuleExecutionResult.class); // FIXME we dont have converter for this class, how does it work?
        } catch (Exception e) {
            logger.error("Can not execute Mvel Expression : {} Error: {}", expression, e.getMessage());
            throw e;
        }
    }

    /**
     * Compile expressions importing necessary classes such as an {@code Enum}.
     * 
     * @param expression - expression to be compiled
     * @return
     */
    private static Serializable compile(String expression) {
        try {
            ParserContext ctx = new ParserContext();
            ctx.addImport(RuleExecutionContext.class);
            
            return  MVEL.compileExpression(expression, ctx);
        } catch (Exception e) {
            logger.error("Can not compile Mvel Expression : {} Error: {}", expression, e.getMessage());
            throw e;
        }
    }
    
    
    private RuleEvaluator() {
        throw new IllegalStateException("Utility class");
    }
}
