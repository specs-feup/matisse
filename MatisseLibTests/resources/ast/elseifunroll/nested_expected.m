function y = nested(x)
    if x > 0,
        if x < 5,
            y = 0;
        else
            if x < 3
                y = 2;
            end
        end
    else
        if x < 2
            if x < 5,
                y = 1;
            else
                if x > 3,
                    y = 3;
                else
                end
            end
        end
    end
end