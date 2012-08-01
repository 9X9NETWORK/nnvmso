import os, datetime, shutil, sys

choices = {'server': 'dev', 'version': '0.0.0.1', 'root': 'y', 'cms': 'n', 'mgnt': 'n', 'queue' : 'n'}
versions = {'version':'0.0.0.1', 'rev':'0'}
nncloudtv_installed = False

#================================================================
def get_choices(pkgs):
   choice = raw_input('Environment (1.devel 2.alpha 3.prod/stage) : ')
   if choice == "2":
     choices['server'] = "alpha"
   if choice == "3":                                               
     choices['server'] = 'prod'

   version = raw_input('Enter version number : ')
   versions['version'] = version

   if (pkgs == None): 
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

   global nncloudtv_installed     
   if choices['cms'] == 'y' or choices['mgnt'] == 'y' or choices['queue'] == 'y': 
      choice = raw_input('install nncloudtv (y/n) : ')
      if choice == 'n':           
         nncloudtv_installed = True     
                                                                           
#================================================================
def copy_property_files():
   list=['datanucleus_analytics.properties', 'datanucleus_content.properties', 'datanucleus_nnuser1.properties', 'datanucleus_nnuser2.properties', 'aws.properties', 'memcache.properties', 'queue.properties', 'sns.properties', 'piwik.properties']
   server = choices['server']
   
   os.chdir("../nncloudtv")
   for l in list:                                                                                        
     src = "installer/" + server + "/" + l                                                                    
     dst = "src/main/resources/" + l
     shutil.copyfile(src, dst)

   if choices['cms'] == 'y':   
      os.chdir("../nncms")
      for l in list:                   
        src = "../nncloudtv/installer/" + server + "/" + l                                                
        dst = "src/main/resources/" + l
        shutil.copyfile(src, dst)                                                                        

   if choices['mgnt'] == 'y':    
      os.chdir("../nnadmin")
      src = "installer/" + server + "/resource.properties"                                                                    
      dst = "src/main/resources/" + l
      shutil.copyfile(src, dst)

   os.chdir("../packages")
   
#================================================================                         
def modify_version_file():
   os.chdir("../nncloudtv")
   version = versions['version']  
   source = open(".svn/entries", "rU")
   cnt = 0
   for line in source:
     cnt = cnt + 1
     if cnt == 4:          
       versions['rev'] =  line.rstrip()
       rev = versions['rev']
       break
   source.close
   print "Revision number:" + rev
   now = datetime.datetime.utcnow()
   print now
   old_file = open("src/main/java/com/nncloudtv/web/VersionController.java", "rU")
   new_file = open("src/main/java/com/nncloudtv/web/VersionController.java.tmp",'w')
   for line in old_file:
     if (line.find("String appVersion") > 0):   
        line = "        String appVersion = \"" + version + "\";\n"
     if (line.find("String svn") > 0):   
        line = "        String svn = \"" + rev + "\";\n"
     if (line.find("String packagedTime") > 0):   
        line = "        String packagedTime = \"" + str(now) + "\";\n"
     new_file.write(line)
   old_file.close()
   new_file.close()
                                                               
   os.remove("src/main/java/com/nncloudtv/web/VersionController.java")
   os.rename("src/main/java/com/nncloudtv/web/VersionController.java.tmp", "src/main/java/com/nncloudtv/web/VersionController.java")

   os.chdir("../packages")
#================================================================                      
def build_root(): 
   if choices['root'] == "y":
      print "\n--- root.war ---\n"
      os.chdir("../nncloudtv")
      os.system("mvn clean compile")
      os.system("mvn datanucleus:enhance")
      os.system("mvn war:war")      
      os.system("cp target/nncloudtv-0.0.1-SNAPSHOT.war \"../packages/root.war\"")                  
      os.chdir("../packages")

#================================================================
def build_cms():
   if choices['cms'] == "y":
      global nncloudtv_installed      
      if nncloudtv_installed == False:
         print "nncloudtv install"
         os.chdir("../nncloudtv")
         os.system("mvn clean compile install")
         nncloudtv_installed = True
      os.chdir("../nncms")      
      os.system("mvn clean compile war:war")
      os.system("cp target/cms.war \"../packages/cms.war\"")            
      os.chdir("../packages")                                                     
      print "../packages"

#================================================================
def build_mgnt():    
   if choices['mgnt'] == "y":
      print "\n--- mgnt.war ---\n"
      os.chdir("../nnadmin")
      os.system("mvn clean compile")
      os.system("mvn war:war")
      os.system("cp target/nnadmin-0.0.1-SNAPSHOT.war \"../packages/mgnt.war\"")
      os.chdir("../packages")

#================================================================
def build_queue(): 
   if choices['queue'] == "y":     
      print "\n--- nnqueue.war ---\n"
      global nncloudtv_installed
      if nncloudtv_installed == False:
         print "nncloudtv install"
         os.chdir("../nncloudtv")
         os.system("mvn clean compile install")
         nncloudtv_installed = True
      os.chdir("../nnqueue1")
      os.system("mvn clean compile")
      os.system("mvn clean assembly:assembly")
      os.system("cp target/nnqueue-0.0.1-SNAPSHOT-jar-with-dependencies.jar \"../packages/nnqueue.jar\"")
      os.chdir("../packages")

#================================================================
get_choices(sys.argv)
copy_property_files()
modify_version_file()
build_root()
build_cms()
build_mgnt()
build_queue()

