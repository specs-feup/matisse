% Init file
fid = fopen('<OUTPUT_FILENAME>','w');
fprintf(fid, '');
fclose(fid);

% Number of measures to take
%executions = 5;

% Append header
fid = fopen('times.csv','a');
fprintf(fid, 'sep=;\nProgram');
for i=1:<EXECUTIONS>
	fprintf(fid, ';Exec %d', i);
end
fprintf(fid, '\n');
fclose(fid);



% Tests

<TESTS>