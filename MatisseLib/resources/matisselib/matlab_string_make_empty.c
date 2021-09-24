if (*out != NULL) {
	free((*out)->data);
} else {
	*out = malloc(sizeof(<STRUCT_NAME>));
}

(*out)->length = 0;
(*out)->data = malloc(1);
(*out)->data[0] = '\0';
return *out;
