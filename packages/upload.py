import shutil, os, re, sys, smtplib, commands
import paramiko
from email.MIMEText import MIMEText
from datetime import datetime

# it does
#   1.copies over root.war
#   2.write md5 to root.md5 file
#   3.write version file
#   4.upload files to server:
#     prerequisites: your own key files, pscp(change to your own scp programs)


choices = {'server': 'dev', 'root': 'y', 'cms': 'n', 'mgnt': 'n', 'queue' : 'n'}
version = ""

#================================================================
def get_choices(pkgs):   
   choice = raw_input('Environment (1.dev2 2.alpha 3.stage 4.prod ) : ')
   if choice is "2":
     choices['server'] = "alpha"
   if choice is "3":                                               
     choices['server'] = 'stage'
   if choice is "4":                                               
     choices['server'] = 'prod'     

   if (len(pkgs) < 2):
      root = raw_input('Build root (y/n) : ')
      choices['root'] = root                          

      mgnt = raw_input('Build mgnt (y/n) : ')
      choices['mgnt'] = mgnt

      cms = raw_input('Build cms (y/n) : ')
      choices['cms'] = cms

      queue = raw_input('Build queue (y/n) : ')
      choices['queue'] = queue
   else:
      choices['root'] = 'n'
      choices['mgnt'] = 'n'
      choices['cms'] = 'n'
      choices['queue'] = 'n'

   for arg in pkgs:
      if (arg == 'root'):
         choices['root'] = 'y'               
      if (arg == 'cms'):
         choices['cms'] = 'y'
      if (arg == 'mgnt'):
         choices['mgnt'] = 'y'
      if (arg == 'queue'):
         choices['queue'] = 'y'

#================================================================
def get_version():       
   if choices['root'] == 'y':
      os.chdir("../nncloudtv")            
      myfile = open("src//main//java//com//nncloudtv//web//VersionController.java", "rU")
      global version
      for line in myfile:                                                                                               
        if (line.find("String appVersion") > 0):
           start = line.find("\"")+1;   
           end = line.rfind("\"");
           version = line[start:end]
      print "version number:" + version
      os.chdir("../packages")   
      if version is "":                 
         print "no version number. exit"
         sys.exit()                    
                                       
      # write version file
      print "--- generate root version file ---"
      dest = open("version", "w")
      line = version
      dest.write(line)
      dest.close() 
                       
#================================================================
def md5_prep(name):
   command = "md5sum " + name + ".war"
   md5 = os.popen(command).read()
   match = re.match("(.*)( .*)", md5)
   if match:
      print "--- generate " + name + ".war md5 = " + md5
   command = name + ".md5"
   dest = open(command, "w")
   line = md5 + " " + name + ".war" + "\x00\x0a"
   dest.write(line)
   dest.close()                                     

# generate md5
def files_prep():  
   if choices['root'] == 'y':
      md5_prep("root")
   if choices['cms'] == 'y':                                                                       
      md5_prep("cms")
   if choices['mgnt'] == 'y':                                                                        
      md5_prep("mgnt")

#================================================================
def folder_create():
   privatekeyfile = os.path.expanduser('~/keys/dev-west2.pem')
   if choices['server'] is "prod":
      privatekeyfile = os.path.expanduser('~/keys/prod-west2.pem')
   mykey = paramiko.RSAKey.from_private_key_file(privatekeyfile)
   ssh = paramiko.SSHClient()
   ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
   server = "alpha_log.9x9.tv"
   if choices['server'] is "prod":
       server = "moveout-log.9x9.tv"
   ssh.connect(server, username = 'ubuntu', pkey = mykey)
   stdin, stdout, stderr = ssh.exec_command('mkdir /var/www/updates/' + version)
   stdin, stdout, stderr = ssh.exec_command('ls /var/www/updates/')
   ssh.close()

#================================================================
def upload():                                             
   print "--- " + datetime.now().strftime("%d/%m/%y %H:%M:%S %Z") + "---"
   #key values
   devel_key = " ~/keys/dev-west2.pem "
   prod_key = " ~/keys/prod-west2.pem "
   #files   
   cms_file = "cms.war "
   root_file = "root.war "
   queue_file = "queue.jar "
   mgnt_file = "mgnt.war "
   #paths
   dev_path = ":/home/ubuntu/files/"
   prod_path = ":/var/www/updates/"
   #command
   scp_command = "scp -i "                  
   #servers
   dev_server = "ubuntu@dev2.9x9.tv"
   alpha_server = "ubuntu@alpha_log.9x9.tv"
   stage_server = "ubuntu@stage.9x9.tv"
   prod_server = "ubuntu@moveout-log.9x9.tv"
   #default values
   key = devel_key
   path = dev_path                                  
   server = dev_server
   
   if choices['server'] is 'alpha':      
      path = prod_path
      server = alpha_server
   if choices['server'] is 'stage':               
      server = stage_server
      key = prod_key
   if choices['server'] is 'prod':
      key = prod_key                               
      path = prod_path
      server = prod_server
   
   #---root
   if choices['root'] == 'y':
      print "upload root"                  
      #scp -i ~/keys/dev-west2.pem root.war ubuntu@devel2.9x9.tv:/home/ubuntu/files/root.war
      if (choices['server'] is "prod") or (choices['server'] is "alpha"):
         folder_create()
         
      if ((choices['server'] is "prod") or (choices['server'] is "alpha")):
          command = scp_command + key + root_file + server + path + version + "/" + root_file
          print command
          os.system(command)                        
          command = scp_command + key + "root.md5 " + server + path + version + "/" + "root.md5"
          print command                                                        
          os.system(command)
          command = scp_command + key + "version " + server + path + version + "/" + "version"
          print command      
          os.system(command)
          command = scp_command + key + "version " + server + path + "version"
          print command
          os.system(command)
      else:                                                          
          command = scp_command + key + root_file + server + path + root_file
          print command
          
   #---cms              
   if choices['cms'] == 'y':
      print "upload cms"
      if ((choices['server'] is "prod") or (choices['server'] is "alpha")):
         command = scp_command + key + "cms.md5 " + server + path + "cms/cms.md5"
         print command
         os.system(command)
         command = scp_command + key + cms_file + server + path + "cms/" + cms_file
         print command
         os.system(command)
      else:   
        command = scp_command + key + cms_file + server + path + cms_file
        print command
        os.system(command)

   #---mgnt   
   if choices['mgnt'] == 'y':
      print "upload mgnt"
      if ((choices['server'] is "prod") or (choices['server'] is "alpha")):
         command = scp_command + key + "mgnt.md5 " + server + path + "mgnt/mgnt.md5"
         print command
         os.system(command)
         command = scp_command + key + mgnt_file + server + path + "mgnt/" + mgnt_file
         print command
         os.system(command)
      else:   
        command = scp_command + key + mgnt_file + server + path + mgnt_file
        print command
        os.system(command)
                
   #---queue    
   if choices['queue'] == 'y': 
      print "upload queue"
      command = scp_command + key + queue_file + server + "/var/www/updates/nnqueue/" + queue_file
      print command                
      os.system(command)                          

   print "--- " + datetime.now().strftime("%d/%m/%y %H:%M:%S %Z") + "---"                                                 
                                
#================================================================
get_choices(sys.argv)
get_version()
files_prep()     
upload()   

#windows                                                               
#md5 = os.popen("tools\md5sums -u root.war").read()
#os.system("pscp -i dev-west2.ppk root.war ubuntu@alpha_log.9x9.tv:/var/www/updates/" + version)
