package claseAI;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Ventana sencilla que muestra una imagen con un título.
 */
public class ImageJFrame extends JFrame {

    public ImageJFrame(Image imagen, String titulo) {
        super(titulo);
        add(new JLabel(new ImageIcon(imagen)));
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setVisible(true);
    }

    public ImageJFrame(Image imagen) {
        this(imagen, "Imagen");
    }
}
