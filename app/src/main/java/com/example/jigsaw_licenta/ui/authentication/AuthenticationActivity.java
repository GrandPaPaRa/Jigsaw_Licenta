package com.example.jigsaw_licenta.ui.authentication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.render3D.ZShapeRenderer;

import org.rajawali3d.view.SurfaceView;
import org.rajawali3d.view.TextureView;

public class AuthenticationActivity extends AppCompatActivity {
    private TextureView zShapeView;
    private ZShapeRenderer renderer;
    private float previousX;
    private float previousY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Initialize Rajawali surface view
        zShapeView = findViewById(R.id.z_shape_view);
        renderer = new ZShapeRenderer(this);
        zShapeView.setSurfaceRenderer(renderer);

        zShapeView.setOpaque(false);
        //zShapeView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Set touch listener for rotating the Z shape
        zShapeView.setOnTouchListener((v, event) -> {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Stop auto-rotation on first touch
                    renderer.applyUserRotation(0, 0);
                    break;

                case MotionEvent.ACTION_MOVE:
                    float dx = x - previousX;
                    float dy = y - previousY;

                    // Convert swipe speed into rotation velocity
                    renderer.applyUserRotation(dy * 0.1, dx * 0.1);
                    break;
            }

            previousX = x;
            previousY = y;
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (zShapeView != null) {
            zShapeView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (zShapeView != null) {
            zShapeView.onPause();
        }
    }
}