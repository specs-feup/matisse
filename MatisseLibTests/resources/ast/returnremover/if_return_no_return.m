function y = if_return(x)
    return_1 = 0;
    if x
        y = 2;
        return_1 = 1;
    end
    if ~return_1
        y = 1;
    end
end