package com.example;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class Imagenes {

    private byte[] imageToByteArray(Image image) throws IOException {
        // Convierte la imagen a BufferedImage para asegurarse de que sea de tipo
        // compatible
        BufferedImage bufferedImage = (BufferedImage) image;

        // Usar ByteArrayOutputStream y envolverlo en un ImageOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);

        // Escribir la imagen en el ImageOutputStream
        ImageIO.write(bufferedImage, "jpg", ios);

        // Cerrar el ImageOutputStream
        ios.close();

        // Devolver los bytes de la imagen
        return baos.toByteArray();
    }
}
