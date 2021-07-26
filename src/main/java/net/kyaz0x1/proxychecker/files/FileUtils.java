package net.kyaz0x1.proxychecker.files;

import net.kyaz0x1.proxychecker.files.type.FileExtensionType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtils {

    public static boolean hasExtension(File file, FileExtensionType type){
        final String name = file.getName();
        return name.endsWith(type.getExtension());
    }

    public static void write(String value, File file){
        try {
            final PrintWriter writer = new PrintWriter(file);
            writer.println(value);
            writer.flush();
            writer.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

}