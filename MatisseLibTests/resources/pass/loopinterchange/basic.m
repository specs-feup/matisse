%!assume_indices_in_range
function y = basic(rows, cols, A)
	y = A;
	for i = 1:rows,
		for j = 1:cols,
			y(i, j) = 1;
		end
	end
end