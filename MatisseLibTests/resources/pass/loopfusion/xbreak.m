function [y1,y2] = xbreak(X, a)
  y1 = zeros(1, numel(X));
  for i = 1:numel(X),
  	y1(i) = a;
  	break;
  end
  y2 = zeros(1, numel(X));
  for i = 1:numel(X),
  	y2(i) = y1(i) * 2;
  end
end