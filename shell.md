# LOGIN SHELL and NON-LOGIN SHELL 
## *Login shells*
A Login shell is started after a successful login, using /bin/login, by reading the /etc/passwd file.
Login shell is the first process that executes under our user ID when we log in to a session. 
The login process tells the shell to behave as a login shell with a convention: passing argument 0,
which is normally the name of the shell executable, with a “-” character prepended.
For example, for Bash shell it will be -bash.

When Bash is invoked as a Login shell;
 1. Login process calls /etc/profile
 2. /etc/profile calls the scripts in /etc/profile.d/
 3. Login process calls ~/.bash_profile
 4. ~/.bash_profile calls ~/i.bashrc
 5. ~/.bashrc calls /etc/bashrc

A Login shell can be recognized by the following procedure.
Execute the below command in shell.
  echo $0
 If the output is the name of our shell, prepended by a dash, then it is a login shell.
 For example -bash, -su etc.


## *Non login shells*
A Non login shell is started by a program without a login.
In this case, the program just passes the name of the shell executable.
For example, for a Bash shell it will be simply bash.

When bash is invoked as a Non login shell;
 1. Non-login process(shell) calls ~/.bashrc
 2. ~/.bashrc calls /etc/bashrc
 3. /etc/bashrc calls the scripts in /etc/profile.d/

Non login shells include the following.
• Shells created using the below command syntax.
examples: # su | # su USERNAME
• Graphical terminals
• Executed scripts
• Any other bash instances

A Non login shell can be recognized by the following procedure.
Execute the below command in shell.
echo $0
If the output is the name of our shell, does not prepend by a dash, then it is a Non login shell.
For example bash, su etc.
