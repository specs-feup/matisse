function y2=size_aware(n)
    y1 = matisse_new_array_from_dims(1, n);
    for i = 1:n,
        y1(i) = 1;
    end
    s = size(y1);
    y2 = matisse_new_array(s);
    for i = 1:numel(y2),
        y2(i) = y1(i) * 2;
    end
end
