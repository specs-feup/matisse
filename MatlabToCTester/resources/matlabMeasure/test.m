%%% <TEST_NAME> %%%

fprintf('Testing ''<TEST_NAME>''\n');

% Append program name
fid = fopen('times.csv','a');
fprintf(fid, '<TEST_NAME>');
fclose(fid);

% Add paths
<ADD_PATHS>

% Clear variables
clear;

% Load input vector
<INPUT_VECTOR_FUNCTION>;

% First execution, for warming up
<FUNCTION_CALL>

% First Execution
tic;
<FUNCTION_CALL>
first_time = toc;

for i=1:<EXECUTIONS>

	if(first_time < 0.05)

		rep = 50;
		tic;
		for i=1:rep
			<FUNCTION_CALL>
		end
		time = toc/rep;
	else
		tic;
		<FUNCTION_CALL>;
		time = toc;
	end

	% Save time
	fid = fopen('times.csv','a');
	fprintf(fid, ';%e', time);
	fclose(fid);
end

% New line
fid = fopen('times.csv','a');
fprintf(fid, '\n');
fclose(fid);

% Close file
%fclose(fid);

% Append to file
%fid = fopen('times.txt','a');
%fprintf(fid, '<TEST_NAME>:%e\n', time);
%fclose(fid);

% Remove paths
<REMOVE_PATHS>

