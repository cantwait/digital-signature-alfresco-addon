# digital-signature-alfresco-addon
Digital Signature Addon for alfresco CE 5.0.d (all-in-one)
This a fork from Digital Signature addon from https://addons.alfresco.com/addons/digital-signing developed by Emmanuel ROUX

Ported the code from SDK 2.0 to 2.1
ALL-IN-ONE project Archetype used

# Create modules

mvn install -DskiptTests to create the AMP modules.

if everything is ok then the two modules are created under:

${repo-amp.folder}/target/repo-amp.amp
${share-amp.folder}/target/share-amp.amp

#Install the modules into alfresco

visit: http://docs.alfresco.com/5.0/tasks/dev-extensions-tutorials-simple-module-install-amp.html

# Run the project 

give exec permission to run.sh or run.bat depending on your OS then run the project using this file

Linux ./run.sh
Windows run.bat

# Debug mode

If you want to debug your app using eclipse then you have to edit run.sh or run.bat and change the command "mvn" and write "mvnDebug" instead, on the eclipse side you must go to Debug configurations -> new Remote Java Application and:

select share-amp project 

leave host and port as it is

change to Source tab and add share-amp and repo-amp

then run in debug mode.
