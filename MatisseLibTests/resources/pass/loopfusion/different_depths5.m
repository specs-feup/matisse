%!assume_indices_in_range
%!disable loop_fusion_only_related
function tmp3 = different_depths5(X, a)
  [rows, cols] = size(X);
  tmp1 = zeros(rows, cols);
  tmp2 = zeros(rows, cols);
  tmp3 = zeros(rows, cols);
  for j = 1:cols,
    for i = 1:rows,
      tmp1(i, j) = i;
    end
    for i = 1:rows,
      tmp2(i, j) = j;
    end
  end
  for i = 1:numel(tmp3),
    tmp3(i) = tmp1(i) + tmp2(i);
  end
end
