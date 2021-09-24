%!assume_indices_in_range
function y2 = simple2d_no_fuse(X)
  s = numel(X) + 1;
  y1 = zeros(1, s);
  for i = 1:numel(X),
  	y1(1, i) = i;
  end
  y2 = zeros(1, s);
  for j = 1:numel(X),
  	y2(1, j) = y1(1, j + 1) * 2;
  end
end