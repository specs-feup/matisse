function result = fix_general(X)

	if(X < 0)
		result = ceil(X);
	else
		result = floor(X);
	end

end