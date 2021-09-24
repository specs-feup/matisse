%!assume_indices_in_range
function out = delete_single_index_scalar(in, index, unused_always0, unused_always1)
	% Column matrices remain columns, other sizes are flattened into rows.
	out = in(1:end-1);
	
	for i = 1:numel(out),
		if i < index,
			out(i) = in(i);
		else
			out(i) = in(i + 1);
		end
	end
end
