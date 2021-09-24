function y2 = simple2d_constant(X)
  y1 = zeros(1, numel(X));
  for i = 1:numel(X),
  	y1(1, i) = i;
  end
  y2 = zeros(1, numel(X));
  for j = 1:numel(X),
  	y2(1, j) = y1(1, j) * 2;
  end
end