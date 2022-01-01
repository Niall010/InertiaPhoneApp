package com.example.inertiaphoneapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.SharedCamera;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARTrackActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    private Session mSession;
    private Camera camera;
    //3d model credit : google.poly.com
    private String Model_URL = "https://github.com/Niall010/InertiaPhoneApp/blob/master/app/sampledata/AnchorModel.glb?raw=true";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artrack);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        setUpModel();
        setUpPlane();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUpModel() {
        ModelRenderable.builder()
                .setSource(this,
                        RenderableSource.builder().setSource(
                                this,
                                Uri.parse(Model_URL),
                                RenderableSource.SourceType.GLB)
                                .setScale(0.01f)
                                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                                .build())

                .setRegistryId(Model_URL)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Log.i("Model","cant load");
                    Toast.makeText(ARTrackActivity.this,"Model can't be Loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });

    }

    private void setUpPlane(){

        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            createAnchor(anchorNode);
        }));

    }

    private void createAnchor(AnchorNode anchorNode){
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.select();
    }

}