# How to Access the AIX AppDynamics Extension Zip Files

## Available Zip Files

The following zip files have been created and are ready for download:

### Individual Extension Files:
- `aix-process-monitor-dist.zip` (1.9 KB)
- `aix-nfs-monitor-dist.zip` (1.8 KB)  
- `aix-service-monitor-dist.zip` (1.8 KB)
- `aix-file-change-monitor-dist.zip` (1.9 KB)

### Complete Package:
- `aix-extensions-deployment-package.zip` (18.5 KB) - Contains all extensions + deployment files

## File Locations

All files are located in: `/app/deployment/artifacts/`

## Options to Access These Files:

### Option 1: Download from Current Session
If you have access to this environment, you can copy the files from:
```
/app/deployment/artifacts/
```

### Option 2: Commit to Repository (if you have git access)
```bash
cd /app
git add deployment/artifacts/*.zip
git add deployment/artifacts/BUILD_INSTRUCTIONS.md
git add DEPLOYMENT_SUMMARY.md
git add DOWNLOAD_INSTRUCTIONS.md
git commit -m "Add AIX AppDynamics Extensions deployment artifacts"
git push origin main
```

### Option 3: Copy Content to Your Local Environment
You can recreate these files locally using the project structure I've created:

1. Copy the entire project structure from `/app/`
2. Run the build process locally
3. The zip files will be generated in the same locations

## File Verification

To verify file integrity:
```bash
cd /app/deployment/artifacts
md5sum *.zip
```

Current checksums:
- The files are properly structured AppDynamics extensions
- Each contains config.yml, monitor.xml, and build instructions
- Ready for deployment once AppDynamics SDK is added

## Next Steps

1. **Download the files** from `/app/deployment/artifacts/`
2. **Follow BUILD_INSTRUCTIONS.md** to add the AppDynamics SDK
3. **Use the Ansible playbook** for automated deployment
4. **Test on AIX systems** with your configuration

Let me know if you need help with any of these steps or if you'd like me to modify any of the extensions!