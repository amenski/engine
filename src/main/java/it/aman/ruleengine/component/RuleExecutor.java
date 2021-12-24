package it.aman.ruleengine.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mvel2.util.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import it.aman.ruleengine.dal.entity.Rule;
import it.aman.ruleengine.dal.repository.RuleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RuleExecutor {

    private final Logger logger = LoggerFactory.getLogger(RuleExecutor.class);

    private final RuleRepository ruleRepository;

    private final ApplicationContextProvider applicationContextProvider;

    private static final String RULE_SEPARATOR = ";";
    public static final String ACTION_PREFIX = "service.execute";

    public RuleExecutionResult eval(Map<String, Object> facts) {
        final String methodName = "RuleExecutor.eval()";
        Rule rule = null;
        try {
            RuleExecutionResult executionResult = new RuleExecutionResult();
            List<Rule> rulesList = ruleRepository.findAll();

            List<Rule> matchedRules = match(rulesList, facts);
            rule = resolve(matchedRules);
            if (rule == null) {
                logger.error("{} No matching rule found.", methodName);
                executionResult.setError("No matching rule found");
                return executionResult;
            }

            logger.info("{} Found matching rule: {}", methodName, rule.getName());

            String action = rule.getAction();
            if (!StringUtils.isBlank(action)) { // execute an action
                IRuleExecutor service = (IRuleExecutor) resolveBean(action); // all services should extend this Interface
                Map<String, Object> fc = new HashMap<>();
                fc.put("service", service);
                fc.putAll(facts);
                action = ACTION_PREFIX + action.substring(action.indexOf("(")); //"ServiceName(params...)"; => ServiceName.execute(...)
                executionResult = RuleEvaluator.executeExpression(action, fc);
            }
            return executionResult;
        } catch (Exception e) {
            logger.error("{} Error evaluating rule: {}", methodName, rule);
            throw e;
        }
    }
    
    /**
     * If there are multiple condition(; separated),
     * split them to be executed individually later in the process.
     * 
     * @param expression
     * @return array of expressions
     */
    private String[] parse(String expression) {
        expression = StringUtils.trimToEmpty(expression);
        if (expression.contains(RULE_SEPARATOR)) {
            return expression.split(RULE_SEPARATOR);
        }
        
        return new String[] {expression};
    }

    /**
     * Match potential rules, step 2 of executing rules.
     *
     *
     * @param rules
     * @param inputObjects - facts
     * @return matching rules for the given facts(inputObjects) or empty list
     */
    private List<Rule> match(List<Rule> rules, Map<String, Object> inputObjects) {
        List<Rule> matching = new ArrayList<>();
        try {
            for (Rule rule : rules) {
                String[] expressionList = parse(rule.getCondition());
                for (String value : expressionList) {
                    boolean success = RuleEvaluator.evalExpressionToBoolean(value, inputObjects);
                    if (Boolean.FALSE.equals(success)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }

        return matching;
    }

    /**
     * Specificity resolution
     * If all of the conditions of two or more rules are satisfied, choose the rule according to how specific its conditions are.
     * It is possible to favor either the more general or the more specific case.
     * The most specific may be identified roughly as the one having the greatest number of preconditions.
     * This usefully catches exceptions and other special cases before firing the more general (default) rules
     *
     * @param matchingRules
     * @return
     */
    private Rule resolve(List<Rule> matchingRules) {
        if (matchingRules == null || matchingRules.isEmpty()) return null;
        if (matchingRules.size() == 1) return matchingRules.get(0);
        
        Map<Integer, Rule> conditionCount = new HashMap<>();
        for (Rule rule : matchingRules) {
            String[] parsed = parse(rule.getCondition());
            conditionCount.put(count(parsed), rule);
        }

        Integer max = Collections.max(conditionCount.keySet());
        return conditionCount.get(max);
    }
    
    /**
     * Resolve bean of type {@code IRuleExecutor}
     * @param action  in a format {@code BeanName(params...)}
     * @return
     */
    private Object resolveBean(String action) {
        ApplicationContext context = applicationContextProvider.getContext();
        String beanName = action.substring(0, action.indexOf("(")); // take only BeanName
        return context.getBean(beanName);
    }
    
    /**
     * Consider that every LITERAL in a rule must be separated from one another with a SPACE char
     * 
     * @param parsed
     * @return
     */
    private int count(String[] parsed) {
        int count = 0;
        for (String expression : parsed) {
            String[] split = expression.split(StringUtils.SPACE);
            for (String exp1 : split) {
                count = ParseTools.isReservedWord(exp1) ? count : count + 1;
            }
        }
        return count;
    }
    
}