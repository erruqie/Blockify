package one.clownless.blockify.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class URLImage
{
    private static final int EXCEPT_R_MASK = 0xFF00FFFF;
    private static final int ONLY_R_MASK = ~EXCEPT_R_MASK;
    private static final int EXCEPT_B_MASK = 0xFFFFFF00;
    private static final int ONLY_B_MASK = ~EXCEPT_B_MASK;
    private NativeImage urlImage;
    private NativeImageBackedTexture urlTexture;
    private Identifier urlID;
    private int width;
    private int height;
    private static MinecraftClient client;

    public URLImage(int width, int height)
    {
        this.width = width;
        this.height = height;
        client = MinecraftClient.getInstance();
        urlImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        urlTexture = new NativeImageBackedTexture(urlImage);
        urlID = client.getTextureManager().registerDynamicTexture("urlimage", urlTexture);
    }

    public void setImage(String url)
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            con.connect();
            InputStream in = con.getInputStream();
            BufferedImage img = ImageIO.read(in);
            for (int x = 0; x < this.width; x++)
            {
                if (x < img.getWidth())
                {
                    for (int y = 0; y < this.height; y++)
                    {
                        if (y < img.getHeight())
                        {
                            //urlImage.setPixelColor(x, y, getABRGfromARGB(img.getRGB(x, y)));
                            urlImage.setColor(x, y, getABRGfromARGB(img.getRGB(x, y)));
                        }
                        else
                        {
                            urlImage.setColor(x, y, new Color(0, 0, 0, 0).getRGB());
                        }
                    }
                }
            }
            urlTexture.upload();
            in.close();
            con.disconnect();
            img.flush();
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private int getABRGfromARGB(final int argbColor)
    {
        int r = (argbColor & ONLY_R_MASK) >> 16;
        int b = argbColor & ONLY_B_MASK;
        return
                (argbColor & EXCEPT_R_MASK & EXCEPT_B_MASK) | (b << 16) | r;
    }

    public Identifier getIdentifier()
    {
        return this.urlID;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }
}
