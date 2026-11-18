package claseAI;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

/**
 * Programa principal: abre una imagen, muestra su espectro, aplica un filtro
 * y muestra el resultado.
 *
 * @author Roberto Cruz Leija
 */
public class FFTPrueba {

    public static void main(String[] args) {
        BufferedImage imagen = ImageManager.openImage();
        if (imagen == null) {
            System.out.println("No se seleccionó ninguna imagen.");
            return;
        }

        // Gestor redimensiona a potencia de 2 y calcula la FFT directa
        Gestor complex = new Gestor(imagen);
        Dimension dim = new Dimension(complex.getWidth(), complex.getHeight());

        BufferedImage original = complex.getImagenTrabajo();
        BufferedImage espectro = complex.getFrequencyDomainImage(true, true);

        // creamos el filtro (prueba radios de 20 a 80 para notar el efecto)
        NumeroComplejo[][] f = Filtros.crearFiltroIdealPasaBajas(40, dim);
        // NumeroComplejo[][] f = Filtros.crearFiltroButterWorth(40, dim, 2);
        // NumeroComplejo[][] f = Filtros.crearFiltroIdealPasaAltas(20, dim);
        Image filtro = Filtros.complejoToImagen(f);

        complex.aplicarFiltro(f);
        BufferedImage resultado = complex.obtenerImagenDominioEspacial();

        // las ventanas de Swing se crean en el hilo de eventos
        SwingUtilities.invokeLater(() -> {
            new ImageJFrame(original, "original");
            new ImageJFrame(espectro, "espectro");
            new ImageJFrame(filtro, "filtro");
            new ImageJFrame(resultado, "resultado");
        });
    }
}
