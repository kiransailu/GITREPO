# AppDynamics AIX Extensions - Build Instructions

## Overview

These zip files contain the basic structure for AppDynamics AIX extensions. To complete the build process, you need to add the compiled JAR files.

## Building Complete Extensions

### Prerequisites

1. **AppDynamics SDK**: Download `appd-exts-commons-2.2.3.jar` from AppDynamics
2. **Java 8+**: Required for compilation
3. **Maven 3.6+**: For building the extensions

### Step 1: Install AppDynamics SDK

```bash
# Download the AppDynamics SDK JAR and install to local Maven repository
mvn install:install-file \
  -Dfile=appd-exts-commons-2.2.3.jar \
  -DgroupId=com.appdynamics \
  -DartifactId=appd-exts-commons \
  -Dversion=2.2.3 \
  -Dpackaging=jar
```

### Step 2: Build Extensions

```bash
# From the project root directory
./build-all-extensions.sh
```

Or build individually:

```bash
# Process Monitor
cd process-monitor-extension
mvn clean package

# NFS Monitor  
cd ../nfs-monitor-extension
mvn clean package

# Service Monitor
cd ../service-monitor-extension  
mvn clean package

# File Change Monitor
cd ../file-change-monitor-extension
mvn clean package
```

### Step 3: Update Zip Files

After building, replace the placeholder files in each zip with the actual compiled JARs:

```bash
# Example for Process Monitor
unzip aix-process-monitor-dist.zip
cp ../process-monitor-extension/target/aix-process-monitor.jar AIXProcessMonitor/
cp ../process-monitor-extension/target/lib/*.jar AIXProcessMonitor/
zip -r aix-process-monitor-dist.zip AIXProcessMonitor/
```

## Current Zip Contents

Each zip file currently contains:
- `config.yml` - Extension configuration
- `monitor.xml` - AppDynamics monitor definition  
- `README_BUILD.txt` - Build placeholder instructions

## Complete Extension Structure (After Build)

After building, each zip should contain:
- `config.yml` - Extension configuration
- `monitor.xml` - AppDynamics monitor definition
- `aix-[extension-name].jar` - Main extension JAR
- `appd-exts-commons-2.2.3.jar` - AppDynamics SDK
- `snakeyaml-1.33.jar` - YAML parsing
- `jackson-databind-2.15.2.jar` - JSON processing
- `httpclient-4.5.14.jar` - HTTP client for config fetching
- Other dependency JARs

## Deployment

Once built with actual JARs:

1. Copy zip files to deployment server
2. Run Ansible playbook: `ansible-playbook deployment/ansible-playbooks/aix-extensions-deploy.yml`
3. Verify extensions appear in AppDynamics Machine Agent logs

## Alternative: Manual Installation

1. Unzip extension to `${MACHINE_AGENT_HOME}/monitors/`
2. Update configuration URLs in `config.yml`
3. Restart AppDynamics Machine Agent
4. Verify metrics appear in AppDynamics Controller

## Troubleshooting

- **Missing SDK**: Extensions won't compile without AppDynamics SDK
- **Java Version**: Ensure Java 8+ is used for compilation
- **Dependencies**: All JAR dependencies must be included in extension directory
- **Permissions**: Ensure Machine Agent user has read access to extension files