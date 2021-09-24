function total = getCurrentEnergy()

	% Check if folder exists
	raplFolder = '/sys/class/powercap/intel-rapl/';
	if(exist(raplFolder) ~= 7)
		disp(strcat('RAPL folder is not available: ', raplFolder));
		return;
	end
	
	% Get RAPL folders for each package
	%listing = dir(raplFolder);
	%listing
	raplFolderListing = eval('ls(raplFolder)');
	folders = strsplit(raplFolderListing);
	
	filter = strncmp(folders, 'intel-rapl:', numel('intel-rapl:'));
	packageFolders = folders(filter);
	
	energyReadings = zeros(size(packageFolders));
	
	for i = 1:numel(packageFolders)
		energyFile = strcat(packageFolders{i}, '/energy_uj');
		energyFilepath = strcat(raplFolder, energyFile);
		[status, output] = system(cstrcat('cat ',energyFilepath));
		energyReadings(i) = str2num(output);
	end

	total = sum(energyReadings);

end