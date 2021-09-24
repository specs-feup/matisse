function result = access_single_colon(X)

	% Create result
	result = zeros(numel(X), 1);
	% Give values to result
	for i=1:numel(X)
		result(i) = X(i);
	end
	

end