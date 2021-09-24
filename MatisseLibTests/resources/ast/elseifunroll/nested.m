function y = nested(x)
    if x > 0,
        if x < 5,
            y = 0;
        elseif x < 3
            y = 2;
        end
    elseif x < 2
        if x < 5,
            y = 1;
        elseif x > 3,
            y = 3;
        else
        end
    end
end