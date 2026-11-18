package claseAI;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Filtros en el dominio de la frecuencia, CENTRADOS: la frecuencia 0 está en
 * el centro de la matriz (Gestor.aplicarFiltro hace el shift).
 *
 * D(u,v) = distancia del punto (u,v) al centro.
 */
public class Filtros {

    private static double distancia(int u, int v, Dimension dim) {
        return Math.hypot(u - dim.width / 2.0, v - dim.height / 2.0);
    }

    /** H = 1 si D <= d0, si no 0. Deja pasar solo frecuencias bajas (difumina). */
    public static NumeroComplejo[][] crearFiltroIdealPasaBajas(double d0, Dimension dim) {
        NumeroComplejo[][] f = new NumeroComplejo[dim.width][dim.height];
        for (int u = 0; u < dim.width; u++)
            for (int v = 0; v < dim.height; v++)
                f[u][v] = new NumeroComplejo(distancia(u, v, dim) <= d0 ? 1 : 0, 0);
        return f;
    }

    /** H = 1 - pasa bajas. Deja solo bordes y detalles finos. */
    public static NumeroComplejo[][] crearFiltroIdealPasaAltas(double d0, Dimension dim) {
        NumeroComplejo[][] f = new NumeroComplejo[dim.width][dim.height];
        for (int u = 0; u < dim.width; u++)
            for (int v = 0; v < dim.height; v++)
                f[u][v] = new NumeroComplejo(distancia(u, v, dim) <= d0 ? 0 : 1, 0);
        return f;
    }

    /** H = 1 / (1 + (D/d0)^(2n)). Corte suave: casi sin anillos. */
    public static NumeroComplejo[][] crearFiltroButterWorth(double d0, Dimension dim, int orden) {
        NumeroComplejo[][] f = new NumeroComplejo[dim.width][dim.height];
        for (int u = 0; u < dim.width; u++)
            for (int v = 0; v < dim.height; v++) {
                double d = distancia(u, v, dim);
                f[u][v] = new NumeroComplejo(1.0 / (1.0 + Math.pow(d / d0, 2.0 * orden)), 0);
            }
        return f;
    }

    /** Butterworth pasa altas = 1 - Butterworth pasa bajas. */
    public static NumeroComplejo[][] crearFiltroButterWorthPasaAltas(double d0, Dimension dim, int orden) {
        NumeroComplejo[][] f = crearFiltroButterWorth(d0, dim, orden);
        for (int u = 0; u < dim.width; u++)
            for (int v = 0; v < dim.height; v++)
                f[u][v] = new NumeroComplejo(1.0 - f[u][v].getParteReal(), 0);
        return f;
    }

    /** Dibuja el filtro en grises: blanco = deja pasar (1), negro = bloquea (0). */
    public static Image complejoToImagen(NumeroComplejo[][] filtro) {
        int ancho = filtro.length, alto = filtro[0].length;
        BufferedImage bi = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < ancho; x++)
            for (int y = 0; y < alto; y++) {
                int v = HerramientasColor.validarRango((int) Math.round(255 * filtro[x][y].getParteReal()));
                bi.setRGB(x, y, (v << 16) | (v << 8) | v);
            }
        return bi;
    }
}
