if (*result == NULL) {
	return;
}
free((*result)-><CELL_DATA>);
free((*result)-><CELL_SHAPE>);
free(*result);
*result = NULL;
