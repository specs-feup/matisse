%!assume_indices_in_range
function [y1, y2] = different_depths3(a, b)
	y1 = matisse_new_array_from_dims(b, a);
	for i = 1:a,
		for j = 1:b,
			y1(j, i) = i + j;
		end
	end
	y2 = matisse_new_array(size(y1));
	for i = 1:numel(y2),
		y2(i) = y1(i) + 1;
	end
end