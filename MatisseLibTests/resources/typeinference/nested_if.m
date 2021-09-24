function a = nested_if(x)
	a = 1;
	if 1 < 2,
		if x > 1,
			a = 0;
		end
	end
end