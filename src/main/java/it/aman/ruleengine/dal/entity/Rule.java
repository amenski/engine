package it.aman.ruleengine.dal.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("rules")
public class Rule {

    @Id
    private String id;
    private String name;
    private String condition;
    private String action;
    private String error;
}
