def BASE_URL = 'http://artifactory.local:8081/artifactory'

pipeline {
    agent {
        docker {
            image 'hashicorp/terraform:0.12.20'
            args '--entrypoint=""'
        }
    }

    parameters {
        choice name: 'module_name' , choices: ['terraform-aws-key-pair', 'terraform-aws-lambda', 'terraform-aws-route53'], description: ''
        string(defaultValue: '0.0.1', description: '', name: 'version', trim: true)
    }

    options {
        timeout(60)
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    stages {
         stage ('Create variables') {
             steps {
                 script {      
                     def module_data = readJSON file: "./${env.module_name}/tf-module.json"            
                     env.module_name = module_data['name']
                     env.provider = module_data['provider']
                     env.namespace = module_data['namespace']
                     env.version = sprintf("%s%s", module_data['version'][0..-2], BUILD_NUMBER)
                     env.url = "${BASE_URL}/${env.namespace}/${env.module_name}/${env.provider}/${BUILD_NUMBER}/${env.version}.tgz"
                     def jobdesc = sprintf("%s %s",  env.module_name, env.version)
                     currentBuild.description = jobdesc.toLowerCase()
                 }
             }
         }

         stage ('Lint') {
             steps {
               dir("./${env.module_name}") {
                  sh 'terraform fmt -recursive'
               }
           }
         }

         stage('Create TGZ') {
             steps {
               sh "tar -cvzf ${env.version}.tgz ${env.module_name}"
           }

         }
         stage('Upload') {
             steps {
               rtUpload (
                    serverId: "artifactory-01",
                    spec:
                        """{
                        "files": [
                            {
                            "pattern": "./${env.version}.tgz",
                            "target": "${env.namespace}/${env.module_name}/${env.provider}/${BUILD_NUMBER}/"
                            }
                        ]
                        }"""
                )
           }         
         }
         stage('Update registry') {
             steps {
                 sh "echo ${env.url}"
             }
         }
    }
}