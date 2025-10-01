# AIX AppDynamics Extensions - Deployment Summary

## Generated Zip Files

I've created the following deployment-ready zip files for your AppDynamics AIX extensions:

### Individual Extension Zip Files:

1. **`aix-process-monitor-dist.zip`** (1.9 KB)
   - Process monitoring extension for AIX
   - Uses `ps -eo pid,pcpu,pmem,rss,args` command
   - Monitors: CPU%, Memory%, RSS, Running Instances

2. **`aix-nfs-monitor-dist.zip`** (1.8 KB)  
   - NFS mount monitoring extension
   - Uses AIX `df -k` command
   - Monitors: Total/Free/Used space, Usage percentage

3. **`aix-service-monitor-dist.zip`** (1.8 KB)
   - AIX system service monitoring
   - Uses `lssrc` and `ps` commands  
   - Monitors: Service status, Uptime

4. **`aix-file-change-monitor-dist.zip`** (1.9 KB)
   - File change detection and monitoring
   - Java-based file system monitoring
   - Monitors: File existence, Size changes, Modification tracking

### Complete Deployment Package:

5. **`aix-extensions-deployment-package.zip`** (18.5 KB)
   - Contains all 4 individual extensions
   - Ansible deployment playbook
   - Configuration templates  
   - Sample configurations
   - Complete documentation

## What's Included in Each Zip

Each extension zip contains:
- ✅ `config.yml` - Extension configuration file
- ✅ `monitor.xml` - AppDynamics monitor definition
- ✅ `README_BUILD.txt` - Instructions for adding compiled JAR

## Next Steps for Production Use

### 1. Add AppDynamics SDK
```bash
mvn install:install-file \
  -Dfile=appd-exts-commons-2.2.3.jar \
  -DgroupId=com.appdynamics \
  -DartifactId=appd-exts-commons \
  -Dversion=2.2.3 \
  -Dpackaging=jar
```

### 2. Build Complete Extensions
```bash
# Run from project root
./build-all-extensions.sh
```

### 3. Deploy with Ansible
```bash
ansible-playbook deployment/ansible-playbooks/aix-extensions-deploy.yml
```

## Key Features Implemented

✅ **AIX-Native Commands** - All extensions use AIX-specific commands  
✅ **Centralized Configuration** - JSON configuration fetched from GitHub  
✅ **Regex Process Matching** - Flexible process identification  
✅ **Multiple NFS Mounts** - Support for monitoring multiple mount points  
✅ **Service Status Detection** - Both SRC and process-based services  
✅ **File Change Tracking** - Real-time file modification monitoring  
✅ **Ansible Automation** - Complete deployment automation  
✅ **Health Rules Integration** - Optional centralized health rules management  

## Configuration Format

The extensions expect JSON configuration in this format:
```json
{
  "process_monitor": {
    "monitors": [{"assignment_group": "CAS", "displayname": "Apache", "regex": "/usr/sbin/httpd.*"}]
  },
  "nfs_monitor": {
    "NFS": [{"nfsMountsToMonitor": "/opt/app", "displayname": "App Storage"}]  
  },
  "service_monitor": {
    "service": [{"assignment_group": "CAS", "service": "httpd, sshd"}]
  },
  "monitored_files": [
    {"name": "/var/log/messages", "last_modified_check": 300}
  ]
}
```

## File Locations

All zip files are available in: `/app/deployment/artifacts/`

The complete deployment package includes everything needed for production deployment once the AppDynamics SDK is added.