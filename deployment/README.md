# AIX AppDynamics Extensions Deployment Guide

This directory contains the deployment artifacts and automation for the AIX AppDynamics Extensions.

## Overview

The AIX AppDynamics Extensions provide comprehensive monitoring capabilities for AIX operating systems:

1. **Process Monitor** - Monitors system processes with CPU, memory, and instance metrics
2. **NFS Monitor** - Monitors NFS mount points for space utilization
3. **Service Monitor** - Monitors AIX system services and their status
4. **File Change Monitor** - Monitors file modifications and changes

## Quick Start

### 1. Build Extensions

```bash
# From the project root directory
./build-all-extensions.sh
```

This will:
- Compile all 4 extensions
- Create deployment-ready zip files
- Place artifacts in `deployment/artifacts/`

### 2. Configure Centralized Configuration

Create host-specific JSON configuration files based on the sample:

```bash
# Copy and modify for each target host
cp deployment/sample-configs/sample-host-config.json your-config-repo/host1.json
cp deployment/sample-configs/sample-host-config.json your-config-repo/host2.json
```

### 3. Deploy with Ansible

```bash
# Deploy to all hosts
ansible-playbook -i inventory deployment/ansible-playbooks/aix-extensions-deploy.yml

# Deploy specific extensions only
ansible-playbook -i inventory deployment/ansible-playbooks/aix-extensions-deploy.yml -e deploy_extensions=true

# Deploy with health rules
ansible-playbook -i inventory deployment/ansible-playbooks/aix-extensions-deploy.yml -e health_rules=true
```

## Directory Structure

```
deployment/
├── ansible-playbooks/
│   └── aix-extensions-deploy.yml      # Main deployment playbook
├── templates/
│   ├── process-monitor-config.yml.j2  # Process monitor configuration template
│   ├── nfs-monitor-config.yml.j2      # NFS monitor configuration template
│   ├── service-monitor-config.yml.j2  # Service monitor configuration template
│   ├── file-change-monitor-config.yml.j2 # File change monitor configuration template
│   └── health-rules-config.json.j2    # Health rules configuration template
├── sample-configs/
│   └── sample-host-config.json        # Sample centralized configuration
├── artifacts/                         # Generated deployment artifacts (created by build)
└── README.md                          # This file
```

## Configuration Management

### Centralized Configuration

Each extension fetches its configuration from a centralized JSON file hosted on GitHub (or other HTTP endpoint). The configuration URL supports the `{inventory_hostname}` placeholder.

Example configuration URLs:
- `https://raw.githubusercontent.com/your-org/aix-configs/main/{inventory_hostname}.json`
- `https://config.yourcompany.com/aix-monitoring/{inventory_hostname}.json`

### Configuration Format

The JSON configuration file should contain all monitoring definitions for a specific host:

```json
{
  "process_monitor": {
    "monitors": [
      {
        "assignment_group": "CAS",
        "displayname": "Apache service Monitor",
        "regex": "/usr/sbin/httpd.*",
        "health_rules": "enabled"
      }
    ]
  },
  "nfs_monitor": {
    "NFS": [
      {
        "nfsMountsToMonitor": "/opt/appdynamics",
        "displayname": "AppDynamics Storage",
        "assignment_group": "CAS",
        "health_rules": "enabled"
      }
    ]
  },
  "service_monitor": {
    "service": [
      {
        "assignment_group": "CAS",
        "service": "httpd, sshd, cron"
      }
    ]
  },
  "monitored_files": [
    {
      "name": "/var/log/messages",
      "last_modified_check": 300
    }
  ]
}
```

## Deployment Process

The Ansible playbook follows this process:

1. **Copy Extensions** - Copies zip files to target hosts
2. **Extract Extensions** - Unzips extensions to Machine Agent monitors directory
3. **Download Configuration** - Fetches host-specific JSON configuration
4. **Update Extension Config** - Updates config.yml files with centralized URLs
5. **Restart Machine Agent** - Restarts AppDynamics Machine Agent service
6. **Health Rules** (Optional) - Pushes configuration to Git repository
7. **Cleanup** - Removes temporary files

## Variables

### Required Variables

- `machine_agent_home` - Path to AppDynamics Machine Agent (default: `/opt/appdynamics/machine-agent`)
- `user` - Owner user for extension files (default: `appdynamics`)
- `install_group` - Owner group for extension files (default: `appdynamics`)

### Optional Variables

- `deploy_extensions` - Whether to deploy extensions (default: `true`)
- `health_rules` - Whether to configure health rules (default: `false`)
- `ansible_git_token` - Git token for health rules repository access

### Extension-Specific Variables

Each extension can be customized via the `extensions_to_deploy` list in the playbook.

## AIX-Specific Commands

The extensions use AIX-native commands for monitoring:

### Process Monitor
- Command: `ps -eo pid,pcpu,pmem,rss,args`
- Reports: Running instances, CPU%, Memory%, RSS

### NFS Monitor  
- Command: `df -k <mount_path>`
- Reports: Total space, Used space, Free space, Usage percentage

### Service Monitor
- Commands: `lssrc -s <service>`, `ps -ef`, `ps -o etime= -p <pid>`
- Reports: Service status, Service uptime

### File Change Monitor
- Uses native Java File API
- Reports: File existence, Size, Modification times, Change detection

## Troubleshooting

### Common Issues

1. **Extensions not appearing in AppDynamics**
   - Verify Machine Agent is running: `lssrc -s appdynamics_machine_agent`
   - Check Machine Agent logs: `/opt/appdynamics/machine-agent/logs/`
   - Verify extension directories exist in monitors folder

2. **Configuration not loading**
   - Test configuration URL manually: `curl -I <config_url>`
   - Check extension logs in Machine Agent logs directory
   - Verify JSON syntax with online validators

3. **Metrics not reporting**
   - Check if processes/services/files exist on target system
   - Verify AIX commands work manually: `ps -eo pid,pcpu,pmem,rss,args`
   - Review regex patterns for process matching

### Log Locations

- Machine Agent logs: `${machine_agent_home}/logs/machine-agent.log`
- Extension logs: Included in Machine Agent logs
- System logs: `/var/adm/messages` (AIX)

## Health Rules Integration

When `health_rules=true` is set, the playbook will:

1. Clone the health rules Git repository
2. Generate updated configuration with controller/tier information
3. Commit and push changes to the repository
4. Enable centralized health rules management

## Security Considerations

1. **Git Token Security** - Store `ansible_git_token` in Ansible Vault
2. **File Permissions** - Extensions run with Machine Agent user permissions
3. **Network Access** - Ensure hosts can access configuration URLs
4. **Command Execution** - Extensions execute AIX commands with Agent privileges

## Support

For issues or questions:
1. Check Machine Agent logs for extension errors
2. Verify AIX command compatibility on target systems
3. Test configuration JSON syntax and accessibility
4. Review AppDynamics extension documentation

## Version Information

- **Extensions Version**: 1.0.0
- **AppDynamics SDK**: 2.2.3
- **Java Requirements**: Java 8+
- **AIX Compatibility**: AIX 7.1+