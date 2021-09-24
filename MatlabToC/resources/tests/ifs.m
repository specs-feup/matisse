function [result] = ifs(a1, a2)

if a1 == 0
  result = a2;
elseif a1 == 1
  if a2 == 2
    result = a2 + a1;
  else
    result = a2 + a2;
  end
else
  result = a2 + 10;
end