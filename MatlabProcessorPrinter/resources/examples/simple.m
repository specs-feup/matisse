%!parallel
function y = test(X, a)
  y = sum(X(:) + a);
end
