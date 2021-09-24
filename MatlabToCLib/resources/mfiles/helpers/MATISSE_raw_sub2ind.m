function output = MATISSE_raw_sub2ind(sizes, indices)
    num_indices = numel(sizes);

	output = indices(1);
	
	% Subtract one to correctly calculate the index
	output = output - 1;
	
	dimAcc = uint32(1);
	for i = 2:num_indices
		dimAcc = dimAcc * uint32(sizes(i-1));
		output = output + ((indices(i)-1) * dimAcc);
	end

	% Add one to adjust for MATLAB index
	output = output + 1;
end

