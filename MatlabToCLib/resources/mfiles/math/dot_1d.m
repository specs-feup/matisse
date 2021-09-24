function acc = dot_1d(A, B)
    acc = 0.0;
    for i = 1:numel(A),
        acc = acc + A(i) * B(i);
    end
end