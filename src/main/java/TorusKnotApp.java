import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.Random;

public class TorusKnotApp {

    private long window;
    private int vao, vbo;
    private int starVao, starVbo;
    private int shaderProgram, starShaderProgram;

    private final int PARTICLE_COUNT = 50000;
    private final int STAR_COUNT = 2000;

    // Knot params
    private final int P = 3;
    private final int Q = 2;
    private final float RADIUS_MAJOR = 2.0f;
    private final float RADIUS_MINOR = 0.5f;

    public static void main(String[] args) {
        new TorusKnotApp().run();
    }

    public void run() {
        init();
        loop();

        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit())
            throw new IllegalStateException();

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        window = GLFW.glfwCreateWindow(1280, 720, "Particle Torus Knot in Space", MemoryUtil.NULL, MemoryUtil.NULL);
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // Additive blending for glow
        GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);

        initShaders();
        createKnotParticles();
        createStars();
    }

    private void initShaders() {
        String vert = "#version 330 core\n" +
                "layout (location = 0) in vec3 aPos;\n" +
                "uniform mat4 model, view, projection;\n" +
                "void main() {\n" +
                "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
                "    gl_PointSize = 2.0;\n" +
                "}\n";

        String frag = "#version 330 core\n" +
                "out vec4 FragColor;\n" +
                "void main() {\n" +
                "    FragColor = vec4(0.4, 0.7, 1.0, 0.6);\n" +
                "}\n";

        String starVert = "#version 330 core\n" +
                "layout (location = 0) in vec3 aPos;\n" +
                "uniform mat4 view, projection;\n" +
                "void main() {\n" +
                "    gl_Position = projection * view * vec4(aPos, 1.0);\n" +
                "    gl_PointSize = 1.2;\n" +
                "}\n";

        String starFrag = "#version 330 core\n" +
                "out vec4 FragColor;\n" +
                "void main() {\n" +
                "    FragColor = vec4(1.0, 1.0, 1.0, 0.8);\n" +
                "}\n";

        shaderProgram = createProgram(vert, frag);
        starShaderProgram = createProgram(starVert, starFrag);
    }

    private int createProgram(String v, String f) {
        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vs, v);
        GL20.glCompileShader(vs);
        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fs, f);
        GL20.glCompileShader(fs);
        int p = GL20.glCreateProgram();
        GL20.glAttachShader(p, vs);
        GL20.glAttachShader(p, fs);
        GL20.glLinkProgram(p);
        return p;
    }

    private void createKnotParticles() {
        float[] data = new float[PARTICLE_COUNT * 3];
        Random rand = new Random();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float t = (float) (rand.nextFloat() * 2.0 * Math.PI);
            float offset = rand.nextFloat() * 0.2f;
            float angle = (float) (rand.nextFloat() * 2.0 * Math.PI);

            float r = RADIUS_MAJOR + RADIUS_MINOR * (float) Math.cos(Q * t);
            float x = r * (float) Math.cos(P * t);
            float y = r * (float) Math.sin(P * t);
            float z = RADIUS_MINOR * (float) Math.sin(Q * t);

            data[i * 3] = x + (float) Math.cos(angle) * offset;
            data[i * 3 + 1] = y + (float) Math.sin(angle) * offset;
            data[i * 3 + 2] = z + rand.nextFloat() * offset;
        }

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);
    }

    private void createStars() {
        float[] data = new float[STAR_COUNT * 3];
        Random rand = new Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            data[i * 3] = (rand.nextFloat() - 0.5f) * 50.0f;
            data[i * 3 + 1] = (rand.nextFloat() - 0.5f) * 50.0f;
            data[i * 3 + 2] = (rand.nextFloat() - 0.5f) * 50.0f;
        }
        starVao = GL30.glGenVertexArrays();
        starVbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(starVao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, starVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);
    }

    private void loop() {
        Matrix4f proj = new Matrix4f().perspective((float) Math.toRadians(45), 1280f / 720f, 0.1f, 100f);
        Matrix4f view = new Matrix4f().lookAt(0, 0, 12, 0, 0, 0, 0, 1, 0);
        float angle = 0;
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);

        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            angle += 0.005f;

            // Draw Stars
            GL20.glUseProgram(starShaderProgram);
            GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(starShaderProgram, "projection"), false, proj.get(fb));
            GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(starShaderProgram, "view"), false, view.get(fb));
            GL30.glBindVertexArray(starVao);
            GL11.glDrawArrays(GL11.GL_POINTS, 0, STAR_COUNT);

            // Draw Knot
            GL20.glUseProgram(shaderProgram);
            Matrix4f model = new Matrix4f().rotate(angle, 0.5f, 1, 0);
            GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderProgram, "projection"), false, proj.get(fb));
            GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderProgram, "view"), false, view.get(fb));
            GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderProgram, "model"), false, model.get(fb));
            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_POINTS, 0, PARTICLE_COUNT);

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }
}
