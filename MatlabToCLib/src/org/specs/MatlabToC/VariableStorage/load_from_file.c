// void load_raw_data_from_file (const char* varname, int num_elements, <ELEMENT_TYPE>* out)

    size_t element_size = sizeof(<ELEMENT_TYPE>);
    size_t read_total_elements = 0;
    char* filename;
    char* relative_filename;
    
    size_t elements_to_read;
    size_t max_read;
    size_t read_elements;
        
    relative_filename = malloc(sizeof("data/.dat") + strlen(varname));
    sprintf(relative_filename, "data/%s.dat", varname);
    filename = get_absolute_filename(relative_filename);
    free(relative_filename);
    FILE* file = fopen(filename, "rb");
    if (file == NULL) {
       fprintf(stderr, "Could not open file %s.\n", filename);
       exit(1);
    }
    
    if (<OUT_DATA> == NULL) {
       fprintf(stderr, "Output data is null.\n");
       exit(1);
    }
    
    while (read_total_elements < num_elements) {
        elements_to_read = num_elements - read_total_elements;
        max_read = 1024 / element_size;
        if (elements_to_read > max_read) {
            elements_to_read = max_read;
        }
        read_elements = fread(<OUT_DATA> + read_total_elements, element_size, elements_to_read, file);
        if (read_elements == 0) {
            // We should use %zu instead of %d for size_t printfs, but unfortunately some compilers
            // (such as MinGW) do not support it.
            fprintf(stderr, "Could not load file %s. Read %d of %d:\n%s\n", filename, (int) read_total_elements, (int) num_elements, strerror(errno));
            exit(1);
        }
        read_total_elements += read_elements;
    }
    
    free(filename);
    fclose(file);
