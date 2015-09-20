package org.house.sprinklers.math;

/* PolygonIntersect.java
 *
 */

import java.awt.geom.Point2D;

/**
 * Area of Intersection of Polygons
 *
 * Algorithm based on http://cap-lore.com/MathPhys/IP/
 *
 * Adapted 9-May-2006 by Lagado
 */
public class PolygonIntersect {
    /**
     * return the area of intersection of two polygons
     *
     * Note: the area result has little more accuracy than a float
     *  This is true even if the polygon is specified with doubles.
     */
    public static double intersectionArea(Point2D[] a, Point2D[] b)
    {
        PolygonIntersect polygonIntersect = new PolygonIntersect();
        return polygonIntersect.inter(a, b);
    }

    public static double intersectionArea(Polygon a, Polygon b) {
        Point2D[] toPointsA = a.getPolygonPoints().toArray(new Point2D[a.getPolygonPoints().size()]);
        Point2D[] toPointsB = b.getPolygonPoints().toArray(new Point2D[b.getPolygonPoints().size()]);
        if (toPointsA.length == 0 || toPointsB.length == 0) {
            return 0;
        }
        return intersectionArea(toPointsA, toPointsB);
    }

    //--------------------------------------------------------------------------

    static class Point {
        double x; double y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }
    static class Box {
        Point min; Point max;
        Box(Point min, Point max) { this.min = min; this.max = max; }
    }
    static class Rng {
        int mn; int mx;
        Rng(int mn, int mx) { this.mn = mn; this.mx = mx; }
    }
    static class IPoint { int x; int y; }
    static class Vertex { IPoint ip; Rng rx; Rng ry; int in; }

    static final double gamut = 500000000.;
    static final double mid = gamut / 2.;

    //--------------------------------------------------------------------------

    private static void range(Point2D[] points, int c, Box bbox)
    {
        while (c-- > 0) {
            bbox.min.x = Math.min(bbox.min.x, points[c].getX());
            bbox.min.y = Math.min(bbox.min.y, points[c].getY());
            bbox.max.x = Math.max(bbox.max.x, points[c].getX());
            bbox.max.y = Math.max(bbox.max.y, points[c].getY());
        }
    }

    private static long area(IPoint a, IPoint p, IPoint q) {
        return (long)p.x * q.y - (long)p.y * q.x +
                (long)a.x * (p.y - q.y) + (long)a.y * (q.x - p.x);
    }

    private static boolean ovl(Rng p, Rng q) {
        return p.mn < q.mx && q.mn < p.mx;
    }

    //--------------------------------------------------------------------------

    private long ssss;
    private double sclx;
    private double scly;

    private void cntrib(int f_x, int f_y, int t_x, int t_y, int w) {
        ssss += (long)w * (t_x - f_x) * (t_y + f_y) / 2;
    }

    private void
    fit(Point2D[] x, int cx, Vertex[] ix, int fudge, Box B)
    {
        int c = cx;
        while (c-- > 0) {
            ix[c] = new Vertex();
            ix[c].ip = new IPoint();
            ix[c].ip.x = ((int)((x[c].getX() - B.min.x) * sclx - mid) & ~7)
                    | fudge | (c & 1);
            ix[c].ip.y = ((int)((x[c].getY() - B.min.y) * scly - mid) & ~7)
                    | fudge;
        }

        ix[0].ip.y += cx & 1;
        ix[cx] = ix[0];

        c = cx;
        while (c-- > 0) {
            ix[c].rx = ix[c].ip.x < ix[c + 1].ip.x ?
                    new Rng(ix[c].ip.x, ix[c + 1].ip.x) :
                    new Rng(ix[c + 1].ip.x, ix[c].ip.x);
            ix[c].ry = ix[c].ip.y < ix[c + 1].ip.y ?
                    new Rng(ix[c].ip.y, ix[c + 1].ip.y) :
                    new Rng(ix[c + 1].ip.y, ix[c].ip.y);
            ix[c].in = 0;
        }
    }

    private void
    cross(Vertex a, Vertex b, Vertex c, Vertex d,
          double a1, double a2, double a3, double a4)
    {
        double r1 = a1 / (a1 + a2);
        double r2 = a3 / (a3 + a4);

        cntrib((int)(a.ip.x + r1 * (b.ip.x - a.ip.x)),
                (int)(a.ip.y + r1 * (b.ip.y - a.ip.y)),
                b.ip.x, b.ip.y, 1);
        cntrib(d.ip.x, d.ip.y,
                (int)(c.ip.x + r2 * (d.ip.x - c.ip.x)),
                (int)(c.ip.y + r2 * (d.ip.y - c.ip.y)),
                1);
        ++a.in;
        --c.in;
    }

    private void inness(Vertex[] P, int cP, Vertex[] Q, int cQ)
    {
        int s = 0;
        int c = cQ;
        IPoint p = P[0].ip;

        while (c-- > 0) {
            if (Q[c].rx.mn < p.x && p.x < Q[c].rx.mx) {
                boolean sgn = 0 < area(p, Q[c].ip, Q[c + 1].ip);
                s += (sgn != Q[c].ip.x < Q[c + 1].ip.x) ? 0 : (sgn ? -1 : 1);
            }
        }
        for (int j = 0; j < cP; ++j) {
            if (s != 0)
                cntrib(P[j].ip.x, P[j].ip.y,
                        P[j + 1].ip.x, P[j + 1].ip.y, s);
            s += P[j].in;
        }
    }

    //-------------------------------------------------------------------------

    private double
    inter(Point2D[] a, Point2D[] b)
    {
        int na = a.length;
        int nb = b.length;
        Vertex[] ipa = new Vertex[na + 1];
        Vertex[] ipb = new Vertex[nb + 1];
        Box bbox = new Box(new Point(Double.MAX_VALUE, Double.MAX_VALUE),
                new Point(-Double.MAX_VALUE, -Double.MAX_VALUE));

        if (na < 3 || nb < 3)
            return 0;

        range(a, na, bbox);
        range(b, nb, bbox);

        double rngx = bbox.max.x - bbox.min.x;
        sclx = gamut / rngx;
        double rngy = bbox.max.y - bbox.min.y;
        scly = gamut / rngy;
        double ascale = sclx * scly;

        fit(a, na, ipa, 0, bbox);
        fit(b, nb, ipb, 2, bbox);

        for (int j = 0; j < na; ++j) {
            for (int k = 0; k < nb; ++k) {
                if (ovl(ipa[j].rx, ipb[k].rx) && ovl(ipa[j].ry, ipb[k].ry)) {
                    long a1 = -area(ipa[j].ip, ipb[k].ip, ipb[k + 1].ip);
                    long a2 = area(ipa[j + 1].ip, ipb[k].ip, ipb[k + 1].ip);
                    boolean o = a1 < 0;
                    if (o == a2 < 0) {
                        long a3 = area(ipb[k].ip, ipa[j].ip, ipa[j + 1].ip);
                        long a4 = -area(ipb[k + 1].ip, ipa[j].ip,
                                ipa[j + 1].ip);
                        if (a3 < 0 == a4 < 0) {
                            if (o)
                                cross(ipa[j], ipa[j + 1], ipb[k], ipb[k + 1],
                                        a1, a2, a3, a4);
                            else
                                cross(ipb[k], ipb[k + 1], ipa[j], ipa[j + 1],
                                        a3, a4, a1, a2);
                        }
                    }
                }
            }
        }

        inness(ipa, na, ipb, nb);
        inness(ipb, nb, ipa, na);

        return ssss / ascale;
    }
}

