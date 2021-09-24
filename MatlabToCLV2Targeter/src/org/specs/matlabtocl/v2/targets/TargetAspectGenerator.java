package org.specs.matlabtocl.v2.targets;

import java.io.File;

import pt.up.fe.specs.util.SpecsIo;

public class TargetAspectGenerator {


    public static File generateTargetAwareAspect(File aspectFilePath, String test, File generatedAspects,
            TargetResource target) {
        generatePlatformSettings(generatedAspects, target);

        File laraWrapperFile = new File(generatedAspects, "lara_wrapper.lara");
        String laraWrapperContent = SpecsIo.getResource(LaraWrapperResource.LARA_WRAPPER);

        String userAspectName = null;
        if (aspectFilePath.isFile()) {
            String fileName = aspectFilePath.getName();
            userAspectName = SpecsIo.removeExtension(fileName);
        } else if (aspectFilePath.isDirectory()) {
            if (new File(aspectFilePath, test + ".lara").exists()) {
                userAspectName = test;
            }
        }

        laraWrapperContent = laraWrapperContent
                .replace("<IMPORT_USER_ASPECT>",
                        userAspectName == null ? "" : "import " + userAspectName + ";")
                .replace("<CALL_USER_ASPECT>", userAspectName == null ? "" : "call " + userAspectName + "();");

        SpecsIo.write(laraWrapperFile, laraWrapperContent);
        return laraWrapperFile;
    }

	public static void generatePlatformSettings(File generatedAspectFolder, TargetResource target) {
		File targetLaraFile = new File(generatedAspectFolder, "PlatformSettings.lara");
        String targetLaraContents = SpecsIo.getResource(target);
        SpecsIo.write(targetLaraFile, targetLaraContents);
	}
}
