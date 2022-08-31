package potrace;

import geometry.Curve;
import geometry.Path;
import image.Bitmap;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetSVG {
    public static final String RGB_STRING_FORMAT = "rgb(%d,%d,%d)";

    private GetSVG() {}

    private static String format(double d) {
        return BigDecimal.valueOf(d).setScale(3, RoundingMode.HALF_UP).toString();
    }

    private static String  bezier(Curve curve, int i, int size) {
        var b = "C " + format(curve.c[i * 3].x * size) + " " +
                format(curve.c[i * 3].y * size) + ",";
        b += format(curve.c[i * 3 + 1].x * size) + ' ' +
                format(curve.c[i * 3 + 1].y * size)+ ',';
        b += format(curve.c[i * 3 + 2].x * size)+ ' ' +
                format(curve.c[i * 3 + 2].y * size) + ' ';
        return b;
    }

    private static String  segment(Curve curve, int i, int size) {
        var s = "L " + format(curve.c[i * 3 + 1].x * size) + ' ' +
                format(curve.c[i * 3 + 1].y * size) + ' ';
        s += format(curve.c[i * 3 + 2].x * size) + ' ' +
                format(curve.c[i * 3 + 2].y * size) + ' ';
        return s;
    }

    private static String path(Curve curve, int size) {
        StringBuilder sb = new StringBuilder();
        int n = curve.n;
        sb.append("M").append(format(curve.c[(n - 1) * 3 + 2].x * size));
        sb.append(" ").append(format(curve.c[(n - 1) * 3 + 2].y * size)).append(" ");
        for (int i = 0; i < n; i++) {
            if (curve.tag[i].equals("CURVE")) {
                sb.append(bezier(curve, i, size));
            } else if (curve.tag[i].equals("CORNER")) {
                sb.append(segment(curve, i, size));
            }
        }
        return sb.toString();
    }

    public static String getSVG(int width, int height, int size, List<Path> pathlist,  String optType) {
        return getSVG(width, height, size, pathlist, optType, Color.BLACK);
    }

    public static String getSVG(int width, int height, int size, List<Path> pathlist, String optType, Color color) {
        int w = width * size;
        int h = height * size;
        String strokec;
        String fillc;
        String fillrule;
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        StringBuilder sb = new StringBuilder();
        sb.append("<svg id=\"svg\" version=\"1.1\" width=\"").append(w).append("\" height=\"").append(h);
        sb.append("\" xmlns=\"http://www.w3.org/2000/svg\">");
        sb.append("<path d=\"");
        for (Path path : pathlist) {
            Curve c = path.curve;
            sb.append(path(c, size));
        }
        if (optType.equals("curve")) {
            strokec = String.format(RGB_STRING_FORMAT, r, g, b);
            fillc = "none";
            fillrule = "";
        } else {
            strokec = "none";
            fillc = String.format(RGB_STRING_FORMAT, r, g, b);
            fillrule = " fill-rule=\"evenodd\"";
        }
        sb.append("\" stroke=\"").append(strokec);
        sb.append("\" fill=\"").append(fillc).append("\"").append(fillrule).append("/></svg>");
        return sb.toString();
    }

    public static String getSVG(int width, int height, int size, String optType, Map<Color, List<Path>> coloredPaths, Color bgColor) {
        int w = width * size;
        int h = height * size;

        String svg = "<svg id=\"svg\" version=\"1.1\" width=\"" + w + "\" height=\"" + h +
                "\" xmlns=\"http://www.w3.org/2000/svg\">";

        int r = bgColor.getRed();
        int g = bgColor.getGreen();
        int b = bgColor.getBlue();
        svg += "<rect width=\""+w+"\" height=\""+h+"\" style=\"fill:"+String.format(RGB_STRING_FORMAT, r, g, b)+";stroke-width:3;stroke:rgb(0,0,0)\" />";

        for (Map.Entry<Color, List<Path>> entry : coloredPaths.entrySet()) {
            Color color = entry.getKey();
            List<Path> pathlist = entry.getValue();

            svg += getPath(pathlist, size, optType, color);
        }

        svg += "</svg>";
        return svg;
    }

    private static String getPath(List<Path> pathlist, int size, String optType, Color color) {
        StringBuilder sb = new StringBuilder();
        sb.append("<path d=\"");
        String strokec;
        String fillc;
        String fillrule;
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        for (Path path : pathlist) {
            Curve c = path.curve;
            sb.append(path(c, size));
        }
        if (optType.equals("curve")) {
            strokec = String.format(RGB_STRING_FORMAT, r, g, b);
            fillc = "none";
            fillrule = "";
        } else {
            strokec = "none";
            fillc = String.format(RGB_STRING_FORMAT, r, g, b);
            fillrule = " fill-rule=\"evenodd\"";
        }
        sb.append("\" stroke=\"").append(strokec);
        sb.append("\" fill=\"").append(fillc).append("\"").append(fillrule).append("/>");

        return sb.toString();
    }
}