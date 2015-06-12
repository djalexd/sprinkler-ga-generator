package de.lighti.clipper;

public class ClipperMain {


    public static void main(String[] args) {

        Path area1 = new Path();
        area1.add(new Point.LongPoint(0, 0));
        area1.add(new Point.LongPoint(4, 0));
        area1.add(new Point.LongPoint(4, 4));
        area1.add(new Point.LongPoint(0, 4));
        area1.add(new Point.LongPoint(0, 0));

        Path area2 = new Path();
        area2.add(new Point.LongPoint(5, 6));
        area2.add(new Point.LongPoint(6, 6));
        area2.add(new Point.LongPoint(6, 5));
        area2.add(new Point.LongPoint(5, 5));
        area2.add(new Point.LongPoint(5, 6));

        Clipper clipper = new DefaultClipper();
        clipper.clear();
        clipper.addPath(area1, Clipper.PolyType.CLIP, true);
        clipper.addPath(area2, Clipper.PolyType.SUBJECT, true);

        Paths solution = new Paths();
        clipper.execute(Clipper.ClipType.INTERSECTION, solution);

        System.out.println(solution);
    }
}
