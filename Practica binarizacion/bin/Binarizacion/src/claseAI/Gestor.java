package claseAI;

import claseAI.HerramientasColor.CanalColor;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Guarda los datos de cada canal (R, G, B) en el dominio espacial y en el de
 * frecuencias, y convierte entre ellos con la FFT.
 *
 * @author panch
 */
public class Gestor {

    private final BufferedImage originalImage;
    private final Map<CanalColor, NumeroComplejo[][]> datosEspaciales = new EnumMap<>(CanalColor.class);
    private final Map<CanalColor, NumeroComplejo[][]> datosFrecuencia = new EnumMap<>(CanalColor.class);
    // copia de la FFT sin filtrar, para poder probar otro filtro sin recalcular
    private final Map<CanalColor, NumeroComplejo[][]> frecuenciaOriginal = new EnumMap<>(CanalColor.class);
    private final FFTCalculo fft = new FFTCalculo();

    /**
     * Si la imagen no mide potencia de 2 se redimensiona (por ejemplo 900 -> 512).
     * La transformada directa se calcula aquí mismo.
     */
    public Gestor(BufferedImage imagen) {
        this.originalImage = ImageManager.ajustarAPotenciaDe2(imagen);
        for (CanalColor canal : CanalColor.values()) {
            NumeroComplejo[][] espacial = obtenerDatosPorCanal(originalImage, canal);
            datosEspaciales.put(canal, espacial);
            NumeroComplejo[][] F = fft.calculateFT(espacial, false);
            frecuenciaOriginal.put(canal, F);
            datosFrecuencia.put(canal, copiar(F));
        }
    }

    /** Quita cualquier filtro aplicado: vuelve a la FFT original. */
    public void restaurarFrecuencias() {
        for (CanalColor canal : CanalColor.values())
            datosFrecuencia.put(canal, copiar(frecuenciaOriginal.get(canal)));
    }

    private static NumeroComplejo[][] copiar(NumeroComplejo[][] m) {
        NumeroComplejo[][] c = new NumeroComplejo[m.length][];
        for (int x = 0; x < m.length; x++)
            c[x] = m[x].clone(); // NumeroComplejo es inmutable: basta copiar referencias
        return c;
    }

    /** Un canal de la imagen como matriz [x][y] de complejos (parte imaginaria = 0). */
    private NumeroComplejo[][] obtenerDatosPorCanal(BufferedImage imagen, CanalColor canal) {
        NumeroComplejo[][] datosCanal = new NumeroComplejo[imagen.getWidth()][imagen.getHeight()];
        for (int y = 0; y < imagen.getHeight(); y++)
            for (int x = 0; x < imagen.getWidth(); x++)
                datosCanal[x][y] = new NumeroComplejo(
                        HerramientasColor.obtenerValorPorCanal(imagen.getRGB(x, y), canal), 0);
        return datosCanal;
    }

    /** La imagen ya redimensionada con la que se trabaja. */
    public BufferedImage getImagenTrabajo() {
        return originalImage;
    }

    /**
     * Imagen del espectro (magnitudes).
     *
     * @param cuadrantes  true = mover la frecuencia 0 al centro (shift)
     * @param escalaLog   true = mostrar log(1 + |F|). Sin esto casi todo se ve
     *                    negro, porque el centro es miles de veces mayor que el resto.
     */
    public BufferedImage getFrequencyDomainImage(boolean cuadrantes, boolean escalaLog) {
        int ancho = getWidth();
        int alto = getHeight();
        BufferedImage image = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        int[][] r = new int[ancho][alto], g = new int[ancho][alto], b = new int[ancho][alto];

        for (CanalColor canal : CanalColor.values()) {
            NumeroComplejo[][] F = datosFrecuencia.get(canal);

            // valor máximo del canal para normalizar a 0..255
            double max = 0;
            for (int x = 0; x < ancho; x++)
                for (int y = 0; y < alto; y++)
                    max = Math.max(max, valorVisible(F[x][y], escalaLog));

            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {
                    int ejeX = cuadrantes ? (x + ancho / 2) % ancho : x;
                    int ejeY = cuadrantes ? (y + alto / 2) % alto : y;
                    int v = (int) Math.round(255 * valorVisible(F[ejeX][ejeY], escalaLog) / max);
                    v = HerramientasColor.validarRango(v);
                    switch (canal) {
                        case ROJO: r[x][y] = v; break;
                        case VERDE: g[x][y] = v; break;
                        case AZUL: b[x][y] = v; break;
                    }
                }
            }
        }
        for (int y = 0; y < alto; y++)
            for (int x = 0; x < ancho; x++)
                image.setRGB(x, y, 0xFF000000 | (r[x][y] << 16) | (g[x][y] << 8) | b[x][y]);
        return image;
    }

    private double valorVisible(NumeroComplejo c, boolean escalaLog) {
        return escalaLog ? Math.log1p(c.magnitud()) : c.magnitud();
    }

    /**
     * Filtrar en frecuencia = multiplicar cada coeficiente complejo por H(u,v).
     * El filtro viene centrado (frecuencia 0 en medio), los datos no, así que
     * se aplica el mismo shift de cuadrantes.
     */
    public void aplicarFiltro(NumeroComplejo[][] filtro) {
        int ancho = getWidth();
        int alto = getHeight();
        if (filtro.length != ancho || filtro[0].length != alto)
            throw new IllegalArgumentException("El filtro mide " + filtro.length + "x" + filtro[0].length
                    + " pero la imagen mide " + ancho + "x" + alto);

        for (CanalColor canal : CanalColor.values()) {
            NumeroComplejo[][] F = datosFrecuencia.get(canal);
            for (int a = 0; a < ancho; a++) {
                for (int b = 0; b < alto; b++) {
                    int x = (a + ancho / 2) % ancho;
                    int y = (b + alto / 2) % alto;
                    F[x][y] = F[x][y].multiplicar(filtro[a][b]);
                }
            }
        }
    }

    /** Transformada inversa de los datos de frecuencia (filtrados o no). */
    public BufferedImage obtenerImagenDominioEspacial() {
        int ancho = getWidth();
        int alto = getHeight();
        BufferedImage image = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        for (CanalColor canal : CanalColor.values()) {
            NumeroComplejo[][] espacial = fft.calculateFT(datosFrecuencia.get(canal), true);
            datosEspaciales.put(canal, espacial);

            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {
                    // la parte imaginaria es ~0; los negativos (rebotes del filtro) se recortan a 0
                    int color = (int) Math.round(espacial[x][y].getParteReal());
                    color = HerramientasColor.validarRango(color);
                    color = HerramientasColor.obtenerARGBdeCanal(color, canal);
                    image.setRGB(x, y, HerramientasColor.agregarColores(image.getRGB(x, y), color));
                }
            }
        }
        return image;
    }

    public int getWidth() {
        return originalImage.getWidth();
    }

    public int getHeight() {
        return originalImage.getHeight();
    }
}
