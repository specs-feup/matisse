function result = <FUNCTION_NAME>(X<NON_WILDCARD_INDEXES>, Y)

	<LAST_INDEX>
	% Get indexes to set
	indexes = <INDEXES_CALL>;

	% Check is matrix is big enough 
%	max_index = max(indexes);
%	if(max_index > numel(X)
%		temp = zeros(
%	end
	
	%TODO: Generalize and replace with 'X(indexes) = Y;'
	for i=1:numel(Y)
		X(indexes(i)) = Y(i)
	end
	result = X;
end