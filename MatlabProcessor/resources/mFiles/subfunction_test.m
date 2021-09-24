function [out] = subfunction_test(arg1)

	out = sub1(arg1);

end

function [out] = sub1(a)

	out = a * a;

end