function [y1, y2] = nested(X)
	[s1, s2] = size(X);
	y1 = zeros(s1, s2);
	for i = 1:s1,
		for j = 1:s2,
			y1(i, j) = i + j;
		end
	end
	y2 = zeros(s1, s2);
	for i = 1:s1,
		for j = 1:s2,
			y2(i, j) = y1(i, j) * j;
		end
	end
end