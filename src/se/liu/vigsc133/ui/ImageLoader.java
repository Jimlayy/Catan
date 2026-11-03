package se.liu.vigsc133.ui;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * Loads images that are packaged with the program, through the class loader, so that
 * they are found regardless of where the program is installed or whether it runs from
 * a JAR file.
 */
public final class ImageLoader
{
    private ImageLoader() {}

    /**
     * @param path the image's path inside the resources folder, for example "images/board.png"
     * @throws FileNotFoundException if the image is not packaged with the program
     */
    public static ImageIcon load(String path) throws FileNotFoundException {
	URL url = ClassLoader.getSystemResource(path);
	if (url == null) {
	    throw new FileNotFoundException("Missing image resource: " + path);
	}
	return new ImageIcon(url);
    }
}