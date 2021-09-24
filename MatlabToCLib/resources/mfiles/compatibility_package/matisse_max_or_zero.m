function y = matisse_max_or_zero(X)
	y = 0;
	for i = 1:numel(X),
		y = max(y, X(i));
	end
end