function y2 = branch_in_first_loop2(X)
  y1 = zeros(1, numel(X));
  for i = 1:numel(X),
  	if i > 5,
  		y1(1, i) = i;
  	end
  end
  y2 = zeros(1, numel(X));
  for j = 1:numel(X),
  	y2(1, j) = y1(1, j) * 2;
  end
end