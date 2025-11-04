#!/usr/bin/env python3
"""
Resume to PDF Converter
Converts Kiran's resume into a professionally formatted PDF
"""

from reportlab.lib.pagesizes import letter, A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.lib.enums import TA_LEFT, TA_CENTER, TA_JUSTIFY
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak
from reportlab.lib.colors import HexColor
from datetime import datetime

def create_resume_pdf(output_filename="Kiran_Resume.pdf"):
    """Create a professionally formatted PDF resume"""
    
    # Create document
    doc = SimpleDocTemplate(
        output_filename,
        pagesize=letter,
        rightMargin=0.75*inch,
        leftMargin=0.75*inch,
        topMargin=0.75*inch,
        bottomMargin=0.75*inch
    )
    
    # Container for the 'Flowable' objects
    elements = []
    
    # Define custom styles
    styles = getSampleStyleSheet()
    
    # Header style (name)
    header_style = ParagraphStyle(
        'CustomHeader',
        parent=styles['Heading1'],
        fontSize=24,
        textColor=HexColor('#1a1a1a'),
        spaceAfter=6,
        alignment=TA_CENTER,
        fontName='Helvetica-Bold'
    )
    
    # Contact info style
    contact_style = ParagraphStyle(
        'ContactInfo',
        parent=styles['Normal'],
        fontSize=11,
        textColor=HexColor('#333333'),
        alignment=TA_CENTER,
        spaceAfter=16
    )
    
    # Section header style
    section_header_style = ParagraphStyle(
        'SectionHeader',
        parent=styles['Heading2'],
        fontSize=14,
        textColor=HexColor('#1a5490'),
        spaceBefore=14,
        spaceAfter=8,
        fontName='Helvetica-Bold',
        borderWidth=0,
        borderColor=HexColor('#1a5490'),
        borderPadding=2,
        leftIndent=0
    )
    
    # Body text style
    body_style = ParagraphStyle(
        'CustomBody',
        parent=styles['Normal'],
        fontSize=10,
        textColor=HexColor('#333333'),
        spaceAfter=8,
        alignment=TA_JUSTIFY,
        leading=14
    )
    
    # Job title style
    job_title_style = ParagraphStyle(
        'JobTitle',
        parent=styles['Normal'],
        fontSize=11,
        textColor=HexColor('#1a1a1a'),
        spaceBefore=10,
        spaceAfter=4,
        fontName='Helvetica-Bold'
    )
    
    # Company/Date style
    company_style = ParagraphStyle(
        'Company',
        parent=styles['Normal'],
        fontSize=10,
        textColor=HexColor('#555555'),
        spaceAfter=6,
        fontName='Helvetica-Oblique'
    )
    
    # Bullet point style
    bullet_style = ParagraphStyle(
        'Bullet',
        parent=styles['Normal'],
        fontSize=9.5,
        textColor=HexColor('#333333'),
        leftIndent=20,
        spaceAfter=4,
        bulletIndent=10,
        leading=13
    )
    
    # Skills style
    skills_style = ParagraphStyle(
        'Skills',
        parent=styles['Normal'],
        fontSize=10,
        textColor=HexColor('#333333'),
        leftIndent=10,
        spaceAfter=6,
        leading=14
    )
    
    # ===== HEADER SECTION =====
    elements.append(Paragraph("<b>KIRAN</b>", header_style))
    elements.append(Paragraph("(361) 355-0727 | kiran.11721577@gmail.com", contact_style))
    elements.append(Spacer(1, 0.15*inch))
    
    # ===== PROFESSIONAL SUMMARY =====
    elements.append(Paragraph("<b>PROFESSIONAL SUMMARY</b>", section_header_style))
    
    summary_text = """9+ years of experience in designing Build process, Software Product Development, Process Automation, 
    Build and Deployment Automation, Release Management, Packaging Quality Control, Source Code repository &amp; Environment management. 
    Experience in Designing, Architecting, and implementing scalable cloud-based web application using GCP. Worked on google cloud 
    platform (GCP) services like compute engine, cloud load balancing, cloud storage, cloud SQL, stack driver monitoring and cloud 
    deployment manager. Experience with CI (Continuous Integration) and CD (Continuous Deployment) methodologies using Jenkins. 
    Proficient in defining build pipelines in addition to creating and maintaining multiple jobs AWS management tools such as 
    (EC2, EBS, ASG, RDS, R53) Developed AWS Cloud Formation templates to create custom sized VPC, subnets, EC2 instances, ELB, 
    Security Groups. Experience in using Build Automation tools like Maven, Ant. Proficient in using Configuration Management tools 
    such as Chef and Puppet on windows. Created many customized modules and cookbooks for configuring applications on the client nodes. 
    Integrating and Automation of Version Control Systems for source code management. Worked extensively on Git, SVN. Creating Release 
    Templates, Release pipeline, Release Paths and Inventory in Release management tool. Hands on experience in using Continuous 
    Integration tools like Jenkins, Bamboo and Hudson. Implemented code coverage and unit test plugins with Maven in Jenkins. 
    Worked with optimization of server resources hosted in cloud platforms like AWS, GCP as well as On-premises data centers. 
    Worked on migration of resources from data Centers to AWS and GCP cloud platforms. Automated application deployments in the cloud 
    using Docker and Kubernetes to support Microservices architecture."""
    
    elements.append(Paragraph(summary_text, body_style))
    elements.append(Spacer(1, 0.1*inch))
    
    # ===== TECHNICAL SKILLS =====
    elements.append(Paragraph("<b>TECHNICAL SKILLS &amp; ABILITIES</b>", section_header_style))
    
    skills_data = [
        ("SCM Tools", "CVS, Subversion, GIT, GitHub, GitLab, Visual Studio Team Services (VSTS)"),
        ("DevOps Tools", "Vagrant, Chef, Ansible, Salt, Puppet, Docker Containers, Rundeck, Kubernetes, Dynatrace, AppDynamics, VMWare"),
        ("Monitoring Tools", "Splunk, AppDynamics and CloudWatch"),
        ("Cloud Platform", "Amazon Web Services (AWS) and Google Cloud"),
        ("CI/CD Tools", "Jenkins, TeamCity, Nexus, Sonar, ANT, Maven, Gradle, MS Build, VS Build"),
        ("Deployment Tools", "Genesis, Visual Studio Team Services, UDeploy, Octopus"),
        ("Scripting Languages", "UNIX Shell, Python, Perl, PowerShell (DSC) and RUBY"),
        ("Operating Systems", "Windows, UNIX, LINUX, CentOS, Mac"),
        ("Database", "Oracle 10g/9i, SQL Server, MySQL"),
        ("Web/App Servers", "Web logic, JBOSS, WebSphere, Apache Tomcat, IIS, Nginx"),
        ("Bug Tracking Tools", "JIRA, Rally")
    ]
    
    for skill_category, skill_list in skills_data:
        skill_text = f"<b>{skill_category}:</b> {skill_list}"
        elements.append(Paragraph(skill_text, skills_style))
    
    elements.append(Spacer(1, 0.15*inch))
    
    # ===== PROFESSIONAL EXPERIENCE =====
    elements.append(Paragraph("<b>PROFESSIONAL EXPERIENCE</b>", section_header_style))
    
    # Job 1: Walt Disney (Senior SRE)
    elements.append(Paragraph("<b>Senior SRE</b>", job_title_style))
    elements.append(Paragraph("Walt Disney, Orlando, FL | October 2021 - October 2025", company_style))
    
    disney_sr_responsibilities = [
        "Created Ansible playbooks to manage Application servers, Environments configuration Files, Users, Mount points and Packages in different environments.",
        "Configuration of various plugins for Jenkins for automation of the workflow and integrated with Maven to schedule builds.",
        "Worked with development teams to create Docker files for all application components and setup a private registry in artifactory to store Docker Images.",
        "Established automation of deployments to Kubernetes clusters in EKS using deployment.yaml and service.yaml files.",
        "Deployed Ansible playbooks in AWS environment using Terraform and created Ansible roles using YAML.",
        "Worked with Application Dev team to build solutions on GCP using GKE, Anthos, Google cloud build and terraform/Deployment Manager.",
        "Proficient in modern scripting language Python for automation of build tasks.",
        "Implemented GKE to automate application deployment in the cloud using Docker Automation techniques.",
        "Migrated legacy applications from on-premises VMware to GCP VMware engines.",
        "Provisioned highly available App engine and Compute engine using Terraform.",
        "Managed AWS infrastructure as code (IaaS) using Terraform.",
        "Continuous delivery (CD) with Jenkins and Kubernetes, running continuous deployment pipeline with Jenkins Workflow on Kubernetes.",
        "Automated Compute Engine and Docker Image Builds with Jenkins, Packer, and Kubernetes.",
        "Terraform as infrastructure as code, execution plans, resource graph and change automation.",
        "Performed system administration tasks including deployments, configuration, systems monitoring, troubleshooting, and support.",
        "RCA on resolution of P1, P2 and P3 incidents and documented steps taken in Confluence.",
        "Worked on JIRA for tracking and updating tickets and closely worked with Product owners and managers."
    ]
    
    for resp in disney_sr_responsibilities:
        elements.append(Paragraph(f"• {resp}", bullet_style))
    
    elements.append(Spacer(1, 0.1*inch))
    
    # Job 2: Wells Fargo
    elements.append(Paragraph("<b>DevOps Engineer</b>", job_title_style))
    elements.append(Paragraph("Wells Fargo, Dallas, TX | March 2019 - January 2021", company_style))
    
    wells_fargo_responsibilities = [
        "Worked on integrating SVN into the continuous Integration (CI) environment along with Jenkins.",
        "Designed the overall SCM, CI and Release Management process and workflows.",
        "Implemented branching and merging strategies and automated release branching process in GitHub.",
        "Created end-to-end Jenkins pipeline to absorb code from GitHub, resolve dependencies from Artifactory and deploy using UDeploy.",
        "Automated application deployments using groovy scripts in Jenkins for CI and CD process.",
        "Enabled Quality gates on SonarQube dashboard to control quality of automated deployments.",
        "Automated infrastructure activities like Continuous Deployment, application server setup using Ansible playbooks.",
        "Set up and maintained Logging and Monitoring subsystems using Elasticsearch, Prometheus, Grafana and Alert manager.",
        "Deployed microservices using Kubernetes YAML files.",
        "Performed monitoring, log aggregation and analysis on LogStash/RabbitMQ/Elasticsearch/Kibana.",
        "Deployed Kafka Manager for better insights into Kafka clusters.",
        "Installed Kafka using configuration management tool Ansible.",
        "Implemented spring boot microservices to process messages into Kafka cluster setup.",
        "Implemented to reprocess failure messages in Kafka using offset id.",
        "Created organizational AMI template baseline incorporating AWS Web Application Firewall, Elastic Load Balancer and API gateway services.",
        "Coordinated software code deployments to test and production environments.",
        "Created AppDynamics dashboard for monitoring application components and enabled alerts for failures.",
        "Created Reports, Alerts and Dashboards using Splunk Query language.",
        "Delivered specific versions of various components using UDeploy."
    ]
    
    for resp in wells_fargo_responsibilities:
        elements.append(Paragraph(f"• {resp}", bullet_style))
    
    elements.append(PageBreak())
    
    # Job 3: Walt Disney (Sr. DevOps Engineer)
    elements.append(Paragraph("<b>Sr. DevOps Engineer</b>", job_title_style))
    elements.append(Paragraph("Walt Disney, Orlando, FL | May 2018 - April 2019", company_style))
    
    disney_sr_devops_responsibilities = [
        "Responsible for continuous integration, continuous deployment, version management, source code management and environment management.",
        "Built Continuous Integration environment (Jenkins, Nexus) and Continuous delivery environment (puppet).",
        "Created end-to-end Jenkins pipeline to absorb code from Stash (GitHub), resolve dependencies from Artifactory and deploy using UDeploy.",
        "Installed and Configured Enterprise Jfrog Artifactory.",
        "Extensive knowledge on Artifactory upgrades, backups and LDAP integrations.",
        "Created Splunk dashboard for Artifactory application and monitored server logs.",
        "Created Ansible playbooks to install and setup Artifactory.",
        "Installed and configured foreman with puppet automation for auto-provisioning Linux machines in AWS and VMware environments.",
        "Used Ansible tool for automated deployments in windows environment.",
        "Developed PowerShell and Ansible playbooks to set up Continuous Delivery Pipeline.",
        "Involved in building/maintaining Docker containers using Kubernetes to support Development &amp; testing teams.",
        "On-call support for PROD release and deployments during off-hours.",
        "Created Docker images, uploading/downloading from Docker Hub.",
        "Migrated applications from on-premises to AWS cloud using cloud formation templates.",
        "Designed and developed continuous deployment pipeline integrating Vagrant, Git, Jenkins across geographically separated hosting zones in AWS.",
        "Good knowledge of AWS services like EC2, S3, Glacier, ELB, RDS, SNS, SWF, and EBS.",
        "Used Nagios and Dynatrace for proactive monitoring of infrastructure.",
        "Configured Docker containers and created Docker files for different environments.",
        "Build scripts on AWS cloud for scheduling EC2 auto scaling load balancer with python SDK.",
        "Created GIT repositories with standard directory layout."
    ]
    
    for resp in disney_sr_devops_responsibilities:
        elements.append(Paragraph(f"• {resp}", bullet_style))
    
    elements.append(Spacer(1, 0.1*inch))
    
    # Job 4: LI9 Technologies
    elements.append(Paragraph("<b>DevOps Engineer</b>", job_title_style))
    elements.append(Paragraph("LI9 Technologies, Arizona | August 2017 - May 2018", company_style))
    
    li9_responsibilities = [
        "Installing and configuring Linux using manual and automated tools.",
        "Installing, configuring and administering CI tool Jenkins on Linux machines.",
        "Implemented setup for Master-slave architecture to improve Performance of Jenkins.",
        "Used various plug-ins to extend base functionality of Jenkins to deploy, integrate tests and display reports.",
        "Experienced with writing build jobs using Maven and integrating with Jenkins.",
        "Deployed and configured Chef Server including bootstrapping of chef-client nodes for provisioning.",
        "Created Chef Cookbooks to deploy new software and plugins.",
        "Configured Ansible Tower by writing Ansible playbooks for easy execution.",
        "Wrote Ansible playbooks for automating scaling of Apache web servers.",
        "Setup Chef Server, workstation, client and wrote scripts to deploy applications.",
        "Worked on creating Docker containers and Docker consoles for managing application lifecycle.",
        "Configuration Puppet modules for OpenStack deployment.",
        "Installed and configured Jenkins Enterprise with 5 masters.",
        "Managed configuration of multiple environments using OpenStack and GIT.",
        "Developed Networking policies for Docker Containers.",
        "Worked on Open-source development tools like Docker Containers, Mesos clusters, Kubernetes.",
        "Extensively worked on Mesos cluster environment for application deployments &amp; Docker containers.",
        "Involved in complete deployment configuration using UDeploy.",
        "Install, upgrade, and maintain internal Linux infrastructure with RedHat, Apache.",
        "Written cookbooks for WebSphere and Apache Tomcat.",
        "Worked on infrastructure with Docker containerization."
    ]
    
    for resp in li9_responsibilities:
        elements.append(Paragraph(f"• {resp}", bullet_style))
    
    elements.append(Spacer(1, 0.1*inch))
    
    # Job 5: John Deere
    elements.append(Paragraph("<b>DevOps Engineer</b>", job_title_style))
    elements.append(Paragraph("John Deere, Des Moines, IA | February 2016 - August 2017", company_style))
    
    john_deere_responsibilities = [
        "Building DevOps delivery pipeline with infrastructure and environment provisioning, deploying and monitoring tools.",
        "Designed and developed re-usable components and strategies in cloud infrastructure to support scalability and high-availability.",
        "Troubleshooting errors both Application &amp; JVM, analyzed heap/core plus error logs, configured tuned JVM for optimum performance.",
        "Created and maintained Shell/Perl deployment scripts for Web Logic web application servers.",
        "Worked on Git/GitHub with best practiced strategies.",
        "Used Jenkins to automate the whole pipeline with email notifications enabled.",
        "Designed and implemented backup strategy for all critical systems.",
        "Automated configuration management using Chef.",
        "Wrote cookbooks and recipes to manage nodes and Chef Server.",
        "Configured and maintained Jenkins to implement CI process and integrated with Maven to schedule builds.",
        "Scripted internal automation via Shell and Perl.",
        "Integrated Build Process through Jenkins to various SCM tools like GitHub, SVN.",
        "Wrote ANT Scripts to automate the build process.",
        "Developed Maven build scripts for packaging and deploying services.",
        "Created high availability and scalable AWS stacks using EC2 auto scaling functionality.",
        "Used AWS cloud monitoring extensively to monitor and debug cloud based AWS EC2 services.",
        "Created tagging standards for proper identification and ownership of EC2 instances.",
        "Used spring boot to collecting data from users and packaged as JSON distributed to 43 applications.",
        "Managing Daily Builds, Weekly Builds, Nightly Builds and Builds on fortnight basis.",
        "Integrated Jenkins with Docker, Gradle for Continuous Integration and Continuous Deployment.",
        "Designed and implemented scalable, secure cloud architecture based on AWS.",
        "Used ANT and MAVEN as build tools on Java projects.",
        "Produced client-side validation through JavaScript and Ajax for asynchronous communication."
    ]
    
    for resp in john_deere_responsibilities:
        elements.append(Paragraph(f"• {resp}", bullet_style))
    
    # Build PDF
    doc.build(elements)
    print(f"✅ PDF created successfully: {output_filename}")
    return output_filename

if __name__ == "__main__":
    output_file = "/app/Kiran_Resume_Professional.pdf"
    create_resume_pdf(output_file)
    print(f"\n📄 Resume PDF has been generated at: {output_file}")
