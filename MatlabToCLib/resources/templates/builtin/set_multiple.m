function result = <FUNCTION_NAME>(X<INDEX_LIST>, Y)

	% Get indexes to set
	indexes = <INDEXES_CALL>;

	%TODO: Generalize and replace with 'X(indexes) = Y;'
	for i=1:numel(indexes)
		X(indexes(i)) = Y<IS_SCALAR>;
	end
	result = X;
end