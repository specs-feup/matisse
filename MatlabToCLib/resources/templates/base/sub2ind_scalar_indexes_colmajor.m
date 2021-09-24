function output = <FUNCTION_NAME>(size<INDEXES>)

	indexes = zeros(1, <NUM_INDEXES>, 'int32');
<INDEX_ASSIGN>

	output = indexes(1);
	%output = indexes(<NUM_INDEXES>);
	
	if(<NUM_INDEXES> == 1)
		return;
	end
	
	% Subtract one to correctly calculate the index
	output = output - 1;
	
	dimAcc = 1;
	for i = 2:<NUM_INDEXES>
	%for i = <NUM_INDEXES>-1:-1:1
		dimAcc = dimAcc * size(i-1);
		%dimAcc = dimAcc * size(i+1);
		output = output + ((indexes(i)-1) * dimAcc);
		%output = output + ((indexes(i)) * dimAcc);
	end

	% Add one to adjust for MATLAB index
	output = output + 1;
	
end