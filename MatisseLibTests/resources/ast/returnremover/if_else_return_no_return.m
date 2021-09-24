function y = if_return(x)
	return_1 = 0;
    if x == 1
        y = 2;
        return_1 = 1;
    elseif x == 2
    	y = 3;
    	return_1 = 1;
    else
    	y = 4;
    	return_1 = 1;
    end
    if ~return_1
    	y = 1;
    end
end