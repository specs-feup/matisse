%!assume_indices_in_range
%!assume_matrix_sizes_match
function y = parallel_sections(a)
	%!parallel
	y = zeros(a);
	y(:) = 1;
	%!end
	
	for i = 1:a,
	    range = zeros(1, i);
        for j = 1:numel(y),
            range(j) = 1;
        end
	    
	    %!parallel
        for j = 1:i,
	       y(j) = y(j) + range(j);
        end
	    %!end
	end 
end