package claseAI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel con título que dibuja una imagen escalada al tamaño disponible,
 * conservando su proporción.
 */
public class PanelImagen extends JPanel {

    private Image imagen;
    private final JLabel titulo;
    private final Lienzo lienzo = new Lienzo();

    public PanelImagen(String textoTitulo) {
        super(new BorderLayout(0, 4));
        titulo = new JLabel(textoTitulo, SwingConstants.CENTER);
        add(titulo, BorderLayout.NORTH);
        add(lienzo, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    }

    public void setImagen(Image imagen) {
        this.imagen = imagen;
        lienzo.repaint();
    }

    public void setTitulo(String texto) {
        titulo.setText(texto);
    }

    private class Lienzo extends JPanel {

        Lienzo() {
            setPreferredSize(new Dimension(300, 300));
            setBackground(new Color(40, 40, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagen == null) {
                g.setColor(Color.GRAY);
                String txt = "sin imagen";
                g.drawString(txt, (getWidth() - g.getFontMetrics().stringWidth(txt)) / 2, getHeight() / 2);
                return;
            }
            int iw = imagen.getWidth(null), ih = imagen.getHeight(null);
            double escala = Math.min((double) getWidth() / iw, (double) getHeight() / ih);
            int w = (int) (iw * escala), h = (int) (ih * escala);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(imagen, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        }
    }
}
