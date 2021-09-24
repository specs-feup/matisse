%!assume_indices_in_range
function y = <FUNCTION_NAME>(<INPUTS>)
	<SET_VALUE>
	put_prefix = <PRE>;
	put_postfix = <POST>;

	% TODO: Validate pad is vector.

	A_ndims = ndims(A);
	pad_numel = numel(pad);
	y_ndims = max(A_ndims, pad_numel);
	
	y_size = matisse_new_array_from_dims(1, y_ndims);
	for i = 1:y_ndims,
		size_at_i = size(A, i);
		if i <= pad_numel,
			size_at_i = size_at_i + pad(i) * (put_prefix + put_postfix);
		end
		y_size(i) = size_at_i;
	end

	y = zeros(y_size, class(A));
	y(:) = value;
    
    [<SIZES>] = size(A); 
<LOOPS>
		y(<DEST_INDICES>) = A(<SRC_INDICES>);
<END_LOOPS>
end