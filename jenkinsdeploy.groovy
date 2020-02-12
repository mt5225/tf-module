import static groovy.io.FileType.FILES
@NonCPS
def inputParamsString(dir) {
 def list = []
 dir.eachFileRecurse(FILES) {
  // Change `.properties` to the file extension you are interested in
  if (it.name.beginWith('terraform-aws')) {
   // If the full path is required remove `.getName()`
   list << it.getName()
  }
 }
 list.join("\n")
}

def MODULE_LIST = inputParamsString(new File(pwd()))

pipeline {
    agent {
        docker {
            image 'hashicorp/terraform:0.12.20'
            args '--entrypoint=""'
        }
    }

    parameters {
        choice(name: 'module_name' , choices: MODULE_LIST, description: "module name")
    }

    stages {
         stage('fmt') {

         }
         stage ('validate') {

         }
         stage('create tgz') {

         }
         stage('upload') {

         }
     }
}