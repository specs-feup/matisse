try
    [p, e, ef, hem] = <FUNCTION_NAME>_<TEST_NAME>();
catch ERROR
	p = 0;
	hem = 1;
	fprintf('\n\t\tError while running test '' <TEST_NAME> ''.\n');
    fprintf('\t\tMATLAB error: %s\n', ERROR.message);
    for k=1:length(ERROR.stack)
      fprintf('\t\tLine %s, file %s\n', num2str(ERROR.stack(k).line), ERROR.stack(k).file);
   end
end

passed = passed * p;

if(p)
   passed_tests = passed_tests + 1; 
end

if( hem ~= 1 )
	if( ~p )
        fprintf('\n\tFailed test '' <TEST_NAME> '' when using a relative error of <REL_ERROR> and and absolute error of <ABS_ERROR>.\n');
        fprintf('\t\tThe following variables are wrong:\n')
        fprintf('\t\t%s\n', e{:});
        fprintf('\t\tTheir values can be loaded from: %s\n', ef);
    end
end
