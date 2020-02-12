import static groovy.io.FileType.FILES
@NonCPS
def inputParamsString(dir) {
 def list = []

 // If you don't want to search recursively then change `eachFileRecurse` -> `eachFile`
 dir.eachFileRecurse(FILES) {
  // Change `.properties` to the file extension you are interested in
  if (it.name.beginWith('terraform-aws')) {
   // If the full path is required remove `.getName()`
   list << it.getName()
  }
 }
 list.join("\n")
}

pipeline {
    agent {
        docker {
            image 'hashicorp/terraform:0.12.20'
            args '--entrypoint=""'
        }

        def inputParams = inputParamsString(new File(pwd()))
        def selectedProperty = input( id: 'tf_module', message: 'Choose module name', parameters: [ [$class: 'ChoiceParameterDefinition', choices: inputParams, description: 'terraform module', name: 'prop'] ])
    
        println "Property: $selectedProperty"
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