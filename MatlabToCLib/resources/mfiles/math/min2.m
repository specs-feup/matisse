function result = min2(in1, in2)
    result = (in1 < in2) .* in1 + (in1 >= in2) .* in2;
end