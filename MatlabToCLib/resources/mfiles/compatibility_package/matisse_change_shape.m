function new_matrix = matisse_change_shape(matrix, shape)

	new_matrix = zeros(shape);
	
	if(numel(new_matrix) ~= numel(matrix))
		fprintf('matisse_change_shape: Shape does not has the same elements has given matrix\n');
		new_matrix = matrix;
		return;
	end
	
	new_matrix(:) = matrix(:);

end