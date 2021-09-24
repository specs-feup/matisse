function new_matrix = <FUNCTION_NAME>(shape)
%function new_matrix = new_from_matrix_<CLASS>_<VALUE_STRING>(shape)
	% Create matrix
	new_matrix = matisse_new_array(shape, '<CLASS>');

	% Initialize matrix
	for i=1:numel(new_matrix)
		new_matrix(i) = <VALUE>;
	end
end