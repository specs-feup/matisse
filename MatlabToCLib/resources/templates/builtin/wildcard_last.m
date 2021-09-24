	%dims = numel(size(X));
	dims = ndims(X);
	if(dims > <TOTAL_INDEXES>)
		sizeX = size(X);
		lastIndex = prod(sizeX(<TOTAL_INDEXES>:dims));
	else
		lastIndex = size(X, <TOTAL_INDEXES>);
	end
