package it.aman.ruleengine.component;

import lombok.Data;

@Data
public class RuleExecutionResult {

    private boolean success;
    private String error;
}
