function nested(x)
    while x > 0,
        w = x;
        while w > 0,
            w = 0;
        end
        x = x - 1;
    end
end