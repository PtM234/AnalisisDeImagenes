package claseAI;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Abrir, convertir y redimensionar imágenes.
 *
 * @author panch
 */
public class ImageManager {

    /** Abre un selector de archivos. Devuelve null si se cancela o falla. */
    public static BufferedImage openImage() {
        FileNameExtensionFilter filtro =
                new FileNameExtensionFilter("Imagenes", "jpg", "jpeg", "png", "bmp");
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(filtro);
        selector.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (selector.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return null;

        return openImage(selector.getSelectedFile());
    }

    /** Abre una imagen desde un archivo (útil para pruebas sin ventana). */
    public static BufferedImage openImage(File archivo) {
        try {
            BufferedImage bi = ImageIO.read(archivo);
            return bi == null ? null : toBufferedImage(bi);
        } catch (IOException ex) {
            Logger.getLogger(ImageManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /** Un BufferedImage ya es un Image; no hace falta convertir nada. */
    public static Image toImage(BufferedImage bi) {
        return bi;
    }

    /** Convierte cualquier Image a BufferedImage RGB. */
    public static BufferedImage toBufferedImage(Image imagen) {
        if (imagen instanceof BufferedImage
                && ((BufferedImage) imagen).getType() == BufferedImage.TYPE_INT_RGB)
            return (BufferedImage) imagen;

        BufferedImage bi = new BufferedImage(imagen.getWidth(null), imagen.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(imagen, 0, 0, null);
        g.dispose();
        return bi;
    }

    public static BufferedImage redimensionar(BufferedImage bi, int ancho, int alto) {
        BufferedImage r = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = r.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bi, 0, 0, ancho, alto, null);
        g.dispose();
        return r;
    }

    /**
     * La FFT necesita medidas potencia de 2. Cada lado se lleva a la mayor
     * potencia de 2 que no lo supere (900 -> 512, 640 -> 512, 256 -> 256).
     */
    public static BufferedImage ajustarAPotenciaDe2(BufferedImage bi) {
        int ancho = Integer.highestOneBit(bi.getWidth());
        int alto = Integer.highestOneBit(bi.getHeight());
        if (ancho == bi.getWidth() && alto == bi.getHeight())
            return toBufferedImage(bi);
        return redimensionar(bi, ancho, alto);
    }
}
