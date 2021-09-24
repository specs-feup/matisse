function y = if_return(x)
    if x == 1
        y = 2;
        return;
    elseif x == 2
    	y = 3;
    	return;
    else
    	y = 4;
    	return;
    end
    y = 1;
end