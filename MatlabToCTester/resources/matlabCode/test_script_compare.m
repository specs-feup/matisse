% Check if there is a limit threshould for this variable
if(exist('<VAR_NAME>_low_threshold', 'var') == 1)
	result = are_equal(<VAR_NAME>, <VAR_NAME>_C, rel_epsilon, abs_epsilon,<VAR_NAME>_low_threshold);
else 
    result = are_equal(<VAR_NAME>, <VAR_NAME>_C, rel_epsilon, abs_epsilon);
end

%result = are_equal(<VAR_NAME>, <VAR_NAME>_C, rel_epsilon, abs_epsilon);

if( ~result )
	errorVars{end+1} = '<VAR_NAME>';
	passed = 0;
	if( exist(errorVarsFile, 'file') )
        save(errorVarsFile, '<VAR_NAME>', '<VAR_NAME>_C', '-append');
    else
        save(errorVarsFile, '<VAR_NAME>', '<VAR_NAME>_C');
    end
end
