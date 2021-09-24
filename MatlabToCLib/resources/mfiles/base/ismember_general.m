function [y, idx] = ismember_general(a, b)
	y = zeros(size(a));
	idx = zeros(size(a));
	for i = 1:numel(b),
		for j = 1:numel(y),
			if ~y(j) && a(j) == b(i),
				y(j) = 1;
				idx(j) = i;
			end
		end
	end
end