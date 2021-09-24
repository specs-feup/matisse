% See bug #38
function y = if_in_for_return(nint)
  return_1 = 0;
  y = zeros(1, nint);
  for i = 1:100,
    y(i) = i;
    if i == nint,
      return_1 = 1;
    end
    
    if return_1,
      break;
    end
  end
end