%FIND_STATIC   Find the first K indexes of non-zero elements.
%
%	This version is for static implementations, as is more limited than the original version.
%
function I = find_static(X, K)

	for i=1:numel(X)
			if(X(i) ~= 0)
				I = i;
				return;
			end
	end
	
	I = 0;

end
