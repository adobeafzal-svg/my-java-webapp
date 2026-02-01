# Java CI/CD Tutorial: From Local Maven Build to Harness Pipeline

A hands-on guide for Java developers familiar with Ant/J2EE transitioning to modern CI/CD practices.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Part 1: Understanding the Project Structure](#part-1-understanding-the-project-structure)
3. [Part 2: Local Maven Build](#part-2-local-maven-build)
4. [Part 3: Setting Up Harness CI](#part-3-setting-up-harness-ci)
5. [Part 4: Running Your First Pipeline](#part-4-running-your-first-pipeline)
6. [Part 5: Next Steps](#part-5-next-steps)

---

## Project Overview

This tutorial uses a simple Java web application that includes:

- **HelloServlet**: Returns JSON greetings via REST-like endpoint
- **HealthCheckServlet**: Provides health endpoints for CI/CD verification
- **GreetingService**: Business logic separated for testability
- **Unit Tests**: JUnit 5 tests demonstrating modern testing practices

**What You'll Learn:**

1. How Maven replaces Ant for builds
2. How to run builds locally
3. How to set up Harness CI for automated builds
4. How pipelines trigger on code changes

---

## Part 1: Understanding the Project Structure

### Project Layout

```
my-webapp/
├── pom.xml                          # Maven build file (replaces build.xml)
├── Dockerfile                       # For containerization
├── .gitignore                       # Git ignore rules
├── .harness/
│   └── pipeline.yaml                # Harness CI pipeline definition
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/
    │   │       ├── HelloServlet.java
    │   │       ├── HealthCheckServlet.java
    │   │       └── GreetingService.java
    │   └── webapp/
    │       ├── index.html
    │       └── WEB-INF/
    │           └── web.xml
    └── test/
        └── java/
            └── com/example/
                ├── GreetingServiceTest.java
                └── HelloServletTest.java
```

### Mapping Ant Concepts to Maven

| Ant Concept | Maven Equivalent |
|-------------|------------------|
| `build.xml` | `pom.xml` |
| `<property>` | `<properties>` section |
| `<path>` with JARs | `<dependencies>` section |
| `<javac>` task | Implicit (runs automatically) |
| `<junit>` task | `maven-surefire-plugin` |
| `<war>` task | `maven-war-plugin` |
| `<target>` | Phase/Goal (compile, test, package) |

### Key Differences

**Ant**: You explicitly define every step.
```xml
<target name="compile" depends="init">
    <javac srcdir="src" destdir="build/classes">
        <classpath refid="project.classpath"/>
    </javac>
</target>
```

**Maven**: Convention over configuration. Standard directory structure means Maven knows what to do.
```xml
<!-- Just declare your dependencies, Maven handles the rest -->
<dependencies>
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <version>5.0.0</version>
    </dependency>
</dependencies>
```

---

## Part 2: Local Maven Build

### Prerequisites

1. **Java JDK 17+** installed
   ```bash
   java -version
   # Should show: openjdk version "17.x.x" or higher
   ```

2. **Maven 3.9+** installed
   ```bash
   mvn -version
   # Should show: Apache Maven 3.9.x
   ```

### Step 1: Navigate to Project Directory

```bash
cd my-webapp
```

### Step 2: Understand Maven Lifecycle

Maven has a standard lifecycle. Each phase automatically runs all preceding phases:

```
validate → compile → test → package → verify → install → deploy
```

Common commands:

| Command | What It Does | Ant Equivalent |
|---------|--------------|----------------|
| `mvn compile` | Compiles src/main/java | `ant compile` |
| `mvn test` | Compiles + runs tests | `ant test` |
| `mvn package` | Compiles + tests + creates WAR | `ant war` |
| `mvn clean` | Deletes target/ directory | `ant clean` |
| `mvn clean package` | Clean build + WAR | `ant clean war` |

### Step 3: First Build - Compile Only

```bash
mvn compile
```

**What happens:**
1. Maven reads `pom.xml`
2. Downloads dependencies from Maven Central (first time only)
3. Compiles `src/main/java/**/*.java`
4. Outputs to `target/classes/`

**Expected output:**
```
[INFO] Scanning for projects...
[INFO] 
[INFO] -----------------------< com.example:my-webapp >------------------------
[INFO] Building My Web Application 1.0.0-SNAPSHOT
[INFO] --------------------------------[ war ]---------------------------------
[INFO] 
[INFO] --- maven-compiler-plugin:3.12.1:compile (default-compile) @ my-webapp ---
[INFO] Compiling 3 source files to /path/to/my-webapp/target/classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Step 4: Run Unit Tests

```bash
mvn test
```

**What happens:**
1. Compiles main code (if needed)
2. Compiles test code in `src/test/java/`
3. Runs all tests matching `*Test.java`
4. Generates reports in `target/surefire-reports/`

**Expected output:**
```
[INFO] --- maven-surefire-plugin:3.2.5:test (default-test) @ my-webapp ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.GreetingServiceTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.145 s
[INFO] Running com.example.HelloServletTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.089 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Step 5: Package the WAR

```bash
mvn package
```

**What happens:**
1. Compiles code
2. Runs tests
3. Creates WAR file in `target/my-webapp.war`

**Verify the WAR:**
```bash
ls -la target/*.war
# Should show: target/my-webapp.war

# Peek inside the WAR (it's just a ZIP file)
unzip -l target/my-webapp.war | head -20
```

### Step 6: Full Clean Build

```bash
mvn clean package
```

This is the command CI systems typically run. It ensures a clean, reproducible build.

### Step 7: Skip Tests (When Needed)

```bash
mvn package -DskipTests
```

Use sparingly! Tests exist for a reason. But useful when you know tests pass and need a quick rebuild.

### Local Build Summary

```bash
# Complete local CI simulation
mvn clean compile        # Step 1: Compile
mvn test                 # Step 2: Test
mvn package              # Step 3: Package

# Or all at once:
mvn clean package        # Does all three
```

---

## Part 3: Setting Up Harness CI

Now let's automate what you just did manually.

### Step 1: Create Harness Account

1. Go to [app.harness.io](https://app.harness.io)
2. Sign up for free tier (includes 2,000 build minutes/month)
3. Create a new Project (e.g., "Java CI Learning")

### Step 2: Connect Your Git Repository

1. In Harness, go to **Project Settings → Connectors**
2. Click **+ New Connector → Code Repositories → GitHub** (or GitLab/Bitbucket)
3. Configure:
   - **Name**: `my-github-connector`
   - **URL**: Your repository URL
   - **Authentication**: Personal Access Token or OAuth
4. Test connection and save

### Step 3: Set Up Build Infrastructure

You have two options:

**Option A: Harness Cloud (Easiest)**
- No setup required
- Harness manages the build machines
- Just select "Cloud" in pipeline infrastructure

**Option B: Your Own Kubernetes Cluster**
1. Go to **Project Settings → Connectors**
2. Click **+ New Connector → Kubernetes Cluster**
3. Provide cluster credentials
4. Harness will run builds as pods in your cluster

### Step 4: Update Pipeline Configuration

Edit `.harness/pipeline.yaml` in your repository:

```yaml
# Replace these placeholders:
projectIdentifier: YOUR_PROJECT_ID      # From Harness URL
orgIdentifier: YOUR_ORG_ID              # From Harness URL
connectorRef: YOUR_GIT_CONNECTOR        # Connector you created
```

To find your identifiers:
- Look at your Harness URL: `app.harness.io/ng/account/xxx/home/orgs/YOUR_ORG/projects/YOUR_PROJECT`

### Step 5: Create Pipeline in Harness

**Option A: Import from YAML**
1. Go to **Pipelines → + Create Pipeline**
2. Select **Import from Git**
3. Choose your connector and repository
4. Select `.harness/pipeline.yaml`

**Option B: Use Visual Editor**
1. Go to **Pipelines → + Create Pipeline**
2. Name it "Java Maven CI"
3. Add a **Build** stage
4. Add steps using the visual editor:
   - **Run** step: Maven Compile
   - **Run** step: Maven Test
   - **Run** step: Maven Package

### Step 6: Configure Triggers

To run the pipeline automatically on code push:

1. In your pipeline, go to **Triggers**
2. Click **+ New Trigger → Git**
3. Configure:
   - **Name**: "On Push to Main"
   - **Connector**: Your GitHub connector
   - **Event**: Push
   - **Branch**: `main` (or `*` for all branches)
4. Save

---

## Part 4: Running Your First Pipeline

### Manual Run

1. Go to your pipeline in Harness
2. Click **Run**
3. Select branch (e.g., `main`)
4. Click **Run Pipeline**

### Watch the Execution

You'll see each step execute:

```
✓ Maven Compile (15s)
    → Downloading dependencies...
    → Compiling 3 source files...
    → BUILD SUCCESS

✓ Run Unit Tests (25s)
    → Running GreetingServiceTest...
    → Running HelloServletTest...
    → Tests run: 17, Failures: 0

✓ Package WAR (10s)
    → Building WAR: my-webapp.war
    → BUILD SUCCESS

✓ Upload Artifact (5s)
    → Artifact: target/my-webapp.war
```

### Viewing Test Results

1. Click on the "Run Unit Tests" step
2. Go to the **Tests** tab
3. See detailed test results with pass/fail status

### Trigger via Git Push

Now test the automatic trigger:

```bash
# Make a small change
echo "// Updated" >> src/main/java/com/example/GreetingService.java

# Commit and push
git add .
git commit -m "Test CI trigger"
git push origin main
```

Within seconds, you should see a new pipeline execution start in Harness.

---

## Part 5: Next Steps

### Add Code Quality Checks

Add SpotBugs to your `pom.xml`:

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.3.1</version>
</plugin>
```

Then add a pipeline step:
```yaml
- step:
    name: Code Quality
    type: Run
    spec:
      image: maven:3.9-eclipse-temurin-17
      command: mvn spotbugs:check
```

### Add Security Scanning

Harness has built-in Security Testing Orchestration (STO):

```yaml
- step:
    name: OWASP Dependency Check
    type: Security
    spec:
      privileged: true
      settings:
        product_name: owasp
        scan_type: dependency
```

### Add Deployment Stage

After build succeeds, deploy to your environment:

```yaml
- stage:
    name: Deploy to Staging
    type: Deployment
    spec:
      deploymentType: Kubernetes
      service:
        serviceRef: my_webapp_service
      environment:
        environmentRef: staging
```

### Enable Continuous Verification

Harness can automatically verify deployments:

```yaml
- step:
    name: Verify Deployment
    type: Verify
    spec:
      type: Canary
      monitoredService:
        type: Default
      spec:
        sensitivity: MEDIUM
        duration: 5m
```

---

## Quick Reference

### Maven Commands

| Command | Purpose |
|---------|---------|
| `mvn clean` | Delete target/ |
| `mvn compile` | Compile main code |
| `mvn test` | Run unit tests |
| `mvn package` | Create WAR |
| `mvn clean package` | Full clean build |
| `mvn package -DskipTests` | Build without tests |
| `mvn dependency:tree` | Show dependency tree |

### Harness Pipeline Steps

| Step Type | Use For |
|-----------|---------|
| Run | Shell commands, Maven builds |
| BuildAndPushDockerRegistry | Build and push Docker images |
| Security | Security scanning |
| Plugin | Third-party integrations |

### Troubleshooting

**Maven: "Could not find artifact"**
- Check your internet connection
- Verify the dependency coordinates in pom.xml
- Try `mvn clean install -U` to force update

**Harness: "Connector test failed"**
- Verify credentials/tokens haven't expired
- Check network connectivity from Harness to your Git provider
- Ensure the connector has required permissions

**Tests failing in CI but passing locally**
- Check for hardcoded paths or environment-specific values
- Ensure tests don't depend on execution order
- Verify the CI environment has same Java version

---

## Summary

| What You Learned | Ant Equivalent |
|------------------|----------------|
| `pom.xml` defines build | `build.xml` |
| `mvn compile` | `ant compile` |
| `mvn test` | `ant test` |
| `mvn package` | `ant jar` or `ant war` |
| Harness pipeline | Jenkins/Hudson job |
| Git triggers | SCM polling |

The fundamental concepts haven't changed — you're still compiling, testing, and packaging. The tooling has just gotten smarter about automation and conventions.

**Key Insight**: CI isn't magic. It's just running `mvn clean package` automatically every time someone pushes code, then telling you if it broke.
