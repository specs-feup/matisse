% MIN for vectors and matrixes
function result = min_dynamic(X)

	numRows = size(X, 1);
	numCols = size(X, 2);
	% Check why scalar does not work with this code
%	if(numRows == 1 & numCols == 1)
%		result = min_scalar(X);
	if(numRows == 1 | numCols == 1)
		result = min_vector(X);
	else
		result = min_matrix(X);
	end

end

%function result = min_scalar(X)
%	result = zeros(1, 1, class(X));
%	result(1) = X(1);
%end

function result = min_vector(X)

	minValue = X(1);
	
	for i=2:numel(X)
		currentValue = X(i);
		if(currentValue < minValue)
			minValue = currentValue;
		end
	end

	% Output is a scalar
	result = zeros(1, 1, class(X));
	result(1) = minValue;

end

function result = min_matrix(X)

	result = zeros(1, size(X,2), class(X));

	for col=1:size(X,2)

		minValue = X(1, col);
		for row=2:size(X,1)
			currentValue = X(row, col);
			if(currentValue < minValue)
				minValue = currentValue;
			end
		end
		
		result(1, col) = minValue;
	end
	
end