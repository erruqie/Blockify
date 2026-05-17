package one.clownless.blockify.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class URLImage
{
    private static int counter = 0;
    private final NativeImage urlImage;
    private final DynamicTexture urlTexture;
    private final Identifier urlID;
    private final int width;
    private final int height;
    public static final Logger LOGGER = LogManager.getLogger("Blockify");

    public URLImage(int width, int height)
    {
        this.width = width;
        this.height = height;
        Minecraft client = Minecraft.getInstance();
        this.urlTexture = new DynamicTexture("blockify-album-" + (counter++), width, height, false);
        this.urlImage = this.urlTexture.getPixels();
        this.urlID = Identifier.fromNamespaceAndPath("blockify", "album/cover_" + counter);
        client.getTextureManager().register(this.urlID, this.urlTexture);
    }

    public void setImage(String url)
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection) URI.create(url).toURL().openConnection();
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
                            urlImage.setPixel(x, y, img.getRGB(x, y));
                        }
                        else
                        {
                            urlImage.setPixel(x, y, new Color(0, 0, 0, 0).getRGB());
                        }
                    }
                }
            }
            urlTexture.upload();
            in.close();
            con.disconnect();
            img.flush();
        } catch (IOException e)
        {
            LOGGER.error(e.getMessage());
        }
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
