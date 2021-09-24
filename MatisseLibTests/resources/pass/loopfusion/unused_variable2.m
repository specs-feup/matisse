function tmp2=unused_variable2(A)
    [rows, cols] = size(A);
    tmp1 = zeros(rows, cols);
    tmp2 = zeros(rows, cols);
    for i = 1:rows,
        for j = 1:cols,
            tmp1(i, j) = i + j;
        end
    end
    for i = 1:numel(tmp2)
        tmp2(i) = 1;
    end
end
