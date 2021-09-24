function A = dead_branch()
	A = 1;
	if A < 0
		zeros(B);
		A = 2;
	end
end