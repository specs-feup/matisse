function S = sum_matrix(X, DIM)

	size_array = size(X);

	if nargin < 2
		if(size_array(1) ~= 1)
			DIM = 1;
		else
			DIM = 2;
		end
	end

	%TODO: Should exit if DIM less than 1?
	if(DIM < 1)
		fprintf('ERROR: DIM must be a positive integer\n');
		S = X;
		return;
	end
	
	%If DIM is less than one or greater than two, return input
	if(DIM > 2)
		S = X;
		return;
	end
	
	%size_array = size(X);
	%numRows = size_array(1);
	%numCols = size_array(2);
	numRows = size(X, 1);
	numCols = size(X, 2);
	
	% First dimension
	if(DIM == 1)
		%size_array = size(X);
		%numRows = size_array(1);
		%numCols = size_array(2);
		
		S = zeros(1, numCols, class(X));
		
		for i=1:numCols			
			acc = X(1, i);
			for j=2:numRows
				acc = acc + X(j, i);
			end
			
			S(1, i) = acc;
		end
		
	else
		%size_array = size(X);
		%numRows = size_array(1);
		%numCols = size_array(2);
	
		S = zeros(numRows, 1, class(X));
		
		for i=1:numRows			
			acc = X(i, 1);
			for j=2:numCols
				acc = acc + X(i, j);
			end
			
			S(i, 1) = acc;
		end
	end

end