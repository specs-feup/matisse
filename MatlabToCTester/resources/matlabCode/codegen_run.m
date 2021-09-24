% -config:lib to generate a C static library
% -c to generate only a C library
%cfg = coder.config('lib','DynamicMemoryAllocation',<DYNAMIC_MEMORY>, 'SaturateOnIntegerOverflow', false, 'EnableAutoExtrinsicCalls', false);
%clearvars cfg;
%cd '<SCRIPT_FOLDER>';

saved_path = path;
rmpath([matlabroot '\toolbox\embeddedcoder']);

cfg = coder.config('lib');
cfg.DynamicMemoryAllocation = <DYNAMIC_MEMORY>;
cfg.SaturateOnIntegerOverflow = false;
cfg.EnableAutoExtrinsicCalls = false;

codegen '<FUNCTION_NAME>' -config cfg -args {<INPUT_TYPES>} -c;

path(saved_path);

% coder.typeof(double(0), [Inf Inf]),  coder.typeof(double(0), [Inf Inf])