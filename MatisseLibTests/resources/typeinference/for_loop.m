function [x, y] = for_loop()
    y = 3;
    for i = 1:100,
        x = i;
        y = i + 1;
    end
end