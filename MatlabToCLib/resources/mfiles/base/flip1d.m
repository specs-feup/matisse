%!assume_indices_in_range
function y = flip1d(x)
	y = x;
	for i = 1:numel(y)/2,
		value = y(end - i + 1);
		y(end - i) = y(i);
		y(i) = value;
	end
end
