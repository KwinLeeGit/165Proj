package tage.shapes;

import tage.*;
import tage.shapes.*;

public class ManualHome extends ManualObject {
    private float[] vertices = new float[] {
        1.0f, -1.0f, 1.0f,      1.0f, -1.0f, -1.0f,     1.0f, 1.0f, -1.0f, //right1
        1.0f, -1.0f, 1.0f,      1.0f, 1.0f, -1.0f,     1.0f, 1.0f, 1.0f, //right2
        1.0f, -1.0f, -1.0f,     -1.0f, -1.0f, -1.0f,     -1.0f, 1.0f, -1.0f, //back1
        1.0f, -1.0f, -1.0f,     -1.0f, 1.0f, -1.0f,     1.0f, 1.0f, -1.0f, //back2
        -1.0f, -1.0f, -1.0f,    -1.0f, -1.0f, 1.0f,     -1.0f, 1.0f, 1.0f, //left1
        -1.0f, -1.0f, -1.0f,    -1.0f, 1.0f, 1.0f,     -1.0f, 1.0f, -1.0f, //left2
    };

    private float[] texcoords = new float[]{
        0.0f, 0.0f,     1.0f, 0.0f,     1.0f, 1.0f,
        0.0f, 0.0f,     1.0f, 1.0f,     0.0f, 1.0f,
        0.0f, 0.0f,     1.0f, 0.0f,     1.0f, 1.0f,
        0.0f, 0.0f,     1.0f, 1.0f,     0.0f, 1.0f,
        0.0f, 0.0f,     1.0f, 0.0f,     1.0f, 1.0f,
        0.0f, 0.0f,     1.0f, 1.0f,     0.0f, 1.0f,
    };

    private float[] normals = new float[] {
        1.0f, 0.0f, 0.0f,      1.0f, 0.0f, 0.0f,     1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,      1.0f, 0.0f, 0.0f,     1.0f, 0.0f, 0.0f,
        0.0f, -1.0f, 0.0f,       0.0f, -1.0f, 0.0f,      0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f,       0.0f, -1.0f, 0.0f,      0.0f, -1.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,      -1.0f, 0.0f, 0.0f,     -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,      -1.0f, 0.0f, 0.0f,     -1.0f, 0.0f, 0.0f,
    };

    public ManualHome(){
        super();

        setNumVertices(18);
        setVertices(vertices);
        setTexCoords(texcoords);
        setNormals(normals);

        setMatAmb(Utils.goldAmbient());
        setMatDif(Utils.goldDiffuse());
        setMatSpe(Utils.goldSpecular());
        setMatShi(Utils.goldShininess());
    }
}
