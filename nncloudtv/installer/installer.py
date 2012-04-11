import shutil, os, re, sys, smtplib, commands
from email.MIMEText import MIMEText

# it does
#   1.copies over root.war
#   2.write md5 to root.md5 file
#   3.write version file
#   4.upload files to server (requires your own key files)


version = raw_input('Enter version number : ')

# copy over root.war
src = "..\\target\\nncloudtv-0.0.1-SNAPSHOT.war"
dst = "root.war"
shutil.copyfile(src, dst)
print "--- generate root.war ---"

# generate md5
md5 = os.popen("tools\md5sums -u root.war").read()
match = re.match("(.*)( .*)", md5)
if match:
	 md5 = match.group(1)
	 print "--- generate md5 = " + md5

dest = open("root.md5", "w")
line = md5 + "\n"
dest.write(line)
dest.close

# write version file
dest = open("version", "w")
line = version + "\n"
dest.write(line)
dest.close
print "--- generate version file ---"

# upload to server
print "\n"
server = raw_input('Environment (1.devel 2.stage 3.deploy) : ')
if server == "1":
  print "--- uploading to devel server ---" 
  os.system("pscp -i awsdev.ppk root.war ubuntu@ec2-174-129-141-179.compute-1.amazonaws.com:/home/ubuntu/files")
if server == "2":
  print "--- uploading to stage server ---"
  os.system("pscp -i prod-west2.ppk root.war ubuntu@ec2-50-112-111-245.us-west-2.compute.amazonaws.com:/home/ubuntu/files")
if server == "3":
  print "--- uploading to deploy server ---"
  