function [passed_tests, total_tests] = main()

clc;

%Clear times file
fid = fopen('times.txt','w');
fprintf(fid, '');
fclose(fid);

% Add MATISSE primitives compatibility package
addpath(<COMPABILITY_PACKAGE_PATH>, '-begin');

if( exist('errorVars', 'dir') )
	try 
		rmdir('errorVars', 's');
	catch ERROR
		fprintf('\tCould not remove ''errorVars'' folder, ignoring error (is there a MATLAB instance open on that folder?)\n');
	end
end
mkdir('errorVars');

passed_tests = 0;
total_tests = <TOTAL_TESTS>;

<TEST_FUNCTIONS>

fprintf('Passed/Total Tests: %d/%d\n', passed_tests, total_tests);

rmpath(<COMPABILITY_PACKAGE_PATH>);