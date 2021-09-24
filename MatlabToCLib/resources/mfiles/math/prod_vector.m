function result = prod_vector(X)

	% Incomplete version of prod that does not search for the first non-singleton dimension
	
	if(is_vector(X))
		result = X(1);
		for i=2:numel(X)
			result = result * X(i);
		end
	else
		fprintf('Not yet implemented\n')
		result = 0;
	end

end

function result = is_vector(X)

	result = 0;

	if(numel(size(X)) ~= 2)	
		return;
	end
	
	if(size(X,1) == 1 || size(X,2) == 1)
		result = 1;
	end

end