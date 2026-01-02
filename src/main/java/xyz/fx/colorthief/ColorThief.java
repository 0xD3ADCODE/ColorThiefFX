/*
 * Java Color Thief
 * by Sven Woltmann, Fonpit AG
 *
 * JavaFX Port by 0xD3ADCODE
 *
 * https://www.androidpit.com
 * https://www.androidpit.de
 *
 * License
 * -------
 * Creative Commons Attribution 2.5 License:
 * http://creativecommons.org/licenses/by/2.5/
 *
 * Thanks
 * ------
 * Lokesh Dhakar - for the original Color Thief JavaScript version
 * available at http://lokeshdhakar.com/projects/color-thief/
 */

package xyz.fx.colorthief;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ColorThief {
    private static final int DEFAULT_QUALITY = 10;
    private static final boolean DEFAULT_IGNORE_WHITE = true;

    /**
     * Use the median cut algorithm to cluster similar colors and return the base color from the largest cluster
     *
     * @param sourceImage source {@link Image}
     *
     * @return dominant color as {@link Color}
     */
    public static Color getDominantColor(Image sourceImage) {
        return getDominantColor(sourceImage, DEFAULT_QUALITY, DEFAULT_IGNORE_WHITE);
    }

    /**
     * Use the median cut algorithm to cluster similar colors and return the base color from the largest cluster
     *
     * @param sourceImage source {@link Image}
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster a color will be returned but
     *            the greater the likelihood that it will not be the visually most dominant color.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     *
     * @return dominant color as {@link Color}
     * @throws IllegalArgumentException if quality is < 1
     */
    public static Color getDominantColor(Image sourceImage, int quality, boolean ignoreWhite) {
        return Optional.ofNullable(getPalette(sourceImage, 5, quality, ignoreWhite))
                .filter(paletteList -> !paletteList.isEmpty())
                .map(palette -> palette.get(0))
                .orElse(null);
    }

    /**
     * Use the median cut algorithm to cluster similar colors from the image border and return the base color from the largest cluster
     *
     * @param sourceImage source {@link Image}
     * @param borderSize size of the border in pixels to be analyzed
     *
     * @return dominant border color as {@link Color}
     */
    public static Color getDominantBorderColor(Image sourceImage, int borderSize) {
        return getDominantBorderColor(sourceImage, borderSize, DEFAULT_QUALITY, DEFAULT_IGNORE_WHITE);
    }

    /**
     * Use the median cut algorithm to cluster similar colors from the image border and return the base color from the largest cluster
     *
     * @param sourceImage source {@link Image}
     * @param borderSize size of the border in pixels to be analyzed
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster a color will be returned but
     *            the greater the likelihood that it will not be the visually most dominant color.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     *
     * @return dominant border color as {@link Color}
     * @throws IllegalArgumentException if quality is < 1
     */
    public static Color getDominantBorderColor(Image sourceImage, int borderSize, int quality, boolean ignoreWhite) {
        int[][] borderPixels = getBorderPixels(sourceImage, borderSize, quality, ignoreWhite);

        if (borderPixels == null || borderPixels.length == 0) {
            return null;
        }

        ColorMap cmap = MMCQ.quantize(borderPixels, 5);

        return Optional.ofNullable(cmap)
                .map(ColorMap::palette)
                .filter(palette -> palette.length > 0)
                .map(palette -> {
                    int[] dom = palette[0];
                    return Color.rgb(dom[0], dom[1], dom[2]);
                })
                .orElse(null);
    }

    /**
     * Use the median cut algorithm to cluster similar colors
     *
     * @param sourceImage source {@link Image}
     * @param colorCount size of the palette; number of colors returned
     *
     * @return palette {@link List} of {@link Color}
     */
    public static List<Color> getPalette(Image sourceImage, int colorCount) {
        return getPalette(sourceImage, colorCount, DEFAULT_QUALITY, DEFAULT_IGNORE_WHITE);
    }

    /**
     * Use the median cut algorithm to cluster similar colors
     *
     * @param sourceImage source {@link Image}
     * @param colorCount size of the palette; number of colors returned
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite if <code>true</code>, white pixels are ignored
     *
     * @return palette {@link List} of {@link Color}
     * @throws IllegalArgumentException if quality is < 1
     */
    public static List<Color> getPalette(Image sourceImage, int colorCount, int quality, boolean ignoreWhite) {
        return Optional.ofNullable(getColorMap(sourceImage, colorCount, quality, ignoreWhite))
                .map(ColorMap::palette)
                .map(palette ->
                        Arrays.stream(palette)
                                .map(dominantColor -> Color.rgb(dominantColor[0], dominantColor[1], dominantColor[2]))
                                .toList()
                )
                .orElse(null);
    }

    /**
     * Use the median cut algorithm to cluster similar colors
     *
     * @param sourceImage source {@link Image}
     * @param colorCount size of the palette; number of colors returned (minimum 2, maximum 256)
     *
     * @return {@link ColorMap}
     */
    public static ColorMap getColorMap(Image sourceImage, int colorCount) {
        return getColorMap(sourceImage, colorCount, DEFAULT_QUALITY, DEFAULT_IGNORE_WHITE);
    }

    /**
     * Use the median cut algorithm to cluster similar colors
     *
     * @param sourceImage source {@link Image}
     * @param colorCount size of the palette; number of colors returned (minimum 2, maximum 256)
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite if <code>true</code>, white pixels are ignored
     *
     * @return {@link ColorMap}
     * @throws IllegalArgumentException if quality is < 1
     */
    public static ColorMap getColorMap(Image sourceImage, int colorCount, int quality, boolean ignoreWhite) {
        if (colorCount < 2 || colorCount > 256) {
            throw new IllegalArgumentException("Specified colorCount must be between 2 and 256.");
        }
        if (quality < 1) {
            throw new IllegalArgumentException("Specified quality should be greater then 0.");
        }

        // Send array to quantize function which clusters values using median cut algorithm
        return MMCQ.quantize(getPixels(sourceImage, quality, ignoreWhite), colorCount);
    }

    /**
     * Gets the image's pixels {@link Color} via {@link PixelReader#getColor(int, int)} and separates it into RGB array
     *
     * @param sourceImage source {@link Image}
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite if <code>true</code>, white pixels are ignored
     *
     * @return an array of pixels (each as RGB int array)
     */
    private static int[][] getPixels(Image sourceImage, int quality, boolean ignoreWhite) {
        int width = (int) sourceImage.getWidth();
        int height = (int) sourceImage.getHeight();

        int pixelCount = width * height;
        PixelReader pixelReader = sourceImage.getPixelReader();

        // numRegardedPixels must be rounded up to avoid an ArrayIndexOutOfBoundsException if all
        // pixels are good.
        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int[][] res = new int[numRegardedPixels][];
        int r, g, b;

        int numUsedPixels = 0;
        for (int i = 0; i < pixelCount; i += quality) {
            int x = i % width;
            int y = i / width;

            Color pixelColor = pixelReader.getColor(x, y);
            r = (int) (255 * pixelColor.getRed());
            g = (int) (255 * pixelColor.getGreen());
            b = (int) (255 * pixelColor.getBlue());

            if (!(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                res[numUsedPixels] = new int[] {r, g, b};
                numUsedPixels++;
            }
        }

        return Arrays.copyOfRange(res, 0, numUsedPixels);
    }

    /**
     * Gets the image's pixels {@link Color} from the specified border area via {@link PixelReader#getColor(int, int)} and separates it into RGB array
     *
     * @param sourceImage source {@link Image}
     * @param borderSize size of the border in pixels to be analyzed
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite if <code>true</code>, white pixels are ignored
     *
     * @return an array of pixels (each as RGB int array)
     */
    private static int[][] getBorderPixels(Image sourceImage, int borderSize, int quality, boolean ignoreWhite) {
        int width = (int) sourceImage.getWidth();
        int height = (int) sourceImage.getHeight();

        int pixelCount = width * height;
        PixelReader pixelReader = sourceImage.getPixelReader();

        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int[][] res = new int[numRegardedPixels][];
        int r, g, b;

        int numUsedPixels = 0;

        for (int i = 0; i < pixelCount; i += quality) {
            int x = i % width;
            int y = i / width;

            boolean isBorder = x < borderSize || x >= width - borderSize || y < borderSize || y >= height - borderSize;

            if (isBorder) {
                Color pixelColor = pixelReader.getColor(x, y);

                if (pixelColor.getOpacity() < 0.5) {
                    continue;
                }

                r = (int) (255 * pixelColor.getRed());
                g = (int) (255 * pixelColor.getGreen());
                b = (int) (255 * pixelColor.getBlue());

                if (!(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                    res[numUsedPixels] = new int[]{r, g, b};
                    numUsedPixels++;
                }
            }
        }

        return Arrays.copyOfRange(res, 0, numUsedPixels);
    }
}
