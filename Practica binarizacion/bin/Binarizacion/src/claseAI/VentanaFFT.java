package claseAI;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Interfaz para elegir una imagen y aplicarle un filtro ideal pasa bajas.
 *
 *   [Abrir imagen…]
 *   Original          | Espectro |F|
 *   Espectro x filtro | Resultado (FFT inversa)
 *   Radio de corte: ------o------ D₀ = 40
 *
 * Los cálculos pesados se hacen con SwingWorker para que la ventana no se
 * congele mientras la FFT trabaja.
 */
public class VentanaFFT extends JFrame {

    private final PanelImagen panelOriginal = new PanelImagen("Original");
    private final PanelImagen panelEspectro = new PanelImagen("Espectro |F| (log)");
    private final PanelImagen panelFiltrado = new PanelImagen("Espectro × filtro");
    private final PanelImagen panelResultado = new PanelImagen("Resultado (FFT inversa)");

    private final JButton botonAbrir = new JButton("Abrir imagen…");
    private final JSlider sliderRadio = new JSlider(1, 256, 40);
    private final JLabel etiquetaRadio = new JLabel("D₀ = 40");

    private Gestor gestor;              // datos de la imagen actual
    private boolean trabajando = false; // hay una FFT en curso
    private boolean pendiente = false;  // el usuario movió el radio mientras se calculaba

    public VentanaFFT() {
        super("Filtro ideal pasa bajas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // arriba: solo el botón para abrir
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        botonAbrir.addActionListener(e -> elegirImagen());
        arriba.add(botonAbrir);
        add(arriba, BorderLayout.NORTH);

        // centro: las cuatro imágenes en una cuadrícula de 2x2
        JPanel imagenes = new JPanel(new GridLayout(2, 2, 4, 4));
        imagenes.add(panelOriginal);
        imagenes.add(panelEspectro);
        imagenes.add(panelFiltrado);
        imagenes.add(panelResultado);
        add(imagenes, BorderLayout.CENTER);

        // abajo: el radio de corte
        add(crearPanelRadio(), BorderLayout.SOUTH);

        sliderRadio.setEnabled(false); // hasta que haya imagen
        setMinimumSize(new Dimension(600, 650));
        setSize(900, 950);
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelRadio() {
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        abajo.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        sliderRadio.setPreferredSize(new Dimension(500, sliderRadio.getPreferredSize().height));
        etiquetaRadio.setPreferredSize(new Dimension(70, etiquetaRadio.getPreferredSize().height));

        // al soltar el deslizador se vuelve a calcular el resultado
        sliderRadio.addChangeListener(e -> {
            etiquetaRadio.setText("D₀ = " + sliderRadio.getValue());
            if (!sliderRadio.getValueIsAdjusting())
                aplicarFiltro();
        });

        abajo.add(new JLabel("Radio de corte:"));
        abajo.add(sliderRadio);
        abajo.add(etiquetaRadio);
        return abajo;
    }

    // ------------------------------------------------------------------
    // Abrir imagen: lee el archivo y calcula la FFT directa en segundo plano
    // ------------------------------------------------------------------
    private void elegirImagen() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "bmp", "gif"));
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            cargarImagen(selector.getSelectedFile());
    }

    public void cargarImagen(File archivo) {
        BufferedImage imagen = ImageManager.openImage(archivo);
        if (imagen == null) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la imagen:\n" + archivo,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        empezarCalculo();
        new SwingWorker<Gestor, Void>() {
            @Override
            protected Gestor doInBackground() {
                return new Gestor(imagen); // redimensiona + FFT de los 3 canales
            }

            @Override
            protected void done() {
                terminarCalculo();
                try {
                    gestor = get();
                } catch (Exception ex) {
                    mostrarError(ex);
                    return;
                }
                BufferedImage trabajo = gestor.getImagenTrabajo();
                panelOriginal.setImagen(trabajo);
                panelOriginal.setTitulo("Original (" + trabajo.getWidth() + "×" + trabajo.getHeight() + ")");
                panelEspectro.setImagen(gestor.getFrequencyDomainImage(true, true));
                sliderRadio.setMaximum(Math.max(trabajo.getWidth(), trabajo.getHeight()) / 2);
                sliderRadio.setEnabled(true);
                aplicarFiltro();
            }
        }.execute();
    }

    // ------------------------------------------------------------------
    // Aplicar filtro: F·H y FFT inversa en segundo plano
    // ------------------------------------------------------------------
    private void aplicarFiltro() {
        if (gestor == null)
            return;
        if (trabajando) {      // ya hay un cálculo: repetir al terminar
            pendiente = true;
            return;
        }
        pendiente = false;
        int radio = sliderRadio.getValue();

        empezarCalculo();
        new SwingWorker<BufferedImage[], Void>() {
            @Override
            protected BufferedImage[] doInBackground() {
                gestor.restaurarFrecuencias(); // empezar siempre de la FFT sin filtrar
                Dimension dim = new Dimension(gestor.getWidth(), gestor.getHeight());
                gestor.aplicarFiltro(Filtros.crearFiltroIdealPasaBajas(radio, dim));
                BufferedImage espectroFiltrado = gestor.getFrequencyDomainImage(true, true);
                BufferedImage resultado = gestor.obtenerImagenDominioEspacial();
                return new BufferedImage[]{espectroFiltrado, resultado};
            }

            @Override
            protected void done() {
                terminarCalculo();
                try {
                    BufferedImage[] r = get();
                    panelFiltrado.setImagen(r[0]);
                    panelResultado.setImagen(r[1]);
                } catch (Exception ex) {
                    mostrarError(ex);
                }
                if (pendiente)
                    aplicarFiltro();
            }
        }.execute();
    }

    /** Mientras calcula: cursor de espera y sin poder cambiar de imagen. */
    private void empezarCalculo() {
        trabajando = true;
        botonAbrir.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void terminarCalculo() {
        trabajando = false;
        botonAbrir.setEnabled(true);
        setCursor(Cursor.getDefaultCursor());
    }

    private void mostrarError(Exception ex) {
        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
        JOptionPane.showMessageDialog(this, causa.toString(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // si falla se usa el aspecto por defecto de Java
            }
            VentanaFFT ventana = new VentanaFFT();
            ventana.setVisible(true);
            if (args.length > 0) // opcional: java claseAI.VentanaFFT imagen.jpg
                ventana.cargarImagen(new File(args[0]));
        });
    }
}
