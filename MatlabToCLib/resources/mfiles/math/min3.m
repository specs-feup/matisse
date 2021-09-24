%!assume_indices_in_range
function [y, positions] = min3(A, B, DIM)
    if numel(B) ~= 0,
        %error 'Second argument of min(A, B, C) must be an empty matrix.';
    end
    
    A_size = size(A);
    if DIM < 1 || floor(DIM) ~= DIM || numel(DIM) ~= 1,
        %error 'Dimension argument must be a positive integer scalar.';
    end
    
    if DIM > numel(A_size),
    	% Can't do y = A; due to limitations in type system (when A is int, class(A) is reported as int32)
        y = zeros(size(A), class(A));
        y(:) = A(:);

        positions = ones(size(A));
        return;
    end
    
    y_size = A_size;
    size_in_dim = y_size(DIM);
    if size_in_dim ~= 0,
        y_size(DIM) = 1;
    end
    
    y = zeros(y_size, class(A));
    positions = zeros(y_size);
    
    for i = 1:numel(y),
        pos = MATISSE_raw_ind2sub(y_size, i);
        
        value = A(MATISSE_raw_sub2ind(A_size, pos));
        index = 1;
        for j = 2:size_in_dim,
            test_pos = pos;
            test_pos(DIM) = j;
            value2 = A(MATISSE_raw_sub2ind(A_size, test_pos));
            if value2 < value,
                value = value2;
                index = j;
            end
        end
        
        y(MATISSE_raw_sub2ind(y_size, pos)) = value;
        positions(MATISSE_raw_sub2ind(y_size, pos)) = index;
    end
end
