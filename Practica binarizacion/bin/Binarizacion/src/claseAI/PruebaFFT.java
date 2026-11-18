package claseAI;

/**
 * Pruebas sin ventanas para comprobar que la FFT está bien:
 *  1) el tramo de 8 píxeles del ejemplo contra la DFT lenta (fórmula directa)
 *  2) ida y vuelta 2D: inversa(directa(datos)) == datos
 */
public class PruebaFFT {

    public static void main(String[] args) {
        // Tramo del borde (renglón 256, x = 256..263, canal rojo)
        double[] tramo = {255, 254, 253, 255, 222, 55, 19, 18};

        NumeroComplejo[] datos = new NumeroComplejo[tramo.length];
        for (int i = 0; i < tramo.length; i++)
            datos[i] = new NumeroComplejo(tramo[i], 0);

        NumeroComplejo[] lenta = dftLenta(datos);
        new FFTCalculo().fft1D(datos, false);

        System.out.println("k   FFT              DFT lenta");
        double errorMax = 0;
        for (int k = 0; k < datos.length; k++) {
            System.out.printf("%d   %-16s %s%n", k, datos[k], lenta[k]);
            errorMax = Math.max(errorMax, datos[k].restar(lenta[k]).magnitud());
        }
        System.out.printf("Error máximo FFT vs DFT: %.2e%n%n", errorMax);

        // Ida y vuelta 2D con datos aleatorios de 8x16 (no cuadrada a propósito)
        int w = 8, h = 16;
        NumeroComplejo[][] img = new NumeroComplejo[w][h];
        java.util.Random rnd = new java.util.Random(1);
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                img[x][y] = new NumeroComplejo(rnd.nextInt(256), 0);
        FFTCalculo fft = new FFTCalculo();
        NumeroComplejo[][] vuelta = fft.calculateFT(fft.calculateFT(img, false), true);
        double err2 = 0;
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                err2 = Math.max(err2, vuelta[x][y].restar(img[x][y]).magnitud());
        System.out.printf("Error máximo ida y vuelta 2D (8x16): %.2e%n", err2);
    }

    /** X[k] = sum x[n] * e^(-2*pi*i*k*n/N), costo N^2. */
    static NumeroComplejo[] dftLenta(NumeroComplejo[] x) {
        int n = x.length;
        NumeroComplejo[] X = new NumeroComplejo[n];
        for (int k = 0; k < n; k++) {
            NumeroComplejo suma = new NumeroComplejo();
            for (int m = 0; m < n; m++) {
                double ang = -2 * Math.PI * k * m / n;
                suma = suma.sumar(x[m].multiplicar(new NumeroComplejo(Math.cos(ang), Math.sin(ang))));
            }
            X[k] = suma;
        }
        return X;
    }
}
