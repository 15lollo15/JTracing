package image.palette;

import java.awt.*;
import java.util.Set;

public class ElbowKMeansExtractor implements PaletteExtractor{
    public static final double MAX_COLORS = 32;
    public static final double MIN_RATIO = .09;

    @Override
    public Set<Color> extract(Color[] pixels) {
        KMeansExtractor pe = new KMeansExtractor(2);
        Set<Color>  tmpPalette = pe.extract(pixels);
        Set<Color> oldPalette = tmpPalette;

        for(int i = 3; i <= MAX_COLORS; i++) {
            double prevInertia = pe.getInertia();
            double numColorsRatio = i / (i - 1d);
            pe = new KMeansExtractor(i);
            tmpPalette = pe.extract(pixels);
            double inertiaRatio = pe.getInertia() / prevInertia;
            if (pe.getInertia() == 0 || Math.abs(prevInertia - pe.getInertia()) < 0.05) {
                if (pe.getInertia() == 0) {
                    oldPalette = tmpPalette;
                }
                break;
            }
            oldPalette = tmpPalette;
        }
        return  oldPalette;
    }
}
