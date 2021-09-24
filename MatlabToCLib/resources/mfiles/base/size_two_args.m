function result = size_two_args(X, DIM)
	%Get size array
	sizeArray = size(X);
	
	%If DIM greater than number of elements in size, return 1
	if(DIM > numel(sizeArray))
		result = 1;
		return;
	end

	% Get DIM
	result = sizeArray(DIM);
	
end