package geometry;

import potrace.Sum;

import java.util.ArrayList;
import java.util.List;

public class Path {
    public double area = 0;
    public int len = 0;
    public List<IntegerPoint> points = new ArrayList<>();
    public IntegerPoint minPoint;
    public IntegerPoint maxPoint;
    public Sign sign;
    public IntegerPoint firstPoint;
    public List<Sum> sums;
    public int[] longestStraightLine;
    public int optimalPolygonLenght;
    public int[] optimalPolygon;
    public Curve curve;

    @Override
    public String toString() {
        return points.toString();
    }

    public enum Sign {PLUS, MINUS}
}
