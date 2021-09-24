%!assume_indices_in_range
function y = xif(rows, cols, A)
	y = A;
	for i = 1:rows,
		for j = 1:cols,
			if i > 1,
				val = i;
			else
				val = j;
			end
			y(i, j) = val;
		end
	end
end