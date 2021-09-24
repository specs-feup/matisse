%!disable loop_fusion_only_related
function tmp2=unused_variable(A),
    [rows, cols] = size(A);
    tmp1 = zeros(rows, cols); tmp2 = zeros(rows, cols);
    for i = 1:rows,
        for j = 1:cols,
            tmp1(i, j) = i + j;
        end
        for j = 1:cols,
            tmp2(i, j) = i + j;
        end
    end
end
