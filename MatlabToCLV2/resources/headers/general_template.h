#ifndef MATISSE_CL_H_
#define MATISSE_CL_H_

// For compatibility with older versions
#define CL_USE_DEPRECATED_OPENCL_1_1_APIS
#define CL_USE_DEPRECATED_OPENCL_1_2_APIS

#ifdef __APPLE__
#include <OpenCL/opencl.h>
#else
#include <CL/cl.h>
#endif

<CL_EXPORT_LIB_DEFINITION>

struct MATISSE_cl_descriptor {
   cl_context context;
   cl_platform_id platform;
   cl_device_id device;
   cl_command_queue command_queue;
   cl_program program;
<KERNELS>};
extern struct MATISSE_cl_descriptor MATISSE_cl;

#define CHECK(functionName, ...) validate(__FILE__, __LINE__, #functionName, functionName(__VA_ARGS__))
#define CHECK_CODE(functionName, errorCode) validate(__FILE__, __LINE__, #functionName, errorCode)

const char* get_error_message(cl_int error_code);
const char* get_device_type(cl_device_type device_type);
void validate(const char* filename, int line, const char* functionName, cl_int errCode);

void MATISSE_cl_register_kernel_event(cl_event evt);
void MATISSE_cl_register_host_to_device_data_transfer_event(cl_event evt);
void MATISSE_cl_register_device_to_host_data_transfer_event(cl_event evt);

MATISSE_CL_LIB_EXPORT void MATISSE_cl_initialize(int argc, char* argv[]);
void MATISSE_cl_print_times();
void MATISSE_cl_reset_times();

#endif
