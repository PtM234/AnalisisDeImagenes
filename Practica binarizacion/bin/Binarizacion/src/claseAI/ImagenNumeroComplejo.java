package claseAI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Versión de un solo canal (rojo) de Gestor. No la usa FFTPrueba; se deja
 * corregida por si la necesitas para una práctica en escala de grises.
 *
 * @author panch
 */
public class ImagenNumeroComplejo {

    NumeroComplejo[][] spatialImageData;
    NumeroComplejo[][] freqImageData;
    Dimension dim;

    public ImagenNumeroComplejo(Image imagenOriginal) {
        BufferedImage bi = ImageManager.ajustarAPotenciaDe2(ImageManager.toBufferedImage(imagenOriginal));
        dim = new Dimension(bi.getWidth(), bi.getHeight());
        spatialImageData = new NumeroComplejo[bi.getWidth()][bi.getHeight()];
        leerDatosImagenOriginal(bi);
        freqImageData = new FFTCalculo().calculateFT(spatialImageData, false);
    }

    private void leerDatosImagenOriginal(BufferedImage bi) {
        for (int x = 0; x < dim.width; x++)
            for (int y = 0; y < dim.height; y++)
                spatialImageData[x][y] = new NumeroComplejo(new Color(bi.getRGB(x, y)).getRed(), 0);
    }

    /** Espectro en escala logarítmica, en grises. */
    public BufferedImage getFrequImagenDominioImage(boolean isOffset) {
        int w = dim.width, h = dim.height;
        double max = 0;
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                max = Math.max(max, Math.log1p(freqImageData[x][y].magnitud()));

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int ox = isOffset ? (x + w / 2) % w : x;
                int oy = isOffset ? (y + h / 2) % h : y;
                int v = (int) Math.round(255 * Math.log1p(freqImageData[ox][oy].magnitud()) / max);
                image.setRGB(x, y, (v << 16) | (v << 8) | v);
            }
        return image;
    }

    /** Regresa al dominio espacial con la transformada INVERSA. */
    public BufferedImage getEspacialImagenDominioImage() {
        spatialImageData = new FFTCalculo().calculateFT(freqImageData, true);
        BufferedImage image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < dim.height; y++)
            for (int x = 0; x < dim.width; x++) {
                int v = HerramientasColor.validarRango((int) Math.round(spatialImageData[x][y].getParteReal()));
                image.setRGB(x, y, (v << 16) | (v << 8) | v);
            }
        return image;
    }
}
