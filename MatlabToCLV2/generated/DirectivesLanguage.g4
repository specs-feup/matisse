grammar DirectivesLanguage;

@header {
package org.specs.matlabtocl.v2.directives.generated;
}

parallel: PARALLEL parallel_settings EOF;

parallel_settings: (schedule | local_size | sum_reduction_strategy | DISABLE_RANGE_SET)*;

schedule: SCHEDULE LPAREN schedule_mode RPAREN;

schedule_mode: AUTO | DIRECT | COOPERATIVE | SUBGROUP_COOPERATIVE |
                   value_parameterized_distribution_strategy (COMMA value)+;

value_parameterized_distribution_strategy: COARSE | COARSE_SEQUENTIAL | COARSE_GLOBAL_ROTATION |
	FIXED_WORK_GROUPS | FIXED_WORK_GROUPS_SEQUENTIAL | FIXED_WORK_GROUPS_GLOBAL_ROTATION;

sum_reduction_strategy: SUM_REDUCTION_STRATEGY LPAREN sum_reduction_strategy_type RPAREN;

sum_reduction_strategy_type: SIMPLE | LOCAL_MEMORY | WORKGROUP;

value: identifier | NUMBER;

local_size : LOCAL_SIZE LPAREN value (COMMA value)* RPAREN;

/* Allow keywords as names */
identifier: IDENTIFIER |
			LOCAL_SIZE | SCHEDULE | SUM_REDUCTION_STRATEGY |
			AUTO | DIRECT |
            COARSE | COARSE_SEQUENTIAL | COARSE_GLOBAL_ROTATION |
            FIXED_WORK_GROUPS_SEQUENTIAL | FIXED_WORK_GROUPS_GLOBAL_ROTATION |
            COOPERATIVE | SUBGROUP_COOPERATIVE;

COARSE: 'coarse';
COARSE_SEQUENTIAL: 'coarse_sequential';
COARSE_GLOBAL_ROTATION: 'coarse_global_rotation';
FIXED_WORK_GROUPS: 'fixed_work_groups';
FIXED_WORK_GROUPS_SEQUENTIAL: 'fixed_work_groups_sequential';
FIXED_WORK_GROUPS_GLOBAL_ROTATION: 'fixed_work_groups_global_rotation';
PARALLEL: 'parallel';
SCHEDULE: 'schedule';
AUTO: 'auto';
DIRECT: 'direct';
DISABLE_RANGE_SET : 'disable_range_set';
LOCAL_SIZE: 'local_size';
SUM_REDUCTION_STRATEGY: 'sum_reduction_strategy';
SIMPLE: 'simple';
LOCAL_MEMORY: 'local_memory';
WORKGROUP: 'workgroup';
COOPERATIVE: 'cooperative';
SUBGROUP_COOPERATIVE: 'subgroup_cooperative';

LPAREN: '(';
RPAREN: ')';
COMMA: ',';

IDENTIFIER: ALPHA (ALPHA | '_' | DIGIT)*;
NUMBER: [+-]? DIGIT+ ('.' DIGIT*)? ([eE] [+-]? DIGIT+)?;

fragment ALPHA: [a-zA-Z];
fragment DIGIT: [0-9];

WHITESPACE: [ \t\r\n] -> skip;
