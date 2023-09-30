package me.theentropyshard.synergybot.utils;


import me.theentropyshard.synergybot.functions.ImagesOperation;

import java.awt.image.BufferedImage;

public class RgbMaster {
    private BufferedImage image;
    private int width;
    private int height;
    private boolean hasAlphaChannel;
    private int[] pixels;

    public RgbMaster(BufferedImage image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.hasAlphaChannel = image.getAlphaRaster() != null;
        this.pixels = image.getRGB(0, 0, this.width, this.height, null, 0, this.width);
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public void changeImage(ImagesOperation operation) throws Exception {
        for (int i = 0; i < this.pixels.length; i++) {
            float[] pixel = ImageUtils.rgbIntToArray(this.pixels[i]);
            float[] newPixel = operation.execute(pixel);
            this.pixels[i] = ImageUtils.arrayToRgbInt(newPixel);
        }
        this.image.setRGB(0, 0, this.width, this.height, this.pixels, 0, this.width);
    }
}
