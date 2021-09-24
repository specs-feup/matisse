function y2 = branch_in_first_loop(X)
  y1 = zeros(1, numel(X));
  for i = 1:numel(X),
  	v = 0;
  	if i > 5,
  		v = i;
  	end
  	y1(1, i) = v;
  end
  y2 = zeros(1, numel(X));
  for j = 1:numel(X),
  	y2(1, j) = y1(1, j) * 2;
  end
end