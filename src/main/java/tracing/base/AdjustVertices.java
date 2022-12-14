package tracing.base;

import geometry.Curve;
import geometry.DoublePoint;
import geometry.Path;
import geometry.IntegerPoint;
import utils.MathUtils;

import java.util.List;

public class AdjustVertices {
    private Path path;

    public AdjustVertices(Path path) {
        this.path = path;
    }

    private void pointslope(Path path, int i, int j, DoublePoint ctr, DoublePoint dir) {
        List<Sum> sums = path.getSums();

        int n = path.getLen();
        int r = 0;
        double l;

        while (j>=n) {
            j-=n;
            r+=1;
        }
        while (i>=n) {
            i-=n;
            r-=1;
        }
        while (j<0) {
            j+=n;
            r-=1;
        }
        while (i<0) {
            i+=n;
            r+=1;
        }

        double x = sums.get(j+1).x() - sums.get(i).x() + r*sums.get(n).x();
        double y = sums.get(j+1).y() - sums.get(i).y() + r*sums.get(n).y();
        double x2 = sums.get(j+1).x2() -sums.get(i).x2() + r*sums.get(n).x2();
        double xy = sums.get(j+1).xy() - sums.get(i).xy() + r*sums.get(n).xy();
        double y2 = sums.get(j+1).y2() - sums.get(i).y2() + r*sums.get(n).y2();
        double k = j+1.0-i+r*n;

        ctr.setX(x/k);
        ctr.setY(y/k);

        double a = (x2-x*x/k)/k;
        double b = (xy-x*y/k)/k;
        double c = (y2-y*y/k)/k;

        double lambda2 = (a+c+Math.sqrt((a-c)*(a-c)+4*b*b))/2;

        a -= lambda2;
        c -= lambda2;

        if (Math.abs(a) >= Math.abs(c)) {
            l = Math.sqrt(a*a+b*b);
            if (l!=0) {
                dir.setX(-b/l);
                dir.setY(a/l);
            }
        } else {
            l = Math.sqrt(c*c+b*b);
            if (l!=0) {
                dir.setX(-c/l);
                dir.setY(b/l);
            }
        }
        if (l==0) {
            dir.setX(0);
            dir.setY(0);
        }
    }

    private void generateOptimalSlopePoint(int[] po, int n, int m, DoublePoint[] ctr, DoublePoint[] dir) {
        for (int i=0; i<m; i++) {
            int j = po[MathUtils.mod(i+1,m)];
            j = MathUtils.mod(j-po[i],n)+po[i];
            ctr[i] = new DoublePoint();
            dir[i] = new DoublePoint();
            pointslope(path, po[i], j, ctr[i], dir[i]);
        }
    }

    private Quad[] generateQuads(int m, DoublePoint[] dir, double[] v, DoublePoint[] ctr) {
        Quad[] q = new Quad[m];
        for (int i=0; i<m; i++) {
            q[i] = new Quad();
            double d = dir[i].getX() * dir[i].getX() + dir[i].getY() * dir[i].getY();
            if (d == 0.0) {
                for (int j=0; j<3; j++) {
                    for (int k=0; k<3; k++) {
                        q[i].getData()[j * 3 + k] = 0;
                    }
                }
            } else {
                v[0] = dir[i].getY();
                v[1] = -dir[i].getX();
                v[2] = - v[1] * ctr[i].getY() - v[0] * ctr[i].getX();
                for (int l=0; l<3; l++) {
                    for (int k=0; k<3; k++) {
                        q[i].getData()[l * 3 + k] = v[l] * v[k] / d;
                    }
                }
            }
        }
        return q;
    }

    private void addQuadraticForm(int j, int i, Quad[] q, Quad quad) {
        for (int l=0; l<3; l++) {
            for (int k=0; k<3; k++) {
                quad.getData()[l * 3 + k] = q[j].at(l, k) + q[i].at(l, k);
            }
        }
    }

    public void adjustVertices() {
        int m = path.getOptimalPolygon().length;
        DoublePoint[] ctr = new DoublePoint[m];
        DoublePoint[] dir = new DoublePoint[m];
        double[] v = new double[3];
        DoublePoint s = new DoublePoint();
        List<IntegerPoint> pt = path.getPoints();
        int x0 = path.getFirstPoint().getX();
        int y0 = path.getFirstPoint().getY();
        int n = path.getLen();
        int[] po = path.getOptimalPolygon();

        Curve curve = new Curve(m);

        generateOptimalSlopePoint(po, n, m, ctr, dir);

        Quad[] q = generateQuads(m, dir, v, ctr);

        for (int i=0; i<m; i++) {
            Quad quad = new Quad();
            DoublePoint w = new DoublePoint();

            s.setX(pt.get(po[i]).getX()-(double)x0);
            s.setY(pt.get(po[i]).getY()-(double)y0);

            int j = MathUtils.mod(i-1,m);

            addQuadraticForm(j, i, q, quad);

            while(true) {

                double det = quad.at(0, 0)*quad.at(1, 1) - quad.at(0, 1)*quad.at(1, 0);
                if (det != 0.0) {
                    w.setX((-quad.at(0, 2)*quad.at(1, 1) + quad.at(1, 2)*quad.at(0, 1)) / det);
                    w.setY(( quad.at(0, 2)*quad.at(1, 0) - quad.at(1, 2)*quad.at(0, 0)) / det);
                    break;
                }

                if (quad.at(0, 0)>quad.at(1, 1)) {
                    v[0] = -quad.at(0, 1);
                    v[1] = quad.at(0, 0);
                } else if (quad.at(1, 1) != 0) {
                    v[0] = -quad.at(1, 1);
                    v[1] = quad.at(1, 0);
                } else {
                    v[0] = 1;
                    v[1] = 0;
                }
                double d = v[0] * v[0] + v[1] * v[1];
                v[2] = - v[1] * s.getY() - v[0] * s.getX();
                for (int l=0; l<3; l++) {
                    for (int k=0; k<3; k++) {
                        quad.getData()[l * 3 + k] += v[l] * v[k] / d;
                    }
                }
            }
            double dx = Math.abs(w.getX()-s.getX());
            double dy = Math.abs(w.getY()-s.getY());
            if (dx <= 0.5 && dy <= 0.5) {
                curve.getVertex()[i] = new DoublePoint(w.getX()+x0, w.getY()+y0);
                continue;
            }

            double min = MathUtils.quadform(quad, s);
            double xmin = s.getX();
            double ymin = s.getY();

            if (quad.at(0, 0) != 0.0) {
                for (int z=0; z<2; z++) {
                    w.setY(s.getY()-0.5+z);
                    w.setX(- (quad.at(0, 1) * w.getY() + quad.at(0, 2)) / quad.at(0, 0));
                    dx = Math.abs(w.getX()-s.getX());
                    double cand = MathUtils.quadform(quad, w);
                    if (dx <= 0.5 && cand < min) {
                        min = cand;
                        xmin = w.getX();
                        ymin = w.getY();
                    }
                }
            }

            if (quad.at(1, 1) != 0.0) {
                for (int z=0; z<2; z++) {
                    w.setX(s.getX()-0.5+z);
                    w.setY(- (quad.at(1, 0) * w.getX() + quad.at(1, 2)) / quad.at(1, 1));
                    dy = Math.abs(w.getY()-s.getY());
                    double cand = MathUtils.quadform(quad, w);
                    if (dy <= 0.5 && cand < min) {
                        min = cand;
                        xmin = w.getX();
                        ymin = w.getY();
                    }
                }
            }

            for (int l=0; l<2; l++) {
                for (int k=0; k<2; k++) {
                    w.setX(s.getX()-0.5+l);
                    w.setY(s.getY()-0.5+k);
                    double cand = MathUtils.quadform(quad, w);
                    if (cand < min) {
                        min = cand;
                        xmin = w.getX();
                        ymin = w.getY();
                    }
                }
            }

            curve.getVertex()[i] = new DoublePoint(xmin + x0, ymin + y0);
        }
        path.setCurve(curve);
    }

}
