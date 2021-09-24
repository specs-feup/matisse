%
% equal = are_equal(expected, calculated, rel_eps, abs_eps)
%
% Inputs
%   expected  	: the expected result
%   calculated	: the calculated result
%   rel_eps   	: the relative error tolerance
%   abs_eps   	: the absolute error tolerance
%
% Outputs
%	equal		: a boolean that indicates if the values are equal
%
%
% This function compares two values and returns true if
% they are close enough to be considered equal.
%
% An absolute tolerance value is used when the expected result is 0,
% otherwise a relative tolerance value is used.
% 
% If the calculated result is 0 and the expected result is not, this comparison
% will always fail if the relative tolerance smaller than 100%.
%
% For more information on how to compare floating point values please see:
% 	http://www.altdevblogaday.com/2012/02/22/comparing-floating-point-numbers-2012-edition
%
function equal = are_equal(expected, calculated, rel_eps, abs_eps, min_value)

    hmean = harmmeani(abs(expected(expected~=0)));

	% the result matrix
	result_matrix = zeros(size(expected));
    
	% cast the result to double
	calculated = double(calculated);
	expected = double(expected);
    
	% calculate the absolute error
	try
		abs_error = abs(calculated - expected);
	catch ERROR
        ERROR_MESSAGE = ERROR.message;
        fprintf('\n\t\tERROR during results comparison: %s.\n', ERROR_MESSAGE);
        equal = 0;
        return;
    end	
	
	
	% iterate over the expected results matrix
	for i=1:numel(expected)
	
		if(nargin >= 5)
            if(abs(expected(i)) < min_value) 
                result_matrix(i) = 1;
                continue;
            end
		end
	
		% check if it was defined a minimum threshold
%		if(exist('matisse_min_value') == 1)
			% if expected value is below minimum threshold, ignore value
%			if(expected < matisse_min_value) 
%				continue;
%			end
%		end
	
		% if zero
		% compare the absolute error to the absolute epsilon
		if(expected(i) == 0)
			result_matrix(i) = abs_error(i) < abs_eps;
		% if non-zero
		% calculate the relative error and compare it to the relative epsilon
		else
			%result_matrix(i) = abs_error(i) ./ expected(i) < rel_eps;
			result_matrix(i) = abs_error(i) ./ expected(i) <= rel_eps;
			
			if(result_matrix(i) == false) 
               % Check with the harmonic mean 
               result_matrix(i) = abs_error(i) < hmean * rel_eps ;
               %fprintf('abs_error: %e; expected: %e; hmean: %e\n', abs_error(i), expected(i), hmean); 
               %if(result_matrix(i) == false)
               %   fprintf('Still wrong\n'); 
               %end
            end
		end
		
	end

	% the final boolean value
	equal = all(result_matrix(:));
end

% Avoid licensing trouble (too many Statistics Toolbox users)
function m = harmmeani(X)
   n = numel(X);
   total = 0;
   for i = 1:n
      total = total + 1 / X(i); 
   end
   m = n / total;
end