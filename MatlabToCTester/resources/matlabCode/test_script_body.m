function [passed, errorVars, errorVarsFile, hasErrorMessage] = <SCRIPT_NAME>()

clear all;

hasErrorMessage = 0;

<ADD_PATHS>
passed = 1;
errorVars = {};
errorVarsFile = strcat(pwd, '\errorVars\<SCRIPT_NAME>.mat');

abs_epsilon = <ABS_ERROR>;
rel_epsilon = <REL_ERROR>;

<INPUT_VALUES>

try
	tic;
	<FUNCTION_CALL>
	time = toc;

	% Append to file
	fid = fopen('times.txt','a');
	fprintf(fid, '<SCRIPT_NAME>:%e\n', time);

catch ERROR
	hasErrorMessage = 1;

    % Load C outputs
	load 'cOutput.mat';

	ERROR_MESSAGE = ERROR.message;
	if( exist('ERROR_MESSAGE_C', 'var') == 0 )
        passed = 0;
        errorVarsFile = 'No variables to load.';
        fprintf('\n\t\tMATLAB test gave an error, but the C test did not.\n');
        fprintf('\t\tMATLAB error: %s\n', ERROR_MESSAGE);
%        disp(ERROR.stack);
        return;
    end

	ERROR_MESSAGE = strtrim(ERROR_MESSAGE);
	ERROR_MESSAGE_C = strtrim(ERROR_MESSAGE_C);
	if( strcmp(ERROR_MESSAGE, ERROR_MESSAGE_C) == 0 )
		passed = 0;
		errorVarsFile = 'No variables to load.';
		fprintf('\n\t\tMATLAB test gave an error, and the message is different from the C error.\n');
		fprintf('\t\tMATLAB error: %s\n', ERROR_MESSAGE);
      %  for i = 1 : max(size(ERROR.stack))
       %    disp(ERROR.stack(i)); 
       % end
		fprintf('\t\tC error: %s\n', ERROR_MESSAGE_C);
		return;
	end

	fprintf('\n\t<CURRENT>/<TOTAL> Passed test ''<INPUT_NAME>'' (error test)');
	return;

end

% Load C outputs
load 'cOutput.mat';

if( exist('ERROR_MESSAGE_C', 'var') == 1 )
    fprintf('\n\t\tMATLAB test completed successfully, but the C test did not.\n');
    fprintf('\t\tC error: %s\n', ERROR_MESSAGE_C);
    return;
end

<COMPARISON_TESTS>

if( passed )
	fprintf('\n\t<CURRENT>/<TOTAL> Passed test ''<INPUT_NAME>''');
end

<REMOVE_PATHS>

end
