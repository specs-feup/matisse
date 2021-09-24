function S = sum_dynamic(X, DIM)
	if(ndims(X) == 3)
		S = sum_matrix_3d(X);
		return;
	end
	
	
	if nargin < 2
		non_single_dims = find(size(X) ~= 1);
		
		% If only single dimensions, it is a scalar, return it
		if(isempty(non_single_dims))
			S = X;
			return;
		end
		
		% Get the first non-singleton dimension
		DIM = non_single_dims(1);
	end
	
	%Special case: matrix
	if(numel(size(X)) == 2)
		S = sum_matrix(X, DIM);
		return;
	end
	
	fprintf('''sum'' for matrices with more than two dimensions not yet implemented\n');
	S = X;
end

function S = sum_matrix_old(X, DIM)

	%If DIM is less than one or greater than two, return input
	%TODO: Should raise error if DIM less than 1?
	if(DIM < 1 || DIM > 2)
		S = X;
		return;
	end
	
	size_array = size(X);
	numRows = size_array(1);
	numCols = size_array(2);
	%numRows = size(X, 1);
	%numCols = size(X, 2);
	
	% First dimension
	if(DIM == 1)
		
		S = zeros(1, numCols);
		
		for i=1:numCols			
			acc = X(1, i);
			for j=2:numRows
				acc = acc + X(j, i);
			end
			
			S(1, i) = acc;
		end
		
	else
		S = zeros(numRows, 1);
		
		for i=1:numRows			
			acc = X(i, 1);
			for j=2:numCols
				acc = acc + X(i, j);
			end
			
			S(i, 1) = acc;
		end
	end

end

function S = sum_matrix_3d(X)
	size_array = size(X);


	numRows = size(X, 1);
	numCols = size(X, 2);
	z = size(X, 3);
	
	% Init acc
%	acc = X(1);
%	MATISSE_probe(X);
%	MATISSE_probe(X);
	
	% First dimension
		%size_array = size(X);
		%numRows = size_array(1);
		%numCols = size_array(2);
		
		S = zeros(1, numCols, z, class(X));
		
		for k=1:z
			for i=1:numCols		
				acc = X(1, i,k);
				%MATISSE_probe(acc);
				for j=2:numRows
					acc = acc + X(j, i,k);
				end
				
				S(1, i, k) = acc;
			end
		end

end