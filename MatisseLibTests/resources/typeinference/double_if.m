function [a, b] = double_if(x)
	if 1,
		a = 1;
	else
		a = 0;
	end
	if x,
		b = 1;
	else
		b = 0;
	end
end