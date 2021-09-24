	if (str1->length != str2->length) {
		return 0;
	}

	return strcmp(str1->data, str2->data) == 0;
