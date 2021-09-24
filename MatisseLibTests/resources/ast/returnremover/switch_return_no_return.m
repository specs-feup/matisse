function y = switch_return(x)
    return_1 = 0;
    switch x
        case 1
            y = 1;
            return_1 = 1;
        case 2
            y = 2;
        case 3
            y = 3;
            return_1 = 1;
        otherwise
            y = 4;
    end
    if ~return_1
       y = y + 1; 
    end
end