package org.house.sprinklers;

import org.house.sprinklers.sprinkler_system.Sprinkler;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;
import org.house.sprinklers.sprinkler_system.terrain.Terrain;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import java.awt.geom.Point2D;

public class GameRenderer implements Runnable {
    private static final double SCALE = 50;

    private volatile boolean running = true;

    private Point2D.Double offset = new Point2D.Double();

    private Terrain terrain;

    public GameRenderer(Terrain terrain) {
        this.terrain = terrain;
    }

    private volatile SprinklerSystem sprinklerSystem;

    public void setSprinklerSystem(SprinklerSystem sprinklerSystem) {
        this.sprinklerSystem = sprinklerSystem;
    }

    @Override
    public void run() {

        init();

        while (running) {

            performLogic();
            render();
            pollInput();

            if (Display.isCloseRequested()) {
                running = false;
            }

        }

        Display.destroy();
    }

    private void performLogic() {
        /* No-op, logic is performed someplace else. */
    }


    private void render() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslated(offset.getX(), offset.getY(), 0);

        // Clear the screen and depth buffer
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        drawTerrain();
        drawSprinklerSystem();

        Display.update();
        Display.sync(60);
    }

    private void drawSprinklerSystem() {
        if (sprinklerSystem == null) {
            return;
        }

        // draw sprinklers
        GL11.glColor4f(0.4f, 0.0f, 1.0f, 0.3f);
        for (Sprinkler sprinkler : sprinklerSystem.getSprinklers()) {
            GL11.glBegin(GL11.GL_POLYGON);
            for (Point2D p : sprinkler.getPolygonPoints()) {
                GL11.glVertex2d(SCALE * p.getX(), SCALE * p.getY());
            }
            GL11.glEnd();
        }
    }

    private void drawTerrain() {
        // draw terrain
        GL11.glColor3f(0.0f, 0.6f, 0.0f);
        GL11.glBegin(GL11.GL_QUADS);
        for (Point2D p : terrain.getPolygonPoints()) {
            GL11.glVertex2d(SCALE * p.getX(), SCALE * p.getY());
        }
        GL11.glEnd();
    }


    private void init() {

        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        // init OpenGL
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, 800, 0, 600, 1, -1);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }


    private void pollInput() {

        while (Keyboard.next()) {

            if (Keyboard.getEventKeyState()) {
                switch (Keyboard.getEventKey()) {
                    case Keyboard.KEY_A:
                        offset.x -= 10;
                        break;
                    case Keyboard.KEY_D:
                        offset.x += 10;
                        break;
                    case Keyboard.KEY_S:
                        offset.y -= 10;
                        break;
                    case Keyboard.KEY_W:
                        offset.y += 10;
                        break;
                }
            }

        }
    }
}
