	if (*outStr != NULL) {
		// FIXME: See if size is enough.
		free((*outStr)->data);
	} else {
		*outStr = malloc(sizeof(matlab_string));
		if (*outStr == NULL) {
			fprintf(stderr, "Error: Failed to allocate space for string\n");
			abort();
		}
	}

	int length = strlen(str);
	(*outStr)->length = length;
	(*outStr)->data = malloc(length + 1);
	if ((*outStr)->data == NULL) {
		fprintf(stderr, "Error: Failed to allocate space for string data (length=%d)\n", length);
		free(*outStr);
		abort();
	}
	strcpy((*outStr)->data, str);

	return *outStr;
