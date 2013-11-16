/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.prefabs;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;

/**
 *
 * @author ANURAG
 */
public class Asteroid extends Node {

    private SimpleApplication state;
    private static Material material;

    public Asteroid(SimpleApplication appState) {
        this.state = appState;
        setup();
    }

    private void setup() {
        if (material == null) {
            setupMaterial();
        }
        Sphere sphere = new Sphere(12, 16, 8.0f);
        Geometry sphereGeometry = new Geometry("asteroid", sphere);
        sphereGeometry.setMaterial(material);
        TangentBinormalGenerator.generate(sphereGeometry);
    }

    private void setupMaterial() {
        Material mat_lit = new Material(state.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat_lit.setTexture("DiffuseMap", state.getAssetManager().loadTexture("Textures/Asteroid.jpg"));
        mat_lit.setBoolean("UseMaterialColors", true);
        mat_lit.setColor("Specular", ColorRGBA.White);
        mat_lit.setColor("Diffuse", ColorRGBA.White);
        mat_lit.setFloat("Shininess", 1f); // [1,128]   
        material = mat_lit;
    }
}
