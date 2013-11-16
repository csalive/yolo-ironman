package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.ui.Picture;
import com.jme3.util.SkyFactory;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import mygame.prefabs.Asteroid;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    private Node target;
    private Vector3f dir;
    private Node asteroids;
    private BitmapText score;
    private BitmapText timeLeft;
    private BitmapText gameOver;
    private static final String COLLECTABLE_ID = "col";
    private Timer timer;
    private AudioNode background;
    private AudioNode collision;
    private int timeLimit = Level.timeLimit;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        timer = new Timer();
        asteroids = new Node("Asteroid group");
        setupMainCharacter(assetManager.loadModel("Models/SpaceCraft/Rocket.mesh.xml"));
        setupLightning();
        setupCamera(); //Side effect function as its invocation depends upon the order of invocation
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
        placeRandomObjects(250);
        setupKeyMappings();
        setupHUD();
//        setupLifes();
        setupEnvironment();
        setupAudio();
        setDisplayStatView(Level.DEBUG);
        setDisplayFps(Level.DEBUG);
    }

    private void setupLightning() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

//        DirectionalLight sun1 = new DirectionalLight();
//        sun1.setDirection((new Vector3f(1f, 0.5f, 0.5f)).normalizeLocal());
//        sun1.setColor(ColorRGBA.White);
//        rootNode.addLight(sun1);
    }

    private void setupEnvironment() {
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/skybox.dds", false));
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        FogFilter fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(0.45f, 0.69f, 0.98f, 1.0f));
        fog.setFogDistance(155);
        fog.setFogDensity(0.55f);
        fpp.addFilter(fog);
        viewPort.addProcessor(fpp);

        FilterPostProcessor fpp1 = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter();;
        fpp1.addFilter(bloom);
        viewPort.addProcessor(fpp1);
    }

    private void setupMainCharacter(Spatial node) {
        dir = cam.getDirection().clone().normalize();
        target = new Node("spaceship");
        target.attachChild(node);
        target.lookAt(dir, cam.getUp().clone());

        Geometry g = new Geometry("wireframe sphere", new WireBox(0.4f, 0.4f, 1.0f));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Blue);
        g.setMaterial(mat);
        target.attachChild(g);
        rootNode.attachChild(target);
    }

    private void setupCamera() {
        flyCam.setEnabled(false);
        ChaseCamera gameCam = new ChaseCamera(cam, target, inputManager);
        gameCam.setSmoothMotion(true);
        gameCam.setMaxDistance(12.6f);
        gameCam.setMinDistance(10.5f);
    }

    private void setupKeyMappings() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));

        inputManager.addListener(new AnalogListener() {
            public void onAnalog(String name, float value, float tpf) {
                if (name.equals("Left")) {
                    float rotAngle = 0.9f * tpf;
                    target.rotate(0, rotAngle, 0);
                    Quaternion rot = new Quaternion();
                    rot.fromAngleAxis(rotAngle, Vector3f.UNIT_Y);
                    dir = rot.toRotationMatrix().mult(dir).normalize();
                } else if (name.equals("Right")) {
                    float rotAngle = -0.9f * tpf;
                    target.rotate(0, rotAngle, 0);
                    Quaternion rot = new Quaternion();
                    rot.fromAngleAxis(rotAngle, Vector3f.UNIT_Y);
                    dir = rot.toRotationMatrix().mult(dir).normalize();
                } else if (name.equals("Up")) {
                    float steps = 5f * tpf;
                    target.move(0, steps, 0);
                } else if (name.equals("Down")) {
                    float steps = -20f * tpf;
                    target.move(0, steps, 0);
                }
            }
        }, new String[]{"Left", "Right", "Up", "Down"});

    }

    private void placeRandomObjects(int count) {
        assert (count > 0);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Black);

        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", new ColorRGBA(30.0f / 255, 144.0f / 255, 254.0f / 255, 1));
        mat2.setColor("GlowColor", new ColorRGBA(30.0f / 255, 144.0f / 255, 254.0f / 255, 1));
        mat2.setTransparent(true);
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);


        Material mat_lit = new Material(getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat_lit.setTexture("DiffuseMap", getAssetManager().loadTexture("Textures/Asteroid.jpg"));
        mat_lit.setBoolean("UseMaterialColors", true);
        mat_lit.setColor("Specular", ColorRGBA.White);
        mat_lit.setColor("Diffuse", ColorRGBA.White);
        mat_lit.setColor("GlowColor", ColorRGBA.Gray);
        mat_lit.setFloat("Shininess", 1f); // [1,128]   

        int tempLayers = Level.layers;

        while (tempLayers-- > 0) {
            int z = Math.abs(rand.nextInt()) % 150;
            int n1 = Math.abs(rand.nextInt()) % Level.objPerLayer;
            int n2 = Level.objPerLayer - n1;
            int tmp = Math.min(n1, n2);
            n2 = tmp;
            n1 = Level.objPerLayer - n2;
            while (n1-- > 0) {
                int x = rand.nextInt() % 400;
                int y = rand.nextInt() % 400;
//                Box box = new Box(1.5f, 1.5f, 1.5f);
                Sphere dot = new Sphere(24, 24, 8.0f);
                Geometry boxGeom = new Geometry("obstacle", dot);
                boxGeom.move(new Vector3f(x, y, z));
                boxGeom.setMaterial(mat_lit);
                asteroids.attachChild(boxGeom);
            }
            while (n2-- > 0) {
                int x = rand.nextInt() % 200;
                int y = rand.nextInt() % 200;
                Box box = new Box(1.5f, 1.5f, 1.5f);
                Geometry boxGeom = new Geometry(COLLECTABLE_ID, box);
                boxGeom.move(new Vector3f(x, y, z));
                boxGeom.setMaterial(mat2);
                asteroids.attachChild(boxGeom);
            }
        }
        rootNode.attachChild(asteroids);
    }

    private void placeAsteroids(int count) {
        assert (count > 0);
        for (int i = 0; i < count; i++) {
            Asteroid ast = new Asteroid(this);
            int x = rand.nextInt() % 320;
            int y = rand.nextInt() % 320;
            int z = rand.nextInt() % 320;
            ast.move(x, y, z);
            asteroids.attachChild(ast);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f t = dir.clone();
        t = t.mult(Level.factor * tpf);
        target.move(t);

        CollisionResults results = new CollisionResults();
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        asteroids.collideWith(ray, results);
        for (int i = 0; i < results.size(); i++) {
            float dist = results.getCollision(i).getDistance();
            if (dist <= 20f) { //Collided
                String id = results.getCollision(i).getGeometry().getName();
                Geometry geom = results.getCollision(i).getGeometry();
                if (id.equals(COLLECTABLE_ID)) {

                    Level.score++;
                } else if (id.equals("obstacle")) {
                    Level.score--;
                } else if (id.equals("asteroid")) {
                }
                collision.playInstance();
                asteroids.detachChild(geom);
                setScore();
            }
        }
        if (timeLimit < 0) {
            timer.cancel();
            gameOver.setText("GAME OVER");
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    private void setupHUD() {
        score = new BitmapText(guiFont, false);
        score.setSize(2 * guiFont.getCharSet().getRenderedSize());
        score.setColor(ColorRGBA.White);
        score.setText("0");
        score.setLocalTranslation(settings.getWidth()-80, score.getLineHeight(), 0);
        guiNode.attachChild(score);

        gameOver = new BitmapText(guiFont, false);
        gameOver.setSize(4 * guiFont.getCharSet().getRenderedSize());
        gameOver.setColor(ColorRGBA.White);
        gameOver.setLocalTranslation(180, score.getLineHeight() + 100, 0);
        guiNode.attachChild(gameOver);

        timeLeft = new BitmapText(guiFont);
        timeLeft.setSize(2 * guiFont.getCharSet().getRenderedSize());
        timeLeft.setColor(ColorRGBA.White);
        timeLeft.setLocalTranslation(40, score.getLineHeight(), 0);
        guiNode.attachChild(timeLeft);

        Picture watch = new Picture("Watch");
        watch.setImage(assetManager, "Textures/hourglass.png", true);
        watch.setWidth(32f);
        watch.setHeight(32f);
        watch.setPosition(4f, timeLeft.getLineHeight()-36);
        guiNode.attachChild(watch);
        
        Picture star = new Picture("Watch");
        star.setImage(assetManager, "Textures/stars.png", true);
        star.setWidth(32f);
        star.setHeight(32f);
        star.setPosition(settings.getWidth()-120f, timeLeft.getLineHeight()-36);
        guiNode.attachChild(star);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeLeft.setText(Integer.toString(timeLimit--));
//                System.out.println(timeLimit);
                if (timeLimit < 0) {
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }

    private void setScore() {
        String s = "" + Level.score;
        score.setText(s);
    }
    private LinkedList<Picture> lifeList = new LinkedList<Picture>();

    private void setupLifes() {
        for (int i = 0; i < Level.lifes; ++i) {
            Picture pic = new Picture("lifes");
            pic.setImage(assetManager, "Textures/lifes.png", true);
            pic.setWidth(24f);
            pic.setHeight(24f);
            pic.setPosition(10f, (settings.getHeight() / 4) + 24 * i + 8);
            lifeList.add(pic);
            guiNode.attachChild(pic);
        }
    }
    private static Random rand = new Random();

    private void setupAudio() {
        collision = new AudioNode(assetManager, "Sound/Effects/Gun.wav", false);
        collision.setLooping(false);
        collision.setVolume(2);
        rootNode.attachChild(collision);

        /* nature sound - keeps playing in a loop. */
        background = new AudioNode(assetManager, "Sounds/Night_of_Chaos.ogg", false);
        background.setLooping(true);  // activate continuous playing
        background.setPositional(true);
        background.setLocalTranslation(Vector3f.ZERO.clone());
        background.setVolume(3);
        rootNode.attachChild(background);
        background.play(); // play continuously!
    }
}