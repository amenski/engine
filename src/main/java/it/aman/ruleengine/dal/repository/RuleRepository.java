package it.aman.ruleengine.dal.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import it.aman.ruleengine.dal.entity.Rule;

public interface RuleRepository extends MongoRepository<Rule, String> {
}
