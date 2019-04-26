# GITREPO
This is a basic GIT Repo.In this we are going the lean the basic opernation of the GIT. 


## Case-1: 
 What are the steps to be followed in order to make changes in the existing repo. 

 1. `git clone 'https://github.com/kiransailu/GITREPO.git'` {this is a sample URL}
 2. `cd` {into the clone the cloned directory}
 3. make the necessary changes in the file. 
 4. `git status`
 5. `git add .` if you want to add all the files for the changes are made or we can mention the file name itself if we want to add only one or couple of files. 
 6. `git commit -m " explain in a line why changes are being made " `
 7. `git branch <name of the branch> `{we are creating the branch with this command}
 8. `git checkout <name of the branch> `{ we are switch from the current branch to the name of the branch created} 
 9. `git push origin <name of the branch > `{ we are pushing the created branch into the repo}. 

 ## Case-2:


## Basic GIT Commands 

    `git config` :                                                                             :`git config --global user.email sam@google.com`
    `git init`   : *This command is used to create a new GIT repository*                       :`git init $nameof the repo`
    `git add`    : *The git add command can be used in order to add files to the index*        :`git add tmp.txt`
    `git clone`  : *The git clone command is used for repository checking out purposes*        :`git clone $URL`
    `git commit` : *The git commit command is used to commit the changes to the head.*         :`git commit –m “Message to go with the commit here”`
    `git push`   : *A simple push sends the made changes to the master branch *                : git push origin $branchname 

