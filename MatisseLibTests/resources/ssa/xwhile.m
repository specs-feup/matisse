function y = xwhile(x)
    y = 0;
    while rand() > x,
        y = y + x;
        if x < 0,
            y = -1;
            break;
        end
    end
end