if (*out != NULL) {
	free((*out)->data);
} else {
	*out = malloc(sizeof(<STRUCT_NAME>));
	if (*out == NULL) {
		fprintf(stderr, "Failed to allocate output string.\n");
		abort();
	}
}

assert(in != NULL);

(*out)->length = in->length;
(*out)->data = malloc(in->length + 1);
strcpy((*out)->data, in->data);

return *out;
