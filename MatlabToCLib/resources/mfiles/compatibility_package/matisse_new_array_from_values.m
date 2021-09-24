function array = matisse_new_array_from_values(matrix, element, varargin)
	shape = cell2mat(varargin);
	array = zeros(shape, class(element));
end