# Engine
A toy spring & MVEL based rule engine base off of ***Specificity resolution***. 

# Specificity resolution
If all of the conditions of two or more rules are satisfied, choose the rule according to how specific its conditions are. It is possible to favor either the more general or the more specific case. The most specific may be identified roughly as the one having the greatest number of preconditions. This usefully catches exceptions and other special cases before firing the more general (default) rules