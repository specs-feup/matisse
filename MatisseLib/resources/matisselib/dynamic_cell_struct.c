/**
 * @struct cell
 *
 * Represents a cell.
 * Has information about the shape of the cell and the length of its linearized version.
 *
 */
typedef struct dynamic_cell_struct_<SMALL_ID> {

	<DATA_TYPE>* <CELL_DATA>;
	int <CELL_LENGTH>;

	int* <CELL_SHAPE>;
	int <CELL_DIMS>;

} <CELL_NAME>;