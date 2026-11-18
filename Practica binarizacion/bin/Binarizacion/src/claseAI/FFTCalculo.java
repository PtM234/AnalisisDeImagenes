package claseAI;

/**
 * FFT 2D (Cooley-Tukey iterativa, radix 2).
 *
 * La transformada 2D se calcula por separabilidad: primero la FFT 1D de cada
 * renglón y después la FFT 1D de cada columna del resultado.
 *
 * Las dimensiones deben ser potencias de 2 (por ejemplo 512x512, 256x512...).
 *
 * Escala: la directa no divide; la inversa divide entre (ancho * alto), así
 * que ida y vuelta devuelven exactamente los datos originales.
 *
 * @author panch
 */
public class FFTCalculo {

    /**
     * @param imagenOriginal datos en [x][y]
     * @param inversa        false = directa (espacio -> frecuencia),
     *                       true  = inversa (frecuencia -> espacio)
     */
    public NumeroComplejo[][] calculateFT(NumeroComplejo[][] imagenOriginal, boolean inversa) {
        int anchoImagen = imagenOriginal.length;
        int altoImagen = imagenOriginal[0].length;
        validarPotenciaDe2(anchoImagen, "ancho");
        validarPotenciaDe2(altoImagen, "alto");

        // copia para no modificar los datos de entrada
        NumeroComplejo[][] imagenTransformada = new NumeroComplejo[anchoImagen][altoImagen];
        for (int x = 0; x < anchoImagen; x++)
            for (int y = 0; y < altoImagen; y++)
                imagenTransformada[x][y] = imagenOriginal[x][y];

        // 1) FFT de cada renglón (y fijo, x varía: longitud = ancho)
        NumeroComplejo[] renglon = new NumeroComplejo[anchoImagen];
        for (int y = 0; y < altoImagen; y++) {
            for (int x = 0; x < anchoImagen; x++)
                renglon[x] = imagenTransformada[x][y];
            fft1D(renglon, inversa);
            for (int x = 0; x < anchoImagen; x++)
                imagenTransformada[x][y] = renglon[x];
        }

        // 2) FFT de cada columna (x fijo, y varía: longitud = alto)
        for (int x = 0; x < anchoImagen; x++) {
            fft1D(imagenTransformada[x], inversa); // imagenTransformada[x] ya es la columna x
        }

        // 3) escala de la inversa
        if (inversa) {
            double escala = 1.0 / ((double) anchoImagen * altoImagen);
            for (int x = 0; x < anchoImagen; x++)
                for (int y = 0; y < altoImagen; y++)
                    imagenTransformada[x][y] = imagenTransformada[x][y].producto(escala);
        }

        return imagenTransformada;
    }

    /**
     * FFT 1D en el mismo arreglo (in-place).
     * Paso A: reordenar por inversión de bits.
     * Paso B: log2(n) niveles de mariposas: a' = a + w*b ; b' = a - w*b
     */
    public void fft1D(NumeroComplejo[] datos, boolean inversa) {
        int n = datos.length;
        int niveles = calculaLog2(n);

        // ---- Paso A: inversión de bits ----
        // j es el contador "al revés": suma 1 en binario leyendo los bits
        // de izquierda a derecha, así recorre los índices con bits invertidos.
        int j = 0;
        for (int i = 0; i < n - 1; i++) {
            if (i < j) { // intercambiar solo una vez cada pareja
                NumeroComplejo temp = datos[i];
                datos[i] = datos[j];
                datos[j] = temp;
            }
            int puntoMedio = n / 2;
            while (puntoMedio <= j) { // acarreo hacia la derecha
                j -= puntoMedio;
                puntoMedio /= 2;
            }
            j += puntoMedio;
        }

        // ---- Paso B: mariposas ----
        // (coseno, seno) = giro base del nivel. En bloques de 2 el giro es
        // media vuelta: (-1, 0). Cada nivel siguiente usa la mitad del ángulo.
        double coseno = -1.0;
        double seno = 0.0;
        int l2 = 1;
        for (int nivel = 0; nivel < niveles; nivel++) {
            int l1 = l2;   // distancia entre a y b (mitad del bloque)
            l2 <<= 1;      // tamaño del bloque: 2, 4, 8, ...
            double u1 = 1.0; // (u1, u2) = w^jj, empieza en w^0 = 1
            double u2 = 0.0;
            for (int jj = 0; jj < l1; jj++) {
                for (int i = jj; i < n; i += l2) {
                    int i1 = i + l1;
                    // t = w^jj * b   (multiplicación compleja escrita a mano)
                    double t1 = u1 * datos[i1].getParteReal() - u2 * datos[i1].getParteImaginaria();
                    double t2 = u1 * datos[i1].getParteImaginaria() + u2 * datos[i1].getParteReal();
                    NumeroComplejo t = new NumeroComplejo(t1, t2);
                    datos[i1] = datos[i].restar(t); // b' = a - t
                    datos[i] = datos[i].sumar(t);   // a' = a + t
                }
                // girar un paso más: (u1 + i u2) *= (coseno + i seno)
                double z = u1 * coseno - u2 * seno;
                u2 = u1 * seno + u2 * coseno;
                u1 = z;
            }
            // fórmulas del ángulo mitad para el siguiente nivel
            seno = Math.sqrt((1.0 - coseno) / 2.0);
            if (!inversa)
                seno = -seno; // directa: e^(-i...), inversa: e^(+i...)
            coseno = Math.sqrt((1.0 + coseno) / 2.0);
        }
    }

    private int calculaLog2(int dim) {
        int conteo = 0;
        while ((dim >>= 1) > 0)
            conteo++;
        return conteo;
    }

    private void validarPotenciaDe2(int n, String nombre) {
        if (n < 1 || (n & (n - 1)) != 0)
            throw new IllegalArgumentException("El " + nombre + " (" + n
                    + ") debe ser potencia de 2. Redimensiona la imagen primero.");
    }
}
