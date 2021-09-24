<DATA_TYPE>* data = <DATA>;
int* index = <INDEX>;

if (end > begin) {
	<DATA_TYPE>* pivot;
	int l;
	int r;
	<DATA_TYPE> old_value;
	int old_index;

	pivot = data + begin;
	l = begin + 1;
	r = end;

	while (l < r) {
		if (data[l] <COMPARISON_BEFORE> *pivot || (data[l] == *pivot && index[l] <= index[begin])) {
			++l;
		} else if (data[r] <COMPARISON_AFTER> *pivot || (data[r] == *pivot && index[r] > index[begin])) {
			--r;
		} else {
			old_value = data[l];
			old_index = index[l];
			data[l] = data[r];
			index[l] = index[r];
			data[r] = old_value;
			index[r] = old_index;
		}
	}
	--l;

	old_value = data[begin];
	old_index = index[begin];
	data[begin] = data[l];
	index[begin] = index[l];
	data[l] = old_value;
	index[l] = old_index;

	if (data[l] <COMPARISON_AFTER> data[r] || (data[l] == data[r] && index[l] > index[r])) {
		old_value = data[l];
		old_index = index[l];
		data[l] = data[r];
		index[l] = index[r];
		data[r] = old_value;
		index[r] = old_index;
	}

	<FUNCTION_NAME>(dataMatrix, indexMatrix, begin, l);
	<FUNCTION_NAME>(dataMatrix, indexMatrix, r, end);
}
