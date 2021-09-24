%!assume_indices_in_range
function I = find_dynamic2(X, K)
	% Allocate an array the maximum size to return
	I = zeros(1, K, 'int');

	% Fill array until reaches K
	counter = 0;
	for i=1:numel(X)
			if(X(i) ~= 0)
				counter = counter + 1;
				I(counter) = i;
					
				% Check if reached maximum size
				if(counter == K)
					return;
				end
			end
	end
	
	% Make array correct size
	I = I(1:counter);
	
end