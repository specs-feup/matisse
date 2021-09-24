%!assume_indices_in_range
function s = MATISSE_raw_ind2sub(sizes, ind)
	dims_to = zeros(1, numel(sizes) + 1);
	dims_to(1) = 1;
	for i = 1:numel(sizes),
		dims_to(i + 1) = dims_to(i) * sizes(i);
	end

	s = zeros(1, numel(sizes));
	for i = 1:numel(sizes),
		s(i) = floor(mod(ind - 1, dims_to(i + 1)) / dims_to(i)) + 1;
	end
end