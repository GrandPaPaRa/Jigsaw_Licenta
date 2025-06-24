package com.example.jigsaw_licenta.render3D;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.renderer.Renderer;
import java.util.ArrayList;
import java.util.List;

public class ZShapeRenderer extends Renderer {
    private DirectionalLight directionalLight;
    private Object3D zShapeContainer; // Container for all cubes
    private double rotationY = 0;
    private double rotationX = 0;
    private boolean autoRotate = true;
    private double velocityX = 0;
    private double velocityY = 0;

    public ZShapeRenderer(Context context) {
        super(context);
        setFrameRate(60);
    }

    @Override
    protected void initScene() {

        getCurrentScene().setBackgroundColor(0.255f, 0.247f, 0.259f, 1f);


        // Set up lighting
        directionalLight = new DirectionalLight(1f, 0.2f, -1.0f);
        directionalLight.setColor(1.0f, 1.0f, 1.0f);
        directionalLight.setPower(2);
        getCurrentScene().addLight(directionalLight);

        // Create a container to hold all cubes
        zShapeContainer = new Object3D();

        // Create material for cubes with red color and border effect
        Material cubeMaterial = new Material();
        cubeMaterial.setColor(Color.RED);

        // For border effect we'll use:
        // 1. Diffuse method for base color
        cubeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        // 2. Specular method for edge highlights
        SpecularMethod.Phong phongMethod = new SpecularMethod.Phong();
        phongMethod.setShininess(100);
        cubeMaterial.setSpecularMethod(phongMethod);
        //cubeMaterial.setS // White edge highlights

        // 3. Ambient color for darker edges
        cubeMaterial.setAmbientColor(Color.rgb(100, 0, 0)); // Dark red ambient

        // Define the Z pattern (1 = cube, 0 = empty)
        int[][] zPattern = {
                {1, 1, 0},
                {0, 1, 1}
        };

        // Calculate center offset for proper rotation
        double centerX = (zPattern[0].length - 1) / 2.0;
        double centerY = (zPattern.length - 1) / 2.0;

        float thickness = 0.1f; // Edge thickness

        for (int row = 0; row < zPattern.length; row++) {
            for (int col = 0; col < zPattern[row].length; col++) {
                if (zPattern[row][col] == 1) {
                    float x = col - (float)centerX;
                    float y = -(row - (float)centerY);

                    // Red inner cube
                    Cube redCube = new Cube(1f, false, false);
                    redCube.setMaterial(cubeMaterial);
                    redCube.setPosition(x, y, 0);
                    zShapeContainer.addChild(redCube);

                    // Neighbors
                    boolean top    = row > 0 && zPattern[row - 1][col] == 1;
                    boolean bottom = row < zPattern.length - 1 && zPattern[row + 1][col] == 1;
                    boolean left   = col > 0 && zPattern[row][col - 1] == 1;
                    boolean right  = col < zPattern[row].length - 1 && zPattern[row][col + 1] == 1;

                    Material edgeMaterial = new Material();
                    edgeMaterial.setColor(Color.BLACK);
                    edgeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
                    edgeMaterial.enableLighting(true);

                    if (!top) {
                        Cube edge = new Cube(1f, false, false);
                        edge.setMaterial(edgeMaterial);
                        edge.setScaleX(1f);
                        edge.setScaleY(thickness);
                        edge.setScaleZ(1.01f);
                        edge.setPosition(x, y + 0.5f, 0);
                        zShapeContainer.addChild(edge);
                    }
                    if (!bottom) {
                        Cube edge = new Cube(1f, false, false);
                        edge.setMaterial(edgeMaterial);
                        edge.setScaleX(1f);
                        edge.setScaleY(thickness);
                        edge.setScaleZ(1.01f);
                        edge.setPosition(x, y - 0.5f, 0);
                        zShapeContainer.addChild(edge);
                    }
                    if (!left) {
                        Cube edge = new Cube(1f, false, false);
                        edge.setMaterial(edgeMaterial);
                        edge.setScaleX(thickness);
                        edge.setScaleY(1f);
                        edge.setScaleZ(1.01f);
                        edge.setPosition(x - 0.5f, y, 0);
                        zShapeContainer.addChild(edge);
                    }
                    if (!right) {
                        Cube edge = new Cube(1f, false, false);
                        edge.setMaterial(edgeMaterial);
                        edge.setScaleX(thickness);
                        edge.setScaleY(1f);
                        edge.setScaleZ(1.01f);
                        edge.setPosition(x + 0.5f, y, 0);
                        zShapeContainer.addChild(edge);
                    }
                }
            }
        }


        // Add the container to the scene
        getCurrentScene().addChild(zShapeContainer);
        getCurrentCamera().setZ(5); // Move camera back
    }

    @Override
    protected void onRender(long elapsedTime, double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        if (autoRotate) {
            // Auto-rotation mode
            rotationY += 0.5;
            rotationX += 0.3;
        } else {
            // Apply momentum-based rotation
            rotationX += velocityX;
            rotationY += velocityY;

            // Slow down gradually (damping)
            velocityX *= 0.95;
            velocityY *= 0.95;

            // Optional: resume auto-rotation if velocity gets very small
            if (Math.abs(velocityX) < 0.01 && Math.abs(velocityY) < 0.01) {
                autoRotate = true;
            }
        }
        zShapeContainer.setRotation(rotationX, rotationY, 0);
    }

    public void setRotation(double x, double y) {
        rotationX = x;
        rotationY = y;
    }
    public void applyUserRotation(double vx, double vy) {
        autoRotate = false;
        this.velocityX = vx;
        this.velocityY = vy;
    }

    @Override
    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j) {}

    @Override
    public void onTouchEvent(MotionEvent event) {}

}