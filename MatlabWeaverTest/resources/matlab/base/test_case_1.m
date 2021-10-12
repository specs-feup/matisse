%@Section before function
function [out1, out2] = test_case_1(arg1, arg2, arg3)
	%@Section inside function
	out1 = 0;
	%{
	   @Section in comment block
	%}
	
	for i=1:10
		%@Section inside for
		out2 = out1;
	end
	
end

%@Section before foo2
function out1 = foo2(arg1)
	%@Section inside foo2
end
%@Section after foo2

