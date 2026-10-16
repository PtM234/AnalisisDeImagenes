/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package binarizacion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Francisco Tapia
 */
public class Binarizacion {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        JFileChooser seleccionador = new JFileChooser();
        seleccionador.setDialogTitle("Elige una imagen");
        
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "jpeg", "png");
        seleccionador.setFileFilter(filtro);
        
        int respuesta = seleccionador.showOpenDialog(null);
        
        if (respuesta == JFileChooser.APPROVE_OPTION){
            File archivoImagen = seleccionador.getSelectedFile();
            String rutaArchivo = archivoImagen.getAbsolutePath();
            try{
                BufferedImage imagen = ImageIO.read(archivoImagen);
                if (imagen != null){
                    System.out.println("Imagen cargada con éxito");
                    System.out.println("Altura de archivo: " + imagen.getHeight() + " Ancho de archivo: " + imagen.getWidth());
                    
                    int alto = imagen.getHeight();
                    int ancho = imagen.getWidth();
                    
                    int [][] pixelesRGB = new int[ancho*alto][3];
                    int indice = 0;
                    
                    for (int y = 0; y < imagen.getHeight(); y++){
                        for (int x = 0; x < imagen.getWidth(); x++){
                            int valorBit = imagen.getRGB(x, y);
                            
                            int rojo = (valorBit >> 16) & 0xff;
                            int verde = (valorBit >> 8) & 0xff;
                            int azul = valorBit & 0xff;
                            
                            pixelesRGB [indice][0] = rojo;
                            pixelesRGB [indice][1] = verde;
                            pixelesRGB [indice][2] = azul;
                            
                            indice++;
                        }
                    }
                    
                    //K-means
                    
                    int k = 3; //Clusters
                    int maxIteraciones = 10;
                    int [][] centroides = new int [k][3];
                    int [] etiquetas = new int [pixelesRGB.length];
                } else{
                    System.out.println("Imagen no valida");
                } 
            } catch(IOException e){
                    System.out.println(e);
            }
        }
    }
    
}
