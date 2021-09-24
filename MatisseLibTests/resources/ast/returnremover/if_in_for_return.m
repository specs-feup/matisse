% See bug #38
function y = if_in_for_return(nint)
  y = zeros(1, nint);
  for i = 1:100,
    y(i) = i;
    if i == nint,
       return;
    end
  end
end