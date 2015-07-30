package version2.prototype.processor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileSystemsT {

    public static void main(String args[])
    {
        String inputFolder = "D:\\testInputs";
        String outputFolder = "D:\\TestFiles\\output";

        //create outputDirectory
        File outputDir = new File(outputFolder);
        if (!outputDir.exists())
        {   try {
            FileUtils.forceMkdir(outputDir);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   }
        try {
            for (File f : (new File(inputFolder)).listFiles()) {
                FileUtils.copyFileToDirectory(f, outputDir);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
