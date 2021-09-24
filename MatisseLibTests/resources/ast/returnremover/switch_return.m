function y = switch_return(x)
    switch x
        case 1
            y = 1;
            return;
        case 2
            y = 2;
        case 3
            y = 3;
            return;
        otherwise
            y = 4;
    end
    y = y + 1;
end