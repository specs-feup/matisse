%!assume_indices_in_range
function I_out = find_dynamic1(X)
	% Allocate an array the size of the input array
	if numel(X) == size(X, 2),
		I = zeros(1, numel(X), 'int');
	else
		I = zeros(numel(X), 1, 'int');
	end

	% Fill array
	counter = 0;
	for i=1:numel(X)
			if(X(i) ~= 0)
				counter = counter + 1;
				I(counter) = i;
			end
	end
	
	I_out = I(1:counter);
end