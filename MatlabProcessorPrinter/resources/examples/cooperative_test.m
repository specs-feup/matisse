%!parallel schedule(cooperative)
%!assume_indices_in_range
function y = cooperative_test(A, n)
	y = zeros(1, n);
	for i = 1:n,
		y(i) = 0;

		for j = 1:n,
			y(i) = y(i) + A(j, i);
		end
	end
end