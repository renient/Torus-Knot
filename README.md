## Torus Knot Particle Field Made By Re Using(LWJGL / Java)

A small Java / LWJGL demo that renders a rotating **torus knot** made of particles, flying in a star field background, using OpenGL 3.3 core profile.

### Features

- **Torus knot particles**: 50,000 particles distributed along a $(p, q)$ torus knot (`P = 3`, `Q = 2`).
- **Star field background**: 2,000 randomly distributed star points.
- **Simple shaders**: Minimal vertex/fragment shaders for both the knot and the stars.
- **Smooth animation**: Continuous rotation of the knot in 3D space.

---

## Prerequisites

- **Java 21 JDK**
  - Any Java 21 JDK is fine (Oracle, Temurin, etc.).
  - Example: [Eclipse Temurin / Adoptium Java 21](https://adoptium.net)
- **Apache Maven 3.9+**
  - Download from: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
- **Graphics support**
  - A GPU and drivers that support **OpenGL 3.3** or higher.
  - On Windows, make sure your graphics drivers are up to date (NVIDIA / AMD / Intel).

You do **not** install LWJGL or JOML manually; Maven pulls them automatically from Maven Central using the `pom.xml`.

---

## Project Structure & Main Class

- **Main class**: `TorusKnotApp` (`src/main/java/TorusKnotApp.java`)
- **Build tool**: Maven (`pom.xml`)
- **Key libraries**:
  - **LWJGL 3.3.3**: Core, GLFW, OpenGL bindings
  - **JOML 1.10.5**: Matrices and vector math

`pom.xml` already declares:

- `org.lwjgl:lwjgl`
- `org.lwjgl:lwjgl-glfw`
- `org.lwjgl:lwjgl-opengl`
- `org.joml:joml`

and uses Windows natives via `natives-windows`.

---

## How to Build and Run

### 1. Verify Java and Maven

In **PowerShell** (or Command Prompt), run:

```bash
java -version
mvn -version
```

You should see Java 21 and a recent Maven version. If not:

- Install Java 21 (e.g. from [Adoptium](https://adoptium.net))
- Install Maven (from [Maven Download](https://maven.apache.org/download.cgi))
- Restart your terminal so `java` and `mvn` are on `PATH`.

### 2. Build the project

From the project root (`C:\Users\Impale\dsa`):

```bash
mvn clean compile
```

Maven will download LWJGL, JOML and all required native libraries on the first run.

### 3. Run the torus knot demo

Still in the project root:

```bash
mvn exec:java
```

The `exec-maven-plugin` in `pom.xml` is already configured with:

- **mainClass**: `TorusKnotApp`

This should open a **1280x720 window** titled **"Particle Torus Knot in Space"** with:

- A rotating, glowing blue torus knot made of particles.
- A starry background rendered as white points.

Close the window to exit the program.

---

## Troubleshooting

- **Black / empty window**
  - Ensure your GPU supports **OpenGL 3.3+** and drivers are current.
  - Try updating your graphics drivers from your GPU vendor.
- **Maven cannot find Java / `JAVA_HOME` not set**
  - Set `JAVA_HOME` to your Java 21 installation (for example):
    - In Windows System Properties → Environment Variables, set:
      - `JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-21...`
  - Reopen PowerShell after changing environment variables.
- **Native library issues (UnsatisfiedLinkError / LWJGL natives)**
  - Make sure you are on **Windows** (the POM uses `natives-windows`).
  - Run `mvn clean` and then `mvn exec:java` again so Maven re-resolves natives.

---

## Notes

- The torus knot parameters are controlled by:
  - `P = 3`, `Q = 2` (`TorusKnotApp` fields `P` and `Q`)
  - Major radius `RADIUS_MAJOR = 2.0f`
  - Minor radius `RADIUS_MINOR = 0.5f`
- You can experiment with different `P`, `Q`, radii, and particle counts for different shapes and densities.

---

## Math Behind the Torus Knot

### Torus Knot Curve

In `TorusKnotApp#createKnotParticles()`, each particle starts on a $(P, Q)$ torus knot curve.

- Parameters:
  - \( P = 3 \)
  - \( Q = 2 \)
  - Major radius \( R = \text{RADIUS\_MAJOR} \)
  - Minor radius \( r = \text{RADIUS\_MINOR} \)
- Angle parameter:
  - \( t \in [0, 2\pi) \)

The radius in the $xy$-plane varies as:

$$
r(t) = R + r \cos(Q t)
$$

Then the 3D coordinates of the torus knot are:

$$
\begin{aligned}
x(t) &= r(t)\cos(P t) \\
y(t) &= r(t)\sin(P t) \\
z(t) &= r\sin(Q t)
\end{aligned}
$$

This is exactly what the Java code computes:

- `r = RADIUS_MAJOR + RADIUS_MINOR * cos(Q * t)`
- `x = r * cos(P * t)`
- `y = r * sin(P * t)`
- `z = RADIUS_MINOR * sin(Q * t)`

### Thickening the Curve into a Tube

To make the knot look like a glowing strand (not just a line), each particle is offset a bit around the curve:

- Pick a random tube radius \( \rho \in [0, 0.2] \)
- Pick a random angle \( \phi \in [0, 2\pi) \)

Offset:

$$
\begin{aligned}
x' &= x(t) + \rho\cos\phi \\
y' &= y(t) + \rho\sin\phi \\
z' &= z(t) + \rho_z
\end{aligned}
$$

where \( \rho_z \) is a small random offset along $z$.  
That is what these lines implement:

- `offset = rand.nextFloat() * 0.2f;`
- `angle = random in [0, 2π]`
- `x' = x + cos(angle) * offset`
- `y' = y + sin(angle) * offset`
- `z' = z + random() * offset`

### Star Field Distribution

Stars are sampled uniformly in a cube:

$$
x, y, z \sim \text{Uniform}(-25, 25)
$$

which corresponds to:

- `x = (rand - 0.5) * 50`
- `y = (rand - 0.5) * 50`
- `z = (rand - 0.5) * 50`

### Camera and Projection

- **Projection matrix** (perspective):
  - Field of view \( \text{FOV} = 45^\circ \)
  - Aspect ratio \( a = 1280 / 720 \)
  - Near plane \( n = 0.1 \), far plane \( f = 100 \)
  - Built via `new Matrix4f().perspective(...)`.

- **View matrix** (camera):
  - Eye: \( (0, 0, 12) \)
  - Target: \( (0, 0, 0) \)
  - Up: \( (0, 1, 0) \)
  - Built via `new Matrix4f().lookAt(...)`.

The vertex shader multiplies position by:

$$
\text{gl\_Position} = P \cdot V \cdot M \cdot \begin{bmatrix} x' \\ y' \\ z' \\ 1 \end{bmatrix}
$$

where:

- \( P \) = projection matrix
- \( V \) = view matrix
- \( M \) = model (rotation) matrix

### Rotation

Each frame, the knot is rotated by a small angle:

- Angle update: \( \theta \leftarrow \theta + 0.005 \)
- Axis: \( \mathbf{a} = (0.5, 1, 0) \) (normalized internally)

The model matrix is a 3D rotation around axis \( \mathbf{a} \) by angle \( \theta \), using Rodrigues’ rotation formula (handled by JOML).



