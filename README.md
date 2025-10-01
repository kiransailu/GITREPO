# AppDynamics Extensions for AIX Operating Systems

This project contains 4 separate AppDynamics extensions designed specifically for AIX operating systems:

## Extensions

1. **Process Monitor Extension** (`process-monitor-extension/`)
   - Monitors processes using AIX-specific `ps` commands
   - Supports regex-based process matching
   - Reports metrics: Running Instances, CPU%, Memory%, RSS

2. **File Change Monitor Extension** (`file-change-monitor-extension/`)
   - Monitors file modifications and changes
   - Tracks last modified time, file size changes
   - Supports multiple file paths configuration

3. **NFS Monitor Extension** (`nfs-monitor-extension/`)
   - Monitors NFS mounted filesystems using AIX `df` commands
   - Reports: Total Space, Free Space, Used Space, Usage Percentage
   - Supports multiple NFS mount points

4. **Service Monitor Extension** (`service-monitor-extension/`)
   - Monitors AIX system services using `lssrc` commands
   - Tracks service status and availability
   - Supports multiple service monitoring

## Deployment

Each extension is deployed using Ansible playbooks that:
- Copy extension zip files to target AIX machines
- Extract to AppDynamics Machine Agent monitors directory
- Configure centralized JSON-based configuration
- Restart Machine Agent services

## Configuration

All extensions support centralized configuration management via JSON files fetched from GitHub repositories, similar to the provided sample configuration structure.

## Build Requirements

- Java 8+
- Maven 3.6+
- AppDynamics Machine Agent
- AIX Operating System

## Directory Structure

```
/app/
├── process-monitor-extension/
├── file-change-monitor-extension/
├── nfs-monitor-extension/
├── service-monitor-extension/
└── deployment/
    └── ansible-playbooks/
```