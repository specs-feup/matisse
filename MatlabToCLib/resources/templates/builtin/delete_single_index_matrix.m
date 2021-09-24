%!assume_indices_in_range
function out = delete_single_index_matrix(in, indices, unused_always0, unused_always1)
	indices = sort(indices(:));

	% Flatten, but preserve "row-ness"/"column-ness"
	% Column matrices remain columns, other sizes are flattened into rows.
	out = in(1:end);

	current_skip_index = 1;
	out_pos = 1;

	for i = 1:numel(in),
		while current_skip_index <= numel(indices) && indices(current_skip_index) < i,
			current_skip_index = current_skip_index + 1;
		end
		if current_skip_index <= numel(indices) && indices(current_skip_index) == i,
			% Skip
		else
			out(out_pos) = in(i);
			out_pos = out_pos + 1;
		end
	end

	out = out(1:out_pos-1);

end
