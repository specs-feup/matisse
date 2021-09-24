%!assume_indices_in_range
function y = padarray(A, pad, arg3, arg4)
	value = 0;

	% TODO: handle 'circular', 'replicate', 'symmetric'

	if nargin < 2,
		error('Not enough arguments');
	elseif nargin == 2,
		type = 'both';
	elseif nargin == 3,
		if strcmp(class(arg3), 'char'),
			type = arg3;
		else
			value = arg3;
			type = 'both';
		end
	elseif nargin == 4,
		type = arg4;
		if ~strcmp(class(arg3), 'char'),
			value = arg3;
		end
	end

	if strcmp(type, 'pre'),
		put_prefix = 1;
		put_postfix = 0;
	elseif strcmp(type, 'post'),
		put_prefix = 0;
		put_postfix = 1;
	elseif strcmp(type, 'both'),
		put_prefix = 1;
		put_postfix = 1;
	else
		error('Expected ''pre'', ''post'' or ''both''');
	end

	% TODO: Validate pad is vector.
	pad = pad(:).';

	A_ndims = ndims(A);
	pad_numel = numel(pad);
	y_ndims = max(A_ndims, pad_numel);
	A_size = [size(A), zeros(1, y_ndims - A_ndims)];
	pad_size = [pad, zeros(1, y_ndims - pad_numel)];
	pad_size = pad_size.';

	y_size = A_size + pad_size * (put_prefix + put_postfix);

	y = zeros(y_size, class(A));
	y(:) = value;
    
	for i = 1:numel(A),
		mat_index = MATISSE_raw_ind2sub(A_size, i) + put_prefix * pad_size;
        y_index = MATISSE_raw_sub2ind(y_size, mat_index);
		y(y_index) = A(i);
	end
end