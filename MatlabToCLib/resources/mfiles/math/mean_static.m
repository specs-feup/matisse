function y = mean_static(x,dim)
	y = sum(x,dim)/size(x,dim);
end