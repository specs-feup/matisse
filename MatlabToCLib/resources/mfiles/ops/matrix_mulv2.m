function result = matrix_mulv2(X, Y)

	result=zeros(size(X,1),size(Y,2));

	for ii=1:size(Y,2)
		result(:,ii)=X*Y(:,ii);
	end

end