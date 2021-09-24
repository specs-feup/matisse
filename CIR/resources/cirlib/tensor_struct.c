/**
 * @struct tensor
 *
 * Represents a tensor.
 * Has information about the shape of the tensor and the length of its linearized version.
 *
 */
typedef struct tensor_struct_<SMALL_ID> {

	<DATA_TYPE>* <TENSOR_DATA>;
	int <TENSOR_LENGTH>;

	int* <TENSOR_SHAPE>;
	int <TENSOR_DIMS>;
	int <TENSOR_OWNS_DATA>;

} <TENSOR_NAME>;