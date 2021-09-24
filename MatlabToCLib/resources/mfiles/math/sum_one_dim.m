function acc = sum_one_dim(matrix)

	acc = matrix(1);
	for i=2:numel(matrix)
		acc = acc + matrix(i);
	end

end