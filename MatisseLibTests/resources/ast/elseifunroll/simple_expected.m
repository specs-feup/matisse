function y = simple(x)
    if x > 10,
        y = 10;
    else
        if x > 5,
            y = 5;
        else
            if x > 0,
                y = 2;
            else
                y = 0;
            end
        end
    end
end