function y = test(X)
  a = size(X, 1);
  b = size(X, 2);
  
  y = zeros(a, b);
  for i = 1:a,
    for j = 1:b,
      y(i, j) = i;
    end
  end
end
