/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package openCV.FFT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author Francisco Tapia
 */
public class Main {
    public static void main(String[] args) {
        File archivo = elegirImagen();
        if (archivo == null){
            System.out.println("No se eligió una imagen");
            return;
        }else{
            try {
                byte[] bytes = Files.readAllBytes(archivo.toPath());
                Mat img = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_GRAYSCALE);
                if (img.empty()){
                    System.out.println("No se pudo leer la imagen");
                }
                Mat f = fft(img);
            } catch (IOException ex) {
                System.getLogger(Main.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            
        }        
    }
    
    
            
    public static File elegirImagen() {
    JFileChooser imagen = new JFileChooser();
    imagen.setDialogTitle("Seleccionar imagen");
    imagen.setFileFilter(new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "jpeg", "png"));
    imagen.setAcceptAllFileFilterUsed(false);

    int resultado = imagen.showOpenDialog(null);
    if (resultado == JFileChooser.APPROVE_OPTION) {
        return imagen.getSelectedFile();
    }
    return null;   // el usuario canceló o cerró el diálogo
}
}


