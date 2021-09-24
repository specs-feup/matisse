function y = multiple_instantiation()
	A = f(zeros(1, 1));
	y = f(zeros(A, 1, 'int32'));
end

function y = f(x)
	y = x(1);
end