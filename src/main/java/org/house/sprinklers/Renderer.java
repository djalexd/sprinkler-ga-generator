package org.house.sprinklers;

import com.google.common.base.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.house.sprinklers.population.SprinklerValidator;
import org.house.sprinklers.sprinkler_system.SprinklerSystem;
import org.house.sprinklers.sprinkler_system.SprinklerSystemGenome;
import org.house.sprinklers.sprinkler_system.Terrain;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import java.awt.geom.Point2D;
import java.io.InputStream;

@Slf4j

@Data
public class Renderer implements Runnable {

    private double scale = 50;

    private final Terrain terrain;

    private volatile SprinklerSystem sprinklerSystem = null;

    private float xOffset = 0, yOffset = 0;

    public Renderer(Terrain terrain) {
        this.terrain = terrain;
    }

    public void setSprinklerSystem(SprinklerSystem sprinklerSystem) {
        this.sprinklerSystem = sprinklerSystem;
    }

    /**
     * Different render thread.
     */
    @Override
    public void run() {

        start();

        while (!Display.isCloseRequested()) {

            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GL11.glTranslatef(xOffset, yOffset, 0);

            // Clear the screen and depth buffer
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // draw terrain
            GL11.glColor3f(0.0f, 0.6f, 0.0f);
            GL11.glBegin(GL11.GL_QUADS);
            for (Point2D p : terrain.getPolygonPoints()) {
                GL11.glVertex2d(scale * p.getX(), scale * p.getY());
            }
            GL11.glEnd();

            if (sprinklerSystem != null) {

                // draw sprinklers
                GL11.glColor4f(0.4f, 0.0f, 1.0f, 0.3f);
                for (int i = 0; i < sprinklerSystem.getSprinklers().length; i++) {
                    GL11.glBegin(GL11.GL_POLYGON);
                    for (Point2D p : sprinklerSystem.getSprinklers()[i].getPolygonPoints()) {
                        GL11.glVertex2d(scale * p.getX(), scale * p.getY());
                    }
                    GL11.glEnd();
                }

            }

            pollInput();
            Display.update();
            Display.sync(60);
        }

    }

    private void start() {

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

        /*
        while (!Display.isCloseRequested()) {

            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GL11.glTranslatef(xOffset, yOffset, 0);

            // Clear the screen and depth buffer
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // draw terrain
            GL11.glColor3f(0.0f, 0.6f, 0.0f);
            GL11.glBegin(GL11.GL_QUADS);
            for (Point2D p : terrain.getPolygonPoints()) {
                GL11.glVertex2d(scale * p.getX(), scale * p.getY());
            }
            GL11.glEnd();

            if (sprinklerSystem != null) {

                // draw sprinklers
                GL11.glColor4f(0.4f, 0.0f, 1.0f, 0.3f);
                for (int i = 0; i < sprinklerSystem.getSprinklers().length; i++) {
                    GL11.glBegin(GL11.GL_POLYGON);
                    for (Point2D p : sprinklerSystem.getSprinklers()[i].getPolygonPoints()) {
                        GL11.glVertex2d(scale * p.getX(), scale * p.getY());
                    }
                    GL11.glEnd();
                }

            }

            pollInput();
            Display.update();
            Display.sync(60);
        }

        Display.destroy();
        */
    }


    public void pollInput() {

        while (Keyboard.next()) {

            if (Keyboard.getEventKeyState()) {

                if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                    xOffset -= 10;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_S) {
                    yOffset -= 10;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_D) {
                    xOffset += 10;
                }

                if (Keyboard.getEventKey() == Keyboard.KEY_W) {
                    yOffset += 10;
                }

            } else {
                if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_S) {
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_D) {
                }
            }
        }
    }


    public static void main(String[] argv) throws Exception {


        final Terrain t = new Terrain.TerrainLoader().load(resourceAsStream("terrain.in"));

        String genomeString = "400ec45d2367e090400e7788f6473ea6401784cce72297503fed31e6623899904015e16fce55b7a5400de13187de397e400d33da541b6ef4400647c226c524ac4010c7b66c44956e4014b8b98c2c1251400f584e44bf24d53fe74e45781135463ff5cede9b4d27b43ff5ed81b5e029a6400491ed6d7d1a113fef91ebec2c7688401120e202fe2422400ceeff657b4f153fe5b7bac37732853ffd389f4d43776e3fcd0157554d97c83ff7faf8fc3b58f540031dadd68f9e313fe58700f9ee0e194019198aa20f07593fedfc9694f728043ff62bb5470eef054015c6b0a6a980083ff00a1960c8144d3ff41bd937812074400d6b1c0250fd22400306a210ea90954018980d91b24a403fe8f90c95d7934c40022aeccd8d2c494005f235fe1a1cab3fedca8d991ae7624017465ba814ec4e401557f19ce9a4b1401817d7cdd353f33ff7d74520e8b7ab400ee5ba40db7700400843854174b3ee3ffb0d44c865720540066f0b8ee954e9";
        byte[] genes = decodeAsByteArray(genomeString);
        SprinklerSystemGenome genome = new SprinklerSystemGenome(genes);
        log.info("Gene size {}", genes.length);
        SprinklerSystem system = new SprinklerSystemGenome.GenomeLoader(Optional.<SprinklerValidator>absent()).load(genome);

        System.setProperty("org.lwjgl.librarypath", "/Users/alexdobjanschi/workspace/sprinkler-ga-generator/target/natives");

        Renderer renderer = new Renderer(t);
        renderer.setSprinklerSystem(system);
        renderer.start();
    }


    private static InputStream resourceAsStream(String byName) {
        return SprinklerGARunner.class.getResourceAsStream(byName);
    }

    private static String encodeAsHexString(byte[] bytes) {
        return new String(Hex.encodeHex(bytes));
    }

    private static byte[] decodeAsByteArray(String hexString) {
        try {
            return Hex.decodeHex(hexString.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalArgumentException(String.format("Invalid hex string: %s", hexString));
        }
    }
}