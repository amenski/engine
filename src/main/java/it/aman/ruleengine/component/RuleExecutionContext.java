package it.aman.ruleengine.component;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class RuleExecutionContext {

    private Map<String, Object> context;

    public RuleExecutionContext() {
        this(null);
    }

    public RuleExecutionContext(Map<String, Object> context) {
        this.context = context;
    }
    
    public void addContext(String key, Object value) {
        if(StringUtils.isBlank(key) || value == null) return;
        context.put(key, value);
    }
}
